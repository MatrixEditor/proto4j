package de.proto4j.annotation.server.threding;//@date 27.01.2022

import de.proto4j.annotation.documentation.Info;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Executor;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SupplyParallel {

    @Info(text = "Executor.class indicates that Executors.newSingleThreadExecutor() is used")
    Class<? extends Executor> value() default Executor.class;
}
