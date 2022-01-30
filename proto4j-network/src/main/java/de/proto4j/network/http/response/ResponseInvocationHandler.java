package de.proto4j.network.http.response;//@date 26.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.requests.HttpResponseType;

public interface ResponseInvocationHandler<T> {

    public void handle(T t, HttpExchange exchange, HttpResponseType responseType);

}
