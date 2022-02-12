package de.proto4j.internal; //@date 12.02.2022

import de.proto4j.internal.logger.Logger;
import de.proto4j.internal.logger.PrintColor;
import de.proto4j.internal.logger.PrintService;
import de.proto4j.internal.model.PackageGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class DefaultPackageGenerator implements PackageGenerator {

    private static final Logger LOGGER = PrintService.createLogger(PackageGenerator.class);

    private final List<String> package_names = new LinkedList<>();

    private int pointer = 0;

    public DefaultPackageGenerator(Class<?> main) {
        if (main.isAnnotationPresent(MessageRoot.class)) {
            addIfAbsent(main.getDeclaredAnnotation(MessageRoot.class).value());
        }

        if (main.isAnnotationPresent(RootPackage.class)) {
            String p = main.getDeclaredAnnotation(RootPackage.class).value();
            if (p.length() == 0) p = main.getPackageName();
            addIfAbsent(p);
            String s = p.substring(0, p.lastIndexOf('.'));
            addIfAbsent(s);
        } else {
            List<String> names = generateNames(main);

            packageInfoLookup(ClassLoader.getSystemClassLoader(), names);
            names.forEach(this::addIfAbsent);
            if (package_names.isEmpty())
                filterPackages(names);
        }
    }

    @Override
    public boolean hasMoreElements() {
        return pointer < package_names.size();
    }

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
                    LOGGER.except(PrintColor.BRIGHT_MAGENTA, e);
                }
            }
        }
    }

    private void addIfAbsent(String name) {
        if (!package_names.contains(name))
            package_names.add(name);
    }
}
