package de.proto4j.annotation.server;//@date 27.01.2022

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Context {
    String[] value();
}
