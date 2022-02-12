package de.proto4j.internal.model.bean; //@date 24.01.2022

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class SimpleBeanCacheList implements Iterable<SimpleBeanCache> {

    private final List<SimpleBeanCache> beanCaches = new LinkedList<>();

    public int size() {
        return beanCaches.size();
    }

    public boolean isEmpty() {
        return beanCaches.isEmpty();
    }

    public boolean contains(Class<?> o) {
        return beanCaches.stream().anyMatch(p -> p.getMappedClass() == o);
    }

    public boolean add(Class<?> c, Object o) {
        return beanCaches.add(new SimpleBeanCache(c, o));
    }

    public Object getInstance(Class<?> c) {
        SimpleBeanCache sbc = get(c);
        return sbc == null ? null : sbc.getInstance();
    }

    private SimpleBeanCache get(Class<?> c) {
        if (contains(c)) {
            Optional<SimpleBeanCache> o = beanCaches.stream()
                                                                .filter(p -> p.getMappedClass() == c)
                                                                .findFirst();
            if (o.isPresent()) return o.get();
        }
        return null;
    }

    @Override
    public Iterator<SimpleBeanCache> iterator() {
        return beanCaches.iterator();
    }

    public boolean remove(Class<?> o) {
        if (contains(o)) {
            return beanCaches.removeIf(sbc -> sbc.getMappedClass() == o);
        }
        return false;
    }

    public void clear() {
        beanCaches.clear();
    }
}
