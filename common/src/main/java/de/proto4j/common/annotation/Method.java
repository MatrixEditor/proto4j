package de.proto4j.common.annotation;//@date 31.12.2021

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Method {
    String target();
    Class<?> returnType() default void.class;
    Class<?>[] parameterTypes() default {};
}
