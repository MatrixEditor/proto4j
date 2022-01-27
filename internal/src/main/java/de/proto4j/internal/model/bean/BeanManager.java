package de.proto4j.internal.model.bean;//@date 24.01.2022

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public interface BeanManager {

    Object getInstanceOf(Class<?> c, Class<? extends Annotation> type);

    SimpleBeanCacheList findAll(Class<? extends Annotation> type);

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
