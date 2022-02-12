package de.proto4j.serialization.desc; //@date 04.02.2022

import de.proto4j.serialization.Serializer;

import java.io.IOException;

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

        sb.append(getName()).append(DescProviderFactory.DEFAULT_DELIMITER).append("[");
        sb.append(getModifiers()).append("]").append(DescProviderFactory.DEFAULT_DELIMITER);

        if (Serializer.class.isAssignableFrom(getType())) {
            try {
                setSerializer((Serializer) getType().getDeclaredConstructor().newInstance());
            } catch (ReflectiveOperationException e) {
                throw new IllegalCallerException("could not initialize serializer");
            }
        }
        String valueDesc;
        if (getSerializer() == null || getValue() == null)
            valueDesc = NULL_VALUE;
        else valueDesc = getSerializer().serialize(getValue());

        sb.append(getValue().getClass().getName()).append("!").append(getSerializer().getClass().getName()).append(
                DescProviderFactory.DEFAULT_DELIMITER);
        sb.append(valueDesc.length()).append(DescProviderFactory.DEFAULT_DELIMITER).append(valueDesc);

        sb.append(DescProviderFactory.DEFAULT_DELIMITER).append(DescProviderFactory.RF);
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
                setSerializer((Serializer) s.getDeclaredConstructor().newInstance());
                setValue(getSerializer().read(values[4]));
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }
        return this;
    }
}
