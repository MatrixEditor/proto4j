package de.proto4j.annotation.server;//@date 27.01.2022

import de.proto4j.annotation.documentation.Range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeServer {

    @Range(from = 1, to = 65535)
    int port();
}
