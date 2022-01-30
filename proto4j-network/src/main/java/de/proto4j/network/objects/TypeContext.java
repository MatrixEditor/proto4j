package de.proto4j.network.objects;//@date 29.01.2022

import de.proto4j.annotation.documentation.Info;
import de.proto4j.internal.model.bean.BeanManager;

import java.util.Set;

public interface TypeContext {

    @Info("UnmodifiableBeanManager")
    BeanManager getBeanManager();

    @Info("Collections.unmodifiableSet()")
    Set<Class<?>> loadedClasses();

    Class<?> mainClass();

}
