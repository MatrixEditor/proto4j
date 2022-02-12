package de.proto4j.stream; //@date 12.02.2022

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Streams {

    private Streams() {}

    public static <T> SequenceStream<T> emptySequencedStream() {
        return new GenericSequencedStream<>();
    }

    public static <T> SequenceStream<T> prepare(Collection<? super T> collection) {
        Objects.requireNonNull(collection);
        return new GenericSequencedStream<>(collection.toArray());
    }

    public static <T> SequenceStream<T> prepare(T[] t) {
        return new GenericSequencedStream<>(t);
    }

    public static <T> InterruptedStream<T> prepareInterruptedStream() {
        return new GenericInterruptedStream<>();
    }

    public static <T> InterruptedStream<T> prepareInterrupted(Consumer<? super T> c) {
        return prepareInterrupted(c, (x) -> true);
    }

    public static <T> InterruptedStream<T> prepareInterrupted(Predicate<? super T> p) {
        return prepareInterrupted(x -> {}, p);
    }

    public static <T> InterruptedStream<T> prepareInterrupted(Consumer<? super T> c, Predicate<? super T> p) {
        return new GenericInterruptedStream<T>().forEach(c).filter(p);
    }

    public static AnnotatedStream prepareAnnotated(AnnotatedElement e) {
        return new AnnotatedStream(e);
    }
}
