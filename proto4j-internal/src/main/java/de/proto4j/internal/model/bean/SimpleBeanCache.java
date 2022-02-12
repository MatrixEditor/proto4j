package de.proto4j.internal.model.bean; //@date 12.02.2022

public class SimpleBeanCache {
    private Object   instance;
    private Class<?> mappedClass;

    public SimpleBeanCache(Class<?> mappedClass, Object instance) {
        this.instance    = instance;
        this.mappedClass = mappedClass;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Class<?> getMappedClass() {
        return mappedClass;
    }

    public void setMappedClass(Class<?> mappedClass) {
        this.mappedClass = mappedClass;
    }
}
