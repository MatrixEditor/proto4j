package de.proto4j.internal.io.desc; //@date 31.01.2022

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.Ref;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static de.proto4j.internal.io.desc.DescProviderFactory.*;

public class RepeatedFieldDesc extends FieldDesc {

    private static final Map<Class<?>, Function<String, ?>> parser = new HashMap<>();

    private static final Map<Class<?>, BiFunction<String, Class<?>, ?>> collections = new HashMap<>();

    private static final List<Class<?>> arrayTypes = new LinkedList<>();

    static {
        arrayTypes.addAll(List.of(String[].class, byte[].class, Byte[].class, int[].class,
                                  Integer[].class, long[].class, Long[].class, float[].class,
                                  Float[].class, double[].class, Double[].class, short[].class,
                                  Short[].class, char[].class, Character[].class, Boolean[].class,
                                  boolean[].class));

        for (Class<?> c : arrayTypes()) {
            parser.put(c, s -> readArray(s, c));
        }

        collections.put(LinkedList.class, (s, t) -> readCollection(s, t, new LinkedList<>()));
        collections.put(ArrayList.class, (s, t) -> readCollection(s, t, new ArrayList<>()));
        collections.put(Vector.class, (s, t) -> readCollection(s, t, new Vector<>()));
        collections.put(HashSet.class, (s, t) -> readCollection(s, t, new HashSet<>()));
    }

    public static List<Class<?>> arrayTypes() {
        return arrayTypes;
    }

    public static Set<Class<?>> collectionTypes() {
        return Collections.unmodifiableSet(collections.keySet());
    }

    private static Object readArray(String s, Class<?> type) {
        if (!arrayTypes().contains(type))
            throw new IllegalArgumentException("type is not a primitive array");

        String[] vec = s.split("[-]");
        int len = Integer.parseInt(vec[0]);

        StringTokenizer tokenizer = new StringTokenizer(vec[1], "|");
        Object array = Array.newInstance(type, len);
        for (int i = 0; tokenizer.hasMoreElements(); ++i) {
            String next = tokenizer.nextToken();
            Function<String, ?> o = PrimitiveFieldDesc.mappings().get(type.getComponentType());
            Array.set(array, i, o.apply(next));
        }
        return array;
    }

    public static <T> Collection<T> readCollection(String s, Class<T> primitiveType, Collection<T> collType) {

        StringTokenizer tokenizer = new StringTokenizer(s, "|");

        while (tokenizer.hasMoreElements()) {
            //noinspection unchecked
            collType.add((T) PrimitiveFieldDesc.mappings().get(primitiveType).apply(tokenizer.nextToken()));
        }
        return collType;
    }

    private String fromSimpleArray(Object array) {
        if (array == null) return NULL_VALUE;

        StringJoiner sj = new StringJoiner("|");
        int len = Array.getLength(array);

        if (len == 0) return NULL_VALUE;
        for (int i = 0; i < len; i++) {
            Object o = Array.get(array, i);
            if (o != null) {
                if (o instanceof String) {
                   sj.add(o.toString().replaceAll(DELIMITER_REPLACEMENT, DEFAULT_DELIMITER)
                                  .replaceAll(RF_REPLACEMENT, "\r")
                                  .replaceAll(LF_REPLACEMENT, "\n")
                                  .replaceAll("%B", "|"));
                } else sj.add(o.toString());
            }
        }
        return sj.toString();
    }

    private String fromSimpleCollection(Object o) {
        if (!(o instanceof Collection))
            throw new IllegalArgumentException("Object not instance of collection");

        Collection<?> c = (Collection<?>) o;
        if (c.size() == 0) return NULL_VALUE;

        Object[] array = c.toArray();
        if (PrimitiveFieldDesc.primitiveTypes().contains(array[0].getClass())) {
            return fromSimpleArray(array);
        } else throw new IllegalArgumentException("collectionType is not primitive");
    }

    @Override
    public String serialize() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append(DEFAULT_DELIMITER).append("[");
        sb.append(getModifiers()).append("]").append(DEFAULT_DELIMITER);


        String valueDesc;
        if (arrayTypes().contains(getType())) {
            sb.append(getType().getName()).append(DEFAULT_DELIMITER);

            valueDesc = fromSimpleArray(getValue());
            sb.append(Array.getLength(getValue()));
        } else if (Collection.class.isAssignableFrom(getType())) {
            sb.append(getType().getName()).append("&")
              .append(((Collection<?>) getValue()).toArray()[0].getClass().getName())
              .append(DEFAULT_DELIMITER);

            valueDesc = fromSimpleCollection(getValue());
            sb.append(((Collection<?>)getValue()).size());
        } else throw new UnsupportedTemporalTypeException(getType().getSimpleName());

        sb.append(DEFAULT_DELIMITER).append(valueDesc).append(DEFAULT_DELIMITER).append(RF);
        return sb.toString();
    }

    @Override
    public FieldDesc read(String serialized) throws IOException {
        if (serialized == null || serialized.length() == 0) throw new IllegalArgumentException();

        String[] values = serialized.split("[-]");
        setOrdinal(Integer.parseInt(values[0]));
        setModifiers(Integer.parseInt(values[1].substring(1, values[1].length() - 1)));

        if (hasModifier(OPTIONAL_MODIFIER)) {
            if (values[4].equals("null")) {
                setValue(null);
                return this;
            }
        }
        if (values[2].contains("&")) {
            String[] classes = values[2].split("[&]");
            Class<?> collClass = null;
            try {
                collClass = Class.forName(classes[0]);
            } catch (ClassNotFoundException e) {
                if (classes[0].toLowerCase().contains("set")) collClass = HashSet.class;
                else collClass = LinkedList.class;
            }
            try {
                Class<?> typeClass = Class.forName(classes[1]);

                if (!collections.containsKey(collClass)) {
                    if (classes[0].toLowerCase().contains("set")) collClass = HashSet.class;
                    else collClass = LinkedList.class;
                }
                setValue(collections.get(collClass).apply(values[4], typeClass));
                setType(collClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("couldn't resolve type or collection-Class");
            }
            return this;
        }
        for (Map.Entry<Class<?>, Function<String, ?>> entry : parser.entrySet()) {
            Class<?>            c = entry.getKey();
            Function<String, ?> f = entry.getValue();

            if (c.getSimpleName().equals(values[2])) {
                setValue(f.apply(values[3] + "-" + values[4]));
                setType(c);
                break;
            }
        }
        return this;
    }
}
