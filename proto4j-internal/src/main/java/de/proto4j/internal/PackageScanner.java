package de.proto4j.internal;//@date 12.02.2022

import de.proto4j.stream.InterruptedStream;

import java.util.function.Predicate;

@FunctionalInterface
public interface PackageScanner {

    Predicate<String> FILENAME_FILTER = (x) -> x.endsWith("class") && !x.contains("package-info");

    default void readInto(InterruptedStream<Class<?>> stream, String pkg) {
        readInto(stream, pkg, false);
    }

    void readInto(InterruptedStream<Class<?>> stream, String pkg, boolean recursive);

}
