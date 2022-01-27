package de.proto4j.annotation.documentation;//@date 27.01.2022

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface Info {
    String text();
}
