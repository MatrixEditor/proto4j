package de.proto4j.stream; //@date 12.02.2022

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractInterruptedStream<E> implements InterruptedStream<E> {

    protected final List<E> elements = new ArrayList<>();

    private Predicate<? super E> predicate;
    private Consumer<? super E>  consumer;

    protected abstract void yield0(E e);

    @Override
    public InterruptedStream<E> filter(Predicate<? super E> predicate) {
        this.predicate = predicate;
        return this;
    }

    @Override
    public InterruptedStream<E> forEach(Consumer<? super E> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public void yield(E e) {
        if (Objects.requireNonNullElse(predicate, x -> true).test(e)) {
            yield0(e);
        }
    }

    @Override
    public SequenceStream<E> sequencedStream() {
        return new GenericSequencedStream<>(elements.toArray());
    }

    protected Consumer<? super E> getAcceptor() {
        return consumer;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Object[] toArray() {
        return elements.toArray();
    }
}
