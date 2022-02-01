package de.proto4j.internal.io.desc.mapping;//@date 01.02.2022

public interface Mapping<F_T> {

    Class<?> getType();

    F_T getInvoker();
}
