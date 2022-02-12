package de.proto4j.internal.model.bean;//@date 24.01.2022

import de.proto4j.stream.SequenceStream;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public interface BeanManager {

    Object getInstanceOf(Class<?> c, Class<? extends Annotation> type);

    SequenceStream<SimpleBeanCache> findAll(Predicate<Class<? extends Annotation>> predicate);

    void addCategory(Class<? extends Annotation> type);

    boolean map(Class<?> theClassToBeMapped, Class<? extends Annotation> theAnnotationClass);

    boolean mapIfAbsent(Class<?> theClassToBeMapped, Class<? extends Annotation> theAnnotationClass);

    void removeBean(Class<?> c, Class<? extends Annotation> type);

    boolean isEmpty();

    void clear();

    int categorySize();

    int size();

    default Set<Map.Entry<Class<? extends Annotation>, SimpleBeanCacheList>> asSet() {
        return null;
    }

}
