package de.proto4j.stream; //@date 12.02.2022

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class GenericInterruptedStream<E> extends AbstractInterruptedStream<E> {

    public GenericInterruptedStream() {
    }

    public GenericInterruptedStream(Object[] elem) {
        for (Object o : elem) {
            //noinspection unchecked
            elements.add((E) o);
        }
    }

    @Override
    protected void yield0(E e) {
        if (!elements.contains(e)) elements.add(e);
        Objects.requireNonNullElse(getAcceptor(), (x) -> {}).accept(e);
    }

    @Override
    public <R> InterruptedStream<R> map(Function<? super E, ? extends R> function) {
        Objects.requireNonNull(function);
        Object[] copy = new Object[size()];

        for (int i = 0; i < size(); i++) {
            E e = elements.get(i);
            copy[i] = function.apply(e);
        }
        return new GenericInterruptedStream<R>(copy);
    }

    @Override
    public Stream<E> defaultStream() {
        return null;
    }
}
