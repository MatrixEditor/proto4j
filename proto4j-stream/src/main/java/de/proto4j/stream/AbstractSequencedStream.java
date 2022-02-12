package de.proto4j.stream; //@date 12.02.2022


import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractSequencedStream<E> implements SequenceStream<E> {

    protected volatile Object[] elements = {};

    protected volatile int pos = 0;

    protected abstract void reset();

    protected abstract int next();

    protected abstract boolean hasNext();

    protected E get(int index) {
        assert index > 0 && index < size();
        //noinspection unchecked
        return (E) elements[index];
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        if (consumer != null && size() > 0) {
            while (hasNext()) {
                E e = get(next());
                consumer.accept(e);
            }
            reset();
        }
    }


    @Override
    public <R> R collect(Predicate<? super E> predicate, Function<E[], R> collector) {
        return collector.apply(sliceToArray(predicate));
    }

    @Override
    public E find(Predicate<? super E> predicate) {
        E result = null;

        if (predicate != null) {
            while (hasNext()) {
                E e = get(next());
                if (predicate.test(e)) {
                    result = e;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public boolean contains(Predicate<? super E> predicate) {
        return find(predicate) != null;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elements, size());
    }

    @Override
    public int size() {
        return elements.length;
    }
}
