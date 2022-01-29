package de.proto4j.network.objects;//@date 29.01.2022

import de.proto4j.annotation.documentation.Info;
import de.proto4j.annotation.documentation.UnsafeOperation;
import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.network.objects.provider.ObjectServer;

import java.util.Set;

public interface TypeServerContext {

    @UnsafeOperation
    ObjectServer getServer();

    @Info("UnmodifiableBeanManager")
    BeanManager getBeanManager();

    @Info("Collections.unmodifiableSet()")
    Set<Class<?>> loadedClasses();

    Class<?> mainClass();

}
