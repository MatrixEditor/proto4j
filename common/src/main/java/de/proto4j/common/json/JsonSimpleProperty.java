package de.proto4j.common.json; //@date 13.11.2021

public class JsonSimpleProperty implements JsonProperty {

    private String key;
    private Object value;

    public JsonSimpleProperty(String key, Object value) {
        this.key   = key;
        this.value = value;
    }

    @Override
    public String getTag() {
        return key;
    }

    @Override
    public void modifyTag(String tag) {
        this.key = tag;
    }

    public Object value() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isSimpleProperty() {
        return true;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    public boolean getAsBoolean() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public Number getAsNumber() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public String getAsString() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public double getAsDouble() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public float getAsFloat() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public long getAsLong() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public int getAsInt() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public byte getAsByte() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public char getAsCharacter() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    public short getAsShort() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    @Override
    public JsonSimpleProperty asProperty() {
        return this;
    }

    @Override
    public String toJson() {
        return null;
    }
}
