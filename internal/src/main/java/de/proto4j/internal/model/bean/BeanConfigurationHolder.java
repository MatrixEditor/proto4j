package de.proto4j.internal.model.bean;//@date 24.01.2022

import java.lang.annotation.Annotation;

public interface BeanConfigurationHolder extends Comparable<BeanConfigurationHolder> {

    Class<?> getType();

    Class<? extends Annotation> getAnnotationType();

    @Override
    default int compareTo(BeanConfigurationHolder o) {
        return (o.getType() == getType() && o.getAnnotationType() == getAnnotationType()) ? 0 : 1;
    }

    default boolean isEqual(Class<?> c, Class<? extends Annotation> a) {
        return (c == getType() && a == getAnnotationType());
    }

    static BeanConfigurationHolder of(Class<?> c, Class<? extends Annotation> a) {
        return new BeanConfigurationHolder() {
            @Override
            public Class<?> getType() {
                return c;
            }

            @Override
            public Class<? extends Annotation> getAnnotationType() {
                return a;
            }
        };
    }

}
