package de.proto4j.stream;//@date 11.02.2022

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface SequenceStream<E> extends ReusableStream<E> {

    void forEach(Consumer<? super E> consumer);

    SequenceStream<E> slice(Predicate<? super E> predicate);

    <R> SequenceStream<R> map(Function<? super E, ? extends R> mapper);

    <R> R collect(Predicate<? super E> predicate, Function<E[], R> collector);

    E find(Predicate<? super E> predicate);

    boolean contains(Predicate<? super E> predicate);

    default E[] sliceToArray(Predicate<? super E> predicate) {
        //noinspection unchecked
        return (E[]) slice(predicate).toArray();
    }

    default <R> R[] mapToArray(Function<? super E, ? extends R> function) {
        //noinspection unchecked
        return (R[]) map(function).toArray();
    }

    default Optional<E> optionalFind(Predicate<? super E> predicate) {
        return Optional.of(find(predicate));
    }

    default int count(Predicate<? super E> predicate) {
        return slice(predicate).size();
    }

}
