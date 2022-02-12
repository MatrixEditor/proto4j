package de.proto4j.stream;//@date 12.02.2022

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface InterruptedStream<E> extends ReusableStream<E> {

    InterruptedStream<E> forEach(Consumer<? super E> consumer);

    InterruptedStream<E> filter(Predicate<? super E> predicate);

    <R> InterruptedStream<R> map(Function<? super E, ? extends R> function);

    void yield(E e);

    SequenceStream<E> sequencedStream();
}
