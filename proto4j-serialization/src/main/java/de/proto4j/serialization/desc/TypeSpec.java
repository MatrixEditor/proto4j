package de.proto4j.serialization.desc;//@date 29.01.2022

import de.proto4j.serialization.Serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface TypeSpec {
    Class<? extends Serializer> value();
}
