package de.proto4j.annotation.selection;//@date 23.01.2022

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Selection {
    /**
     * @return The validatorType has to have the same method parameters as the
     *         annotated one.
     */
    Class<? extends Selector> selectorType();

    String method() default "select";
}
