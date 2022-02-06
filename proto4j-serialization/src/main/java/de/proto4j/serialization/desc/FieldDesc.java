package de.proto4j.serialization.desc; //@date 31.01.2022

public abstract class FieldDesc extends Member implements ObjectDesc {

    private Class<?> type;

    private int ord;

    private Object value;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public int getOrdinal() {
        return ord;
    }

    public void setOrdinal(int ord) {
        this.ord = ord;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return String.valueOf(getOrdinal());
    }
}
