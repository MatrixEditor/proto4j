package de.proto4j.serialization.desc; //@date 05.02.2022

public abstract class Member {

    public static final int OPTIONAL_MODIFIER  = 0b00000001;
    public static final int ANY_TYPE_MODIFIER  = 0b00000010;
    public static final int ONE_OF_MODIFIER    = 0b00000100;
    public static final int REPEATED_MODIFIER  = 0b00001000;
    public static final int TYPE_SPEC_MODIFIER = 0b00010000;
    public static final int ALL_ARGS_MODIFIER  = 0b00100000;
    public static final int NO_ARGS_MODIFIER   = 0b01000000;

    public static final String NULL_VALUE = "null";

    private int modifiers;

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
}
