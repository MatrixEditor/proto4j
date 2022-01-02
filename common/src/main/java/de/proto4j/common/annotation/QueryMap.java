package de.proto4j.common.annotation; //@date 31.12.2021

import java.util.HashMap;
import java.util.Map;

public final class QueryMap<K> {

    @Item(name = "QueryMap.map", isAccessible = false, hasSetter = false)
    private final Map<K, Object> map = new HashMap<>();

    @Item(name = "QueryMap.queries", isAccessible = false, hasSetter = false)
    private final Map<K, Query> queryMap = new HashMap<>();

    public void add(K k, Object o) {
        if (!map.containsKey(k)) {
            Query q = AnnotationUtil.lookup(o.getClass(), Query.class, o.getClass()::getAnnotation);
            if (q == null) q = AnnotationUtil.lookup(o.getClass(), Query.class);
            if (q != null) {
                map.put(k, o);
                queryMap.put(k, q);
            }
        }
    }

    public void remove(K k) {
        map.remove(k);
        queryMap.remove(k);
    }

    public Query getQuery(K k) {
        return queryMap.get(k);
    }

    public Object get(K k) {
        return map.get(k);
    }

    public void addAll(Map<? extends K, Object> m) {
        if (m != null && !m.isEmpty()) {
            m.forEach(this::add);
        }
    }

}
