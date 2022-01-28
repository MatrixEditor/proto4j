package de.proto4j.annotation.server;//@date 28.01.2022

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IOReader {
    Class<? extends InputStream> value() default ObjectInputStream.class;
}
