package de.proto4j.network.http.invocation;//@date 25.01.2022

import java.lang.reflect.Parameter;

public interface InvocationReference<T, R> {

    R process(Parameter p0, T t);
}
