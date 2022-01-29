package de.proto4j.internal.model; //@date 23.01.2022

import de.proto4j.annotation.message.PacketModifier;
import de.proto4j.internal.RootPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Reflections {

    public static final Predicate<String> FILENAME_FILTER = (x) -> x.endsWith("class") && !x.contains("package-info");

    public static final Set<Class<?>> cachedRuntimeClasses = new HashSet<>();

    private static final ClassScanner system_scanner = newScanner();

    public static ClassScanner newScanner() {
        return new ClassScannerImpl();
    }

    public static ClassScanner getSystemScanner() {
        return system_scanner;
    }

    public static Set<Class<?>> getPacketClasses(String _package) {
        return getPacketClasses(_package, getSystemScanner());
    }

    public static Set<Class<?>> getPacketClasses(String _package, ClassScanner classScanner) {
        if (cachedRuntimeClasses.isEmpty()) {
            Set<Class<?>> classSet = new HashSet<>();
            Set<Class<?>> s        = classScanner.getAllClassesOfPackage(_package);

            s.forEach(x -> {if (PacketModifier.isMessage(x)) classSet.add(x);});
            return classSet;
        } else {
            return cachedRuntimeClasses.stream().filter(PacketModifier::isMessage)
                                       .collect(Collectors.toCollection(HashSet::new));
        }

    }

    public static Set<Class<?>> findRequestControllers(Collection<Class<?>> c) {
        return Collections.emptySet();//findByAnnotationAsSet(c, c0 -> c0.isAnnotationPresent(RequestController.class));
    }

    public static <T> Set<T> findByAnnotationAsSet(Collection<T> c, Predicate<T> p) {
        return findByAnnotation(c, p, Collectors.toSet());
    }

    public static <T, C extends Collection<T>> C findByAnnotation(Collection<T> c, Predicate<T> p,
                                                                  Collector<? super T, ?, C> collector) {
        return c.stream().filter(p).collect(collector);
    }

    public static synchronized Set<Class<?>> getClassesFromMain(Class<?> main) {
        return getClassesFromMain(main, getSystemScanner());
    }

    public static synchronized Set<Class<?>> getClassesFromMain(Class<?> main, ClassScanner classScanner) {
        if (!cachedRuntimeClasses.isEmpty()) return cachedRuntimeClasses;

        PackageGenerator pkg = new PackageGeneratorImpl(main);
        while (pkg.hasMoreElements()) {
            cachedRuntimeClasses.addAll(classScanner.getAllClassesOfPackage(pkg.nextElement(), true));
        }
        return cachedRuntimeClasses;
    }

    private static class PackageGeneratorImpl implements PackageGenerator {

        private final List<String> package_names = new LinkedList<>();

        private int pointer = 0;

        public PackageGeneratorImpl(Class<?> main) {
            if (main.isAnnotationPresent(RootPackage.class)) {
                String p = main.getDeclaredAnnotation(RootPackage.class).path();
                if (p.length() == 0) p = main.getPackageName();
                addIfAbsent(p);
            } else {
                List<String> names = generateNames(main);

                packageInfoLookup(ClassLoader.getSystemClassLoader(), names);
                if (package_names.isEmpty())
                    filterPackages(names);
            }
        }

        /**
         * Tests if this enumeration contains more elements.
         *
         * @return {@code true} if and only if this enumeration object
         *         contains at least one more element to provide;
         *         {@code false} otherwise.
         */
        @Override
        public boolean hasMoreElements() {
            return pointer < package_names.size();
        }

        /**
         * Returns the next element of this enumeration if this enumeration
         * object has at least one more element to provide.
         *
         * @return the next element of this enumeration.
         * @throws NoSuchElementException if no more elements exist.
         */
        @Override
        public String nextElement() {
            return package_names.get(pointer++);
        }

        private void filterPackages(List<String> names) {
            Package[] packages = Package.getPackages();
            for (Package p : packages) {
                String p_name = p.getName();
                cleanup:
                {
                    for (String n : names) {
                        if (p_name.contains(n)) break cleanup;
                    }
                    continue;
                }
                addIfAbsent(p_name);
            }
        }

        private List<String> generateNames(Class<?> main) {
            String[] s = main.getPackageName().split("[.]");
            List<String> names = IntStream.range(2, s.length)
                                          .mapToObj(i -> String.join(".", Arrays.copyOfRange(s, 0, i)))
                                          .collect(Collectors.toCollection(LinkedList::new));
            addIfAbsent(main.getPackageName());
            return names;
        }

        private void packageInfoLookup(ClassLoader cl, List<String> names) {
            for (String n : names) {
                InputStream is = cl.getResourceAsStream(n.replaceAll("[.]", "/"));

                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        while (br.ready()) {
                            String name = br.readLine();
                            if (name != null) {
                                if (name.contains("package-info")) {

                                    Class<?> package_info = Class.forName(name + ".package-info");
                                    if (package_info.isAnnotationPresent(RootPackage.class))
                                        addIfAbsent(name);
                                } else if (!name.contains(".class")) packageInfoLookup(cl, List.of(name));
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        }

        private void addIfAbsent(String name) {
            if (!package_names.contains(name))
                package_names.add(name);
        }
    }

    public static class ClassScannerImpl implements ClassScanner {
        @Override
        public Set<Class<?>> getAllClassesOfPackage(String _package, boolean r) {
            if (_package == null) return Collections.emptySet();

            Set<Class<?>> classSet = new HashSet<>();

            ClassLoader cl = ClassLoader.getSystemClassLoader();
            InputStream is = cl.getResourceAsStream(_package.replaceAll("[.]", "/"));

            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    while (br.ready()) {
                        String name = br.readLine();
                        if (name != null && FILENAME_FILTER.test(name)) {
                            Class<?> c = Class.forName(makeClassName(_package, name.split("[.]")[0]));

                            if (c.getDeclaredClasses().length != 0) {
                                classSet.addAll(Arrays.asList(c.getDeclaredClasses()));
                            }
                            classSet.add(c);
                        } else if (r && name != null && !name.contains("package-info")) {
                            classSet.addAll(getAllClassesOfPackage(makeClassName(_package, name), r));
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    // ignore
                }
            }
            return classSet;
        }

        private String makeClassName(String p, String n) {
            return String.join(".", p, n);
        }
    }
}
