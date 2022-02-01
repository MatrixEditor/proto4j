package de.proto4j.internal.io.desc.mapping; //@date 01.02.2022

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;

import static de.proto4j.internal.io.desc.DescProviderFactory.*;

public class PrimitiveMappings {

    private static final List<Class<?>> primitiveTypes =
            List.of(String.class, byte.class, int.class, double.class, float.class, long.class,
                    char.class, short.class, Byte.class, Integer.class, Double.class, Float.class,
                    Long.class, Character.class, Short.class, boolean.class, Boolean.class);

    private PrimitiveMappings() {}

    public static boolean contains(Class<?> type) {
        return primitiveTypes.contains(type);
    }

    public static Mapping<Function<String, ?>> valueOf(Class<?> type) {
        return valueOf(type.getName());
    }

    public static Mapping<Function<String, ?>> valueOf(String name) {
        if (name.equals(String.class.getName())) {
            return new PrimitiveMapping(s -> s.replaceAll(DELIMITER_REPLACEMENT, DEFAULT_DELIMITER)
                                              .replaceAll(RF_REPLACEMENT, "\r")
                                              .replaceAll(LF_REPLACEMENT, "\n")
                                              .replaceAll("%B", "|"), String.class);
        } else {
            if (name.equals(Character.class.getName()) || name.equals(char.class.getName())) {
                return new PrimitiveMapping(s -> s.charAt(0), Character.class);
            } else {
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
