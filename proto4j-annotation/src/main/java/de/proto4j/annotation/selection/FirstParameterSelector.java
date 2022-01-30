package de.proto4j.annotation.selection; //@date 29.01.2022

public class FirstParameterSelector implements Selector {

    private final Class<?> parameterClass;

    public FirstParameterSelector(Class<?> parameterClass) {this.parameterClass = parameterClass;}

    @Override
    public boolean canSelect(final Object o) {
        if (o == null) return false;
        return parameterClass.isAssignableFrom(o.getClass());
    }
}
