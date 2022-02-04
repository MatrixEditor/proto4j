package de.proto4j.internal.method; //@date 02.02.2022

import de.proto4j.annotation.server.requests.Param;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class MethodLookup {

    public static boolean select(Object o, Parameter[] parameters) {
        if (o == null || parameters == null) return false;

        switch (parameters.length) {
            case 0:
                return false;

            case 1:
                if (parameters[0].getType().isAssignableFrom(o.getClass())) {
                    return true;
                } else if (parameters[0].isAnnotationPresent(Param.class)) {
                    Field f = hasField(parameters[0].getDeclaredAnnotation(Param.class).value(), o);
                    return f != null && parameters[0].getType().isAssignableFrom(f.getType());
                }
                return false;

            default:
                for (Parameter p : parameters) {
                    if (p.isAnnotationPresent(Param.class)) {
                        Field f = hasField(p.getDeclaredAnnotation(Param.class).value(), o);
                        if (f == null || !p.getType().isAssignableFrom(f.getType()))
                            return false;
                    }
                }
                return true;
        }
    }

    public static Object[] tryCreate(Object o, Parameter[] parameters) throws IllegalAccessException {
        if (o == null || parameters == null) throw new IllegalArgumentException();

        switch (parameters.length) {
            case 0:
                return new Object[0];

            case 1:
                if (parameters[0].getType().isAssignableFrom(o.getClass())) {
                    return new Object[]{o};
                } else if (parameters[0].isAnnotationPresent(Param.class)) {
                    Object[] args = new Object[]{get(parameters[0], o)};
                    if (args[0] != null) {
                        return args;
                    }
                }
                throw new IllegalArgumentException();

            default:
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Object x = get(parameters[i], o);
                    if (x == null) throw new IllegalArgumentException();
                    args[i] = x;
                }
                return args;
        }
    }

    private static Object get(Parameter p, Object o) throws IllegalAccessException {
        if (p.getType().isAssignableFrom(o.getClass())) return o;

        String vname = p.getDeclaredAnnotation(Param.class).value();
        if (vname.length() > 0) {
            Field f0 = hasField(vname, o);
            if (f0 == null) return null;
            if (p.getType().isAssignableFrom(f0.getType())) {
                return new Object[]{f0.get(o)};
            }
        }
        return null;
    }

    private static Field hasField(String name, Object o) {
        for (Field f0 : o.getClass().getDeclaredFields()) {
            if (!f0.canAccess(o)) f0.setAccessible(true);

            if (f0.getName().equals(name)) {
                return f0;
            }
        }
        return null;
    }
}
