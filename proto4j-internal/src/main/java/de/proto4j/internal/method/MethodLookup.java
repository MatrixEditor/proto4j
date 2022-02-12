package de.proto4j.internal.method; //@date 02.02.2022

import de.proto4j.annotation.Markup;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class MethodLookup {

    public static boolean select(Object o, Parameter[] parameters) {
        return select(o, parameters, null);
    }

    public static boolean select(Object o, Parameter[] parameters, Class<?> includeParameter) {
        if (o == null || parameters == null) return false;

        switch (parameters.length) {
            case 0:
                return false;

            case 1:
                if (parameters[0].getType().isAssignableFrom(o.getClass())
                        || parameters[0].getType().isAssignableFrom(includeParameter)) {
                    return true;
                } else if (Markup.isParam(parameters[0])) {
                    Field f = hasField(Markup.getParamMarkup(parameters[0]).value(), o);
                    return f != null && parameters[0].getType().isAssignableFrom(f.getType());
                }
                return false;

            default:
                for (Parameter p : parameters) {
                    if (Markup.isParam(p)) {
                        Field f = hasField(Markup.getParamMarkup(p).value(), o);
                        if (f == null) {
                            if (p.getType().isAssignableFrom(includeParameter))
                                continue;
                        }
                        if (f == null || !p.getType().isAssignableFrom(f.getType()))
                            return false;
                    }
                    if (p.getType().isAssignableFrom(includeParameter)
                        || p.getType().isAssignableFrom(o.getClass())) {
                        continue;
                    }

                    return false;
                }
                return true;
        }
    }

    public static Object[] tryCreate(Object o, Object exchange, Parameter[] parameters) throws IllegalAccessException {
        if (o == null || parameters == null) throw new IllegalArgumentException();

        switch (parameters.length) {
            case 0:
                return new Object[0];

            case 1:
                if (parameters[0].getType().isAssignableFrom(o.getClass())) {
                    return new Object[]{o};
                } else if (parameters[0].getType().isAssignableFrom(o.getClass())) {
                    return new Object[] {exchange};
                } else if (Markup.isParam(parameters[0])) {
                    Object[] args = new Object[]{get(parameters[0], o, exchange)};
                    if (args[0] != null) {
                        return args;
                    }
                }
                throw new IllegalArgumentException();

            default:
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Object x = get(parameters[i], o ,exchange);
                    if (x == null) throw new IllegalArgumentException();
                    args[i] = x;
                }
                return args;
        }
    }

    private static Object get(Parameter p, Object o, Object exchange) throws IllegalAccessException {
        if (p.getType().isAssignableFrom(o.getClass())) return o;
        if (p.getType().isAssignableFrom(exchange.getClass())) return exchange;

        String vname = Markup.getParamMarkup(p).value();
        if (vname.length() > 0) {
            Field f0 = hasField(vname, o);
            if (f0 == null) {
                if (o.getClass().isAssignableFrom(p.getType())) return o;
                return null;
            }
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
