package de.proto4j.internal.model.bean; //@date 24.01.2022

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class MapBeanManager implements BeanManager {

    private final Map<Class<? extends Annotation>, SimpleBeanCacheList> beanCache = new HashMap<>();

    @Override
    public SimpleBeanCacheList findAll(Class<? extends Annotation> type) {
        if (type != null && beanCache.containsKey(type)) {
            return beanCache.get(type);
        }
        return new SimpleBeanCacheList();
    }

    @Override
    public Object getInstanceOf(Class<?> c, Class<? extends Annotation> type) {
        if (c != null && type != null) {
            if (beanCache.containsKey(type)) {
                SimpleBeanCacheList bc = beanCache.get(type);
                if (bc.contains(c)) {
                    return bc.getInstance(c);
                }
            }
        }
        return null;
    }

    @Override
    public boolean map(Class<?> theClassToBeMapped, Class<? extends Annotation> theAnnotationClass) {
        if (theClassToBeMapped != null && theAnnotationClass != null) {
            if (beanCache.containsKey(theAnnotationClass)) {
                Object o = null;
                try {
                    o = theClassToBeMapped.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    return false;
                }
                beanCache.get(theAnnotationClass).add(theClassToBeMapped, o);
                return true;
            }
        }
        return false;
    }

    protected boolean mapIfAbsent(Class<?> c, Class<? extends Annotation> a, Object instance) {
        if (c != null && a != null) {
            if (!beanCache.containsKey(a)) addCategory(a);
            beanCache.get(a).add(c, instance);
            return true;
        }
        return false;
    }

    @Override
    public boolean mapIfAbsent(Class<?> theClassToBeMapped, Class<? extends Annotation> theAnnotationClass) {
        if (!beanCache.containsKey(theAnnotationClass)) addCategory(theAnnotationClass);
        return map(theClassToBeMapped, theAnnotationClass);
    }

    @Override
    public void removeBean(Class<?> c, Class<? extends Annotation> type) {
        if (c != null && type != null) {
            if (beanCache.containsKey(type)) beanCache.get(type).remove(c);
        }
    }

    @Override
    public boolean isEmpty() {
        return beanCache.isEmpty();
    }

    @Override
    public void clear() {
        beanCache.clear();
    }

    @Override
    public int size() {
        return beanCache.values().stream().mapToInt(SimpleBeanCacheList::size).sum();
    }

    @Override
    public int categorySize() {
        return beanCache.size();
    }

    @Override
    public void addCategory(Class<? extends Annotation> type) {
        if (type != null) {
            if (!beanCache.containsKey(type)) {
                beanCache.put(type, new SimpleBeanCacheList());
            }
        }
    }
}
