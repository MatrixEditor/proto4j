package de.proto4j.annotation.server.threding;//@date 27.01.2022

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ThreadPooling {
    Class<? extends ExecutorService> poolType() default ForkJoinPool.class;

    // -1 stands for default constructor with no parameters
    int parallelism() default -1;
}
