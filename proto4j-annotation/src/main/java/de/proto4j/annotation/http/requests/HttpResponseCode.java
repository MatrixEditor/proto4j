package de.proto4j.annotation.http.requests;//@date 23.01.2022

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(HttpResponseCode.ResponseCodes.class)
public @interface HttpResponseCode {
    int code();
    String description() default "";


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ResponseCodes {
        HttpResponseCode[] value() default {};
    }
}
