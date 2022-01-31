package de.proto4j.internal.io.desc; //@date 31.01.2022

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static de.proto4j.internal.io.desc.DescProviderFactory.*;

public class PrimitiveFieldDesc extends FieldDesc {

    private static final Map<Class<?>, Function<String, ?>> mappings = new HashMap<>();

    static {
        mappings.put(Integer.class, Integer::parseInt);
        mappings.put(int.class, Integer::parseInt);
        mappings.put(Boolean.class, Boolean::parseBoolean);
        mappings.put(boolean.class, Boolean::parseBoolean);
        mappings.put(char.class, s -> s.charAt(0));
        mappings.put(Character.class, s -> s.charAt(0));
        mappings.put(byte.class, Byte::parseByte);
        mappings.put(Byte.class, Byte::parseByte);
        mappings.put(double.class, Double::valueOf);
        mappings.put(Double.class, Double::valueOf);
        mappings.put(short.class, Short::valueOf);
        mappings.put(Short.class, Short::valueOf);
        mappings.put(Float.class, Float::valueOf);
        mappings.put(float.class, Float::valueOf);

        mappings.put(String.class, s -> s.replaceAll(DELIMITER_REPLACEMENT, DEFAULT_DELIMITER)
                                         .replaceAll(RF_REPLACEMENT, "\r")
                                         .replaceAll(LF_REPLACEMENT, "\n")
                                         .replaceAll("%B", "|"));
    }

    public PrimitiveFieldDesc() {
    }

    public static Set<Class<?>> primitiveTypes() {
        return mappings.keySet();
    }

    public static Map<Class<?>, Function<String, ?>> mappings() {
        return mappings;
    }

    @Override
    public String serialize() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append(DEFAULT_DELIMITER).append("[");
        sb.append(getModifiers()).append("]").append(DEFAULT_DELIMITER);

        String valueDesc = getValue() != null ? getValue().toString() : NULL_VALUE;
        sb.append(getType().getName()).append(DEFAULT_DELIMITER);
        sb.append(valueDesc.length()).append(DEFAULT_DELIMITER).append(valueDesc);

        sb.append(DEFAULT_DELIMITER).append(RF);
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
        for (Map.Entry<Class<?>, Function<String, ?>> entry : mappings.entrySet()) {
            Class<?>            c = entry.getKey();
            Function<String, ?> f = entry.getValue();

            if (c.getSimpleName().equals(values[2])) {
                setValue(f.apply(values[4]));
                setType(c);
                break;
            }
        }
        return this;
    }
}
