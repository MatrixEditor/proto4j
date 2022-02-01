package de.proto4j.internal.io.desc; //@date 31.01.2022

import de.proto4j.internal.io.desc.mapping.Mapping;
import de.proto4j.internal.io.desc.mapping.PrimitiveMappings;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static de.proto4j.internal.io.desc.DescProviderFactory.DEFAULT_DELIMITER;
import static de.proto4j.internal.io.desc.DescProviderFactory.RF;

public class PrimitiveFieldDesc extends FieldDesc {

    public PrimitiveFieldDesc() {
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
                try {
                    setType(Class.forName(values[2]));
                } catch (ClassNotFoundException e) {
                    throw new IllegalCallerException("could not create type");
                }
                return this;
            }
        }

        Mapping<Function<String, ?>> m = PrimitiveMappings.valueOf(values[2]);
        setType(m.getType());
        setValue(m.getInvoker().apply(values[4]));
        return this;
    }
}
