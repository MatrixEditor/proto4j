package de.proto4j.security;//@date 08.02.2022

import java.lang.reflect.Method;

public interface Policy {

    boolean applies(Method m, Object caller);
}
