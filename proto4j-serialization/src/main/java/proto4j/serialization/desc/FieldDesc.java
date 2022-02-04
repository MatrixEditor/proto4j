package proto4j.serialization.desc; //@date 31.01.2022

public abstract class FieldDesc implements ObjectDesc {

    public static final int OPTIONAL_MODIFIER  = 0b00000001;
    public static final int ANY_TYPE_MODIFIER  = 0b00000010;
    public static final int ONE_OF_MODIFIER    = 0b00000100;
    public static final int REPEATED_MODIFIER  = 0b00001000;
    public static final int TYPE_SPEC_MODIFIER = 0b00010000;

    public static final String NULL_VALUE = "null";

    private Class<?> type;

    private int ord;

    private int modifiers;

    private Object value;

    public boolean hasModifier(int m) {
        return (modifiers & m) != 0;
    }

    public void addModifier(int m) {
        if (!hasModifier(m)) {
            modifiers |= m;
        }
    }

    public int getModifiers() {
        return modifiers;
    }

    protected void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

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
