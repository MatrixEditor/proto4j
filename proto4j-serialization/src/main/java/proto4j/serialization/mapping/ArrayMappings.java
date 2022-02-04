package proto4j.serialization.mapping; //@date 01.02.2022

import java.lang.reflect.Array;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;

public class ArrayMappings {

    private static final List<Class<?>> arrayTypes =
            List.of(String[].class, byte[].class, Byte[].class, int[].class,
                    Integer[].class, long[].class, Long[].class, float[].class,
                    Float[].class, double[].class, Double[].class, short[].class,
                    Short[].class, char[].class, Character[].class, Boolean[].class,
                    boolean[].class);

    private ArrayMappings() {}

    public static boolean contains(Class<?> type) {
        return arrayTypes.contains(type);
    }

    public static Mapping<Function<String, ?>> valueOf(Class<?> type) {
        return new ArrayMapping(s -> read(s, type), type);
    }

    private static Object read(String s, Class<?> type) {
        if (!arrayTypes.contains(type))
            throw new IllegalArgumentException("type is not a primitive array");

        String[] vec = s.split("[-]");
        int      len = Integer.parseInt(vec[0]);

        StringTokenizer tokenizer = new StringTokenizer(vec[1], "|");
        Object          array     = Array.newInstance(type, len);
        for (int i = 0; tokenizer.hasMoreElements(); ++i) {
            String next = tokenizer.nextToken();

            Function<String, ?> o = PrimitiveMappings.valueOf(type.getComponentType()).getInvoker();
            Array.set(array, i, o.apply(next));
        }
        return array;
    }

    public static Mapping<Function<String, ?>> valueOf(String value) {
        try {
            return valueOf(Class.forName(value));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static class ArrayMapping implements Mapping<Function<String, ?>> {
        private final Function<String, ?> invoker;
        private final Class<?>            type;

        private ArrayMapping(Function<String, ?> invoker, Class<?> type) {
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
