package de.proto4j.annotation.http.requests;//@date 23.01.2022

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ResponseCode.ResponseCodes.class)
public @interface ResponseCode {
    int code();
    String description() default "";


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ResponseCodes {
        ResponseCode[] value() default {};
    }
}
