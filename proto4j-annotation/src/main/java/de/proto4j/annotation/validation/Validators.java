package de.proto4j.annotation.validation; //@date 25.01.2022


import java.lang.reflect.AnnotatedElement;
import java.util.Hashtable;
import java.util.Map;

public class Validators {

    public static final Validators SYSTEM_VALIDATORS = new Validators();

    private final Map<Class<? extends BaseValidator>, Object> instance_map = new Hashtable<>();

    private Validators() {}

    public static Validators getSystemValidators() { return SYSTEM_VALIDATORS; }

    public static Validators newInstance() { return new Validators(); }

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

    public Object get(Validator v) {
        if (v != null) {
            Class<? extends BaseValidator> c = v.validatorType();
            return get(c);
        }
        return null;
    }

    public Object get(AnnotatedElement e) {
        if (e != null && e.isAnnotationPresent(Validator.class)) {
            return get(e.getDeclaredAnnotation(Validator.class));
        }
        return null;
    }

    public boolean add(Validator v) {
        if (v != null) {
            return add(v.validatorType());
        }
        return false;
    }

    public boolean add(Class<? extends BaseValidator> c) {
        if (c != null) try {
            return add(c, c.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException ex) { /**/ }
        return false;
    }

    private boolean add(Class<? extends BaseValidator> c, Object instance) {
        if (c != null && instance != null && !instance_map.containsKey(c)) {
            instance_map.put(c, instance);
            return true;
        }
        return false;
    }
}
