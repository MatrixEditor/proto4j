package de.proto4j.common.annotation;//@date 31.12.2021

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Query {
    Method makeQuery();

    Method makeQueryResponse();

    Method getFromQuery();
}
