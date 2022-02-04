package de.proto4j.internal.method; //@date 02.02.2022

import java.beans.MethodDescriptor;
import java.lang.reflect.Parameter;

public class ContextSelector {

    public boolean shouldSelect(Object o, MethodDescriptor descriptor) {
        if (o == null || descriptor == null) return false;

        Parameter[] parameters = descriptor.getMethod().getParameters();
        if (parameters.length == 0) return false;

        if (parameters.length == 1) {
            if (parameters[0].getType().isAssignableFrom(o.getClass())) {
                return true;
            }
        }
        return false;
    }
}
