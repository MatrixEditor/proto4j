package de.proto4j.annotation.selection; //@date 25.01.2022


import de.proto4j.annotation.server.requests.RequestHandler;

import java.lang.reflect.AnnotatedElement;
import java.util.Hashtable;
import java.util.Map;

public class Selectors {

    public static final Selectors SYSTEM_SELECTORS = new Selectors();

    private final Map<Class<? extends Selector>, Object> instance_map = new Hashtable<>();

    private Selectors()                           {}

    public static Selectors getSystemSelectors() { return SYSTEM_SELECTORS; }

    public static Selectors newInstance()         { return new Selectors(); }

    public boolean contains(Class<?> c) {
        if (c != null) {
            return instance_map.containsKey(c);
        }
        return false;
    }

    public Object get(Class<?> c) {
        if (c != null) {
            return instance_map.get(c);
        }
        return null;
    }

    public Object get(RequestHandler v) {
        if (v != null) {
            Class<? extends Selector> c = v.selectorType();
            return get(c);
        }
        return null;
    }

    public Object get(AnnotatedElement e) {
        if (e != null && e.isAnnotationPresent(RequestHandler.class)) {
            return get(e.getDeclaredAnnotation(RequestHandler.class));
        }
        return null;
    }

    public boolean add(RequestHandler v) {
        if (v != null) {
            return add(v.selectorType());
        }
        return false;
    }

    public boolean add(Class<? extends Selector> c) {
        if (c != null) try {
            return add(c, c.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException ex) { /**/ }
        return false;
    }

    private boolean add(Class<? extends Selector> c, Object instance) {
        if (c != null && instance != null && !instance_map.containsKey(c)) {
            instance_map.put(c, instance);
            return true;
        }
        return false;
    }
}
