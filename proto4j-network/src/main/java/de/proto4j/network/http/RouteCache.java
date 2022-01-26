package de.proto4j.network.http; //@date 25.01.2022

import java.lang.reflect.Method;

public final class RouteCache {

    private final Class<?> mappedClass;

    private final Object instance;

    private final String route;

    private final Method method;

    public RouteCache(Class<?> mappedClass, Object instance, String route, Method method) {
        this.mappedClass = mappedClass;
        this.instance    = instance;
        this.route       = route;
        this.method      = method;
    }

    public Class<?> getMappedClass() {
        return mappedClass;
    }

    public Object getInstance() {
        return instance;
    }

    public String getRoute() {
        return route;
    }

    public Method getMethod() {
        return method;
    }
}
