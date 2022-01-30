package de.proto4j.annotation.threding;//@date 30.01.2022

import java.util.function.Supplier;

@FunctionalInterface
public interface ParallelSupplier {

    public <T> T supplyAsync(Supplier<T> s);
}
