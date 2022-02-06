package de.proto4j.serialization.desc; //@date 31.01.2022

import de.proto4j.serialization.DescProviderFactory;
import de.proto4j.serialization.mapping.ArrayMappings;
import de.proto4j.serialization.mapping.CollectionMappings;
import de.proto4j.serialization.mapping.Mapping;
import de.proto4j.serialization.mapping.PrimitiveMappings;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringJoiner;
import java.util.function.Function;

public class RepeatedFieldDesc extends FieldDesc {

    private String fromSimpleArray(Object array) {
        if (array == null) return NULL_VALUE;

        StringJoiner sj = new StringJoiner("|");
        int len = Array.getLength(array);

        if (len == 0) return NULL_VALUE;
        for (int i = 0; i < len; i++) {
            Object o = Array.get(array, i);
            if (o != null) {
                if (o instanceof String) {
                   sj.add(o.toString().replaceAll(DescProviderFactory.DELIMITER_REPLACEMENT, DescProviderFactory.DEFAULT_DELIMITER)
                                  .replaceAll(DescProviderFactory.RF_REPLACEMENT, "\r")
                                  .replaceAll(DescProviderFactory.LF_REPLACEMENT, "\n")
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
        if (PrimitiveMappings.contains(array[0].getClass())) {
            return fromSimpleArray(array);
        } else throw new IllegalArgumentException("collectionType is not primitive");
    }

    @Override
    public String serialize() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append(DescProviderFactory.DEFAULT_DELIMITER).append("[");
        sb.append(getModifiers()).append("]").append(DescProviderFactory.DEFAULT_DELIMITER);


        String valueDesc;
        if (ArrayMappings.contains(getType())) {
            sb.append(getType().getName()).append(DescProviderFactory.DEFAULT_DELIMITER);

            valueDesc = fromSimpleArray(getValue());
            sb.append(Array.getLength(getValue()));
        } else if (Collection.class.isAssignableFrom(getType())) {
            sb.append(getType().getName()).append("&")
              .append(((Collection<?>) getValue()).toArray()[0].getClass().getName())
              .append(DescProviderFactory.DEFAULT_DELIMITER);

            valueDesc = fromSimpleCollection(getValue());
            sb.append(((Collection<?>)getValue()).size());
        } else throw new UnsupportedTemporalTypeException(getType().getSimpleName());

        sb.append(DescProviderFactory.DEFAULT_DELIMITER).append(valueDesc).append(DescProviderFactory.DEFAULT_DELIMITER).append(
                DescProviderFactory.RF);
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

                if (!CollectionMappings.contains(collClass)) {
                    if (classes[0].toLowerCase().contains("set")) collClass = HashSet.class;
                    else collClass = LinkedList.class;
                }
                setValue(CollectionMappings.valueOf(collClass).getInvoker().apply(values[4], typeClass));
                setType(collClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("couldn't resolve type or collection-Class");
            }
            return this;
        }
        //here we have an array
        Mapping<Function<String, ?>> m = ArrayMappings.valueOf(values[2]);
        if (m != null) {
            setType(m.getType());
            setValue(m.getInvoker().apply(values[3] + "-" + values[4]));
        }
        return this;
    }
}
