package de.proto4j.annotation.server.requests;//@date 23.01.2022

import de.proto4j.annotation.http.requests.ResponseType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseBody {
    ResponseType value() default ResponseType.PLAIN_RESPONSE;
}
