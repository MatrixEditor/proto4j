package de.proto4j.internal; //@date 12.02.2022

import de.proto4j.annotation.message.PacketModifier;
import de.proto4j.internal.model.PackageGenerator;
import de.proto4j.internal.model.PackageScanner;
import de.proto4j.stream.GenericInterruptedStream;
import de.proto4j.stream.InterruptedStream;
import de.proto4j.stream.SequenceStream;

import java.util.function.Predicate;

public final class Packages {

    private static final ClassLoader LOADER = ClassLoader.getSystemClassLoader();

    public static SequenceStream<Class<?>> readClasses(Class<?> main) {
        return readClasses(main, x -> true);
    }

    public static SequenceStream<Class<?>> readClasses(Class<?> main, Predicate<Class<?>> predicate) {
        return readClasses(main, false, predicate);
    }

    public static SequenceStream<Class<?>> readClasses(Class<?> main, boolean recursive) {
        return readClasses(main, recursive, x -> true);
    }

    public static SequenceStream<Class<?>> readClasses(Class<?> main, boolean recursive,
                                                       Predicate<Class<?>> predicate) {
        PackageGenerator generator = new DefaultPackageGenerator(main);
        PackageScanner   scanner   = new DefaultPackageScanner();

        InterruptedStream<Class<?>> stream = new GenericInterruptedStream<>();
        stream.filter(predicate);

        while (generator.hasMoreElements()) {
            scanner.readInto(stream, generator.nextElement(), recursive);
        }
        return stream.sequencedStream();
    }

    public static boolean isOtherSubPackage(String base, Class<?> c) {
        if (!PacketModifier.isMessage(c)) {
            String[] package_name = c.getPackageName().split("[.]");
            String[] main_names   = base.split("[.]");

            for (int i = 0; i < package_name.length; i++) {
                if (i == main_names.length) return false;

                if (package_name[i].equals(main_names[i])) {
                    if (i + 1 == package_name.length) return false;
                    continue;
                }
                break;
            }
            return true;
        } else return false;
    }

    public static Predicate<Class<?>> getSubPackageFilter(Class<?> main) {
        return s -> isOtherSubPackage(main.getPackageName(), s);
    }

    public static Predicate<Class<?>> getPackageFilter(Class<?> main) {
        return c -> !isOtherSubPackage(main.getPackageName(), c);
    }

}
