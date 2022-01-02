package de.proto4j.common.annotation.event;//@date 31.12.2021

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EventListener {
    String method();
    Class<?> eventType();
}
