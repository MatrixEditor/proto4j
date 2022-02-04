package proto4j.serialization; //@date 04.02.2022

import proto4j.serialization.desc.FieldDesc;
import proto4j.serialization.desc.ObjectDesc;
import proto4j.serialization.mapping.Mapping;
import proto4j.serialization.mapping.PrimitiveMappings;

import java.io.IOException;
import java.sql.Ref;
import java.util.function.Function;

import static proto4j.DescProviderFactory.DEFAULT_DELIMITER;
import static proto4j.DescProviderFactory.RF;

public class TypeSpecField extends FieldDesc {

    private Serializer serializer;

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public String serialize() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append(DEFAULT_DELIMITER).append("[");
        sb.append(getModifiers()).append("]").append(DEFAULT_DELIMITER);

        String valueDesc;
        if (getSerializer() == null || getValue() == null)
            valueDesc = NULL_VALUE;
        else valueDesc = getSerializer().serialize(getValue());

        sb.append(getType().getName()).append("!").append(getSerializer().getClass().getName()).append(DEFAULT_DELIMITER);
        sb.append(valueDesc.length()).append(DEFAULT_DELIMITER).append(valueDesc);

        sb.append(DEFAULT_DELIMITER).append(RF);
        return sb.toString();
    }

    @Override
    public ObjectDesc read(String serialized) throws IOException {
        if (serialized == null || serialized.length() == 0) throw new IllegalArgumentException();

        String[] values = serialized.split("[-]");
        setOrdinal(Integer.parseInt(values[0]));
        setModifiers(Integer.parseInt(values[1].substring(1, values[1].length() - 1)));

        if (hasModifier(OPTIONAL_MODIFIER)) {
            if (values[4].equals("null")) {
                setValue(null);
                try {
                    setType(Class.forName(values[2]));
                } catch (ClassNotFoundException e) {
                    throw new IllegalCallerException("could not create type");
                }
                return this;
            }
        }
        String[] types = values[2].split("[!]");
        try {
            setType(Class.forName(types[0]));
            Class<?> s = Class.forName(types[1]);
            if (Serializer.class.isAssignableFrom(s)) {
                setValue(((Serializer)s.getDeclaredConstructor().newInstance()).read(values[4]));
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }
        return this;
    }
}
