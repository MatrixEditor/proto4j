package de.proto4j.annotation.server;//@date 28.01.2022

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IOWriter {
    Class<? extends OutputStream> value() default ObjectOutputStream.class;
}
