package de.proto4j.network.objects; //@date 29.01.2022

import de.proto4j.annotation.selection.Selector;

import java.lang.reflect.Method;

public final class ContextCache {
    private final Method                    m;
    private final Object                    invoker;
    private final Class<? extends Selector> selectorType;
    private final Class<?>                  controllerClass;

    public ContextCache(Method m, Object invoker, Class<? extends Selector> selectorType,
                        Class<?> controllerClass) {
        this.m               = m;
        this.invoker         = invoker;
        this.selectorType    = selectorType;
        this.controllerClass = controllerClass;
    }

    public Method getMethod() {
        return m;
    }

    public Object getInvoker() {
        return invoker;
    }

    public Class<? extends Selector> getSelectorType() {
        return selectorType;
    }

    public Class<?> getControllerClass() {
            return controllerClass;
        }
}
