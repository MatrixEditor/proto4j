package de.proto4j.stream;//@date 12.02.2022

import java.util.stream.Stream;

public interface ReusableStream<E> {

    Stream<E> defaultStream();

    Object[] toArray();

    int size();
}
