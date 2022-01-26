package de.proto4j.annotation.documentation;//@date 25.01.2022

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
public @interface UnsafeOperation {
}
