package de.proto4j.serialization.mapping; //@date 01.02.2022

import de.proto4j.serialization.DescProviderFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PrimitiveMappings {

    private static final List<Class<?>> primitiveTypes =
            List.of(String.class, byte.class, int.class, double.class, float.class, long.class,
                    char.class, short.class, Byte.class, Integer.class, Double.class, Float.class,
                    Long.class, Character.class, Short.class, boolean.class, Boolean.class);

    private static final List<Class<?>> internalTypes =
            List.of(byte.class, int.class, double.class, float.class, long.class,
                    char.class, short.class, boolean.class);

    private PrimitiveMappings() {}

    public static boolean contains(Class<?> type) {
        return primitiveTypes.contains(type);
    }

    public static boolean containsName(String name) {
        return primitiveTypes.stream().map(Class::getName).collect(Collectors.toList()).contains(name);
    }

    public static Mapping<Function<String, ?>> valueOf(Class<?> type) {
        return valueOf(type.getName());
    }

    public static Mapping<Function<String, ?>> valueOf(String name) {
        if (name.equals(String.class.getName())) {
            return new PrimitiveMapping(s -> s.replaceAll(DescProviderFactory.DELIMITER_REPLACEMENT, DescProviderFactory.DEFAULT_DELIMITER)
                                              .replaceAll(DescProviderFactory.RF_REPLACEMENT, "\r")
                                              .replaceAll(DescProviderFactory.LF_REPLACEMENT, "\n")
                                              .replaceAll("%B", "|"), String.class);
        } else {
            if (name.equals(Character.class.getName()) || name.equals(char.class.getName())) {
                return new PrimitiveMapping(s -> s.charAt(0), Character.class);
            } else {
                if (internalTypes.stream().map(Class::getName).anyMatch(c -> c.equals(name))) {
                    return wrapType(name);
                }

                try {
                    Class<?> type = Class.forName(name);

                    Method m = type.getDeclaredMethod("valueOf", String.class);
                    assert Modifier.isStatic(m.getModifiers());

                    return new PrimitiveMapping(s -> {
                        try {
                            return m.invoke(null, s);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new IllegalArgumentException("invocation failed");
                        }
                    }, type);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException("cannot create class");
                }

            }
        }
    }

    public static PrimitiveMapping wrapType(String name) {
        Function<String, ?> f;
        Class<?>            c;
        switch (name) {
            case "int":
                f = Integer::valueOf; c = int.class; break;
            case "double":
                f = Double::valueOf; c = double.class; break;
            case "float":
                f = Float::valueOf; c = float.class; break;
            case "char":
                f = s -> s.charAt(0); c = char.class; break;
            case "boolean":
                f = Boolean::valueOf; c = boolean.class; break;
            case "short":
                f = Short::valueOf; c = short.class; break;
            case "long":
                f = Long::valueOf; c = long.class; break;
            default:
                throw new IllegalStateException();
        }
        return new PrimitiveMapping(f, c);
    }

    private static class PrimitiveMapping implements Mapping<Function<String, ?>> {

        private final Function<String, ?> invoker;
        private final Class<?>            type;

        private PrimitiveMapping(Function<String, ?> invoker, Class<?> type) {
            this.invoker = invoker;
            this.type    = type;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Function<String, ?> getInvoker() {
            return invoker;
        }
    }
}
