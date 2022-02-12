package de.proto4j.stream;//@date 12.02.2022

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Consumer;

public class AnnotatedStream extends GenericSequencedStream<AnnotatedElement> {

    public AnnotatedStream(Object[] elem) {
        super(elem);
    }

    public AnnotatedStream(Object o) {
        super(new Object[] {o});
    }

    public <A extends Annotation> void ifPresent(Class<A> a, Consumer<? super A> consumer) {
        if (isPresent(a)) {
            consumer.accept(find(x -> x.isAnnotationPresent(a)).getDeclaredAnnotation(a));
        }
    }

    public <V extends Annotation> AnnotationStream<V> collect(Class<V> v) {
        Object[] elem =  slice(x -> x.isAnnotationPresent(v)).toArray();
        return new AnnotationStreamImpl<>(v, elem);
    }

    public <A extends Annotation> boolean isPresent(Class<A> a) {
        return contains(x -> x.isAnnotationPresent(a));
    }

    private static class AnnotationStreamImpl<A extends Annotation> extends GenericSequencedStream<A>
            implements AnnotationStream<A> {

        private final Class<A> aClass;

        private AnnotationStreamImpl(Class<A> aClass, Object[] elem) {
            super(elem);
            this.aClass = aClass;
        }

        @Override
        public Class<A> annotationType() {
            return aClass;
        }
    }
}
