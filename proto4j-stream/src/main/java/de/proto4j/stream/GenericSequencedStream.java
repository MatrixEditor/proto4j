package de.proto4j.stream; //@date 12.02.2022

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GenericSequencedStream<E> extends AbstractSequencedStream<E> {

    public GenericSequencedStream(Object[] elem) {
        elements = elem;
    }

    GenericSequencedStream() {

    }

    @Override
    protected void reset() {
        pos = 0;
    }

    @Override
    protected int next() {
        int x = pos;
        pos = x + 1;
        return x;
    }

    @Override
    protected boolean hasNext() {
        return pos < size();
    }

    @Override
    public SequenceStream<E> slice(Predicate<? super E> predicate) {
        if (size() == 0 || predicate == null) return this;

        int count = 0;

        Object[] copy = new Object[size()];
        while (hasNext()) {
            int p = next();
            E   e = get(p);

            if (predicate.test(e)) {
                copy[count] = e;
                count++;
            }
        }
        reset();

        return new GenericSequencedStream<E>(Arrays.copyOf(copy, count));
    }

    @Override
    public Stream<E> defaultStream() {
        //noinspection unchecked
        E[] classes = Arrays.copyOf((E[])toArray(), size());
        return Arrays.stream(classes);
    }

    @Override
    public <R> SequenceStream<R> map(Function<? super E, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        Object[] rs = new Object[size()];
        for (int i = 0; hasNext(); i++) {
            E e = get(next());
            rs[i] = mapper.apply(e);
        }
        return new GenericSequencedStream<>(rs);
    }
}
