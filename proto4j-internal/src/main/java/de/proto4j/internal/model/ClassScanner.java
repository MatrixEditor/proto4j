package de.proto4j.internal.model;//@date 23.01.2022

import java.util.Set;

public interface ClassScanner {

    default Set<Class<?>> getAllClassesOfPackage(String _package) {
        return getAllClassesOfPackage(_package, false);
    }

    Set<Class<?>> getAllClassesOfPackage(String _package, boolean recursive);
}
