package de.proto4j.internal.model.bean; //@date 27.01.2022

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public class UnmodifiableBeanManager extends MapBeanManager {

    public static UnmodifiableBeanManager of(BeanManager bm) {
        return new UnmodifiableBeanManager(bm);
    }

    private UnmodifiableBeanManager(BeanManager manager) {
        if (manager != null) {
            Set<Map.Entry<Class<? extends Annotation>, SimpleBeanCacheList>> s = manager.asSet();
            if (s != null) {
                copy(s);
            }
        }
    }

    private void copy(Set<Map.Entry<Class<? extends Annotation>, SimpleBeanCacheList>> s) {
        s.forEach(e -> {
            if (e != null) {
                if (e.getKey() != null && e.getValue() != null) {
                    e.getValue().iterator()
                     .forEachRemaining(x -> this.mapIfAbsent(x.getMappedClass(), e.getKey(), x.getInstance()));
                }
            }

        });
    }

    @Override
    public void removeBean(Class<?> c, Class<? extends Annotation> type) {
        throw new UnsupportedOperationException();
    }
}
