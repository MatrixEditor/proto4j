package de.proto4j.annotation.server.requests;//@date 27.01.2022

import de.proto4j.annotation.selection.Selector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequestHandler {
    Class<? extends Selector> selectorType() default Selector.class;
}
