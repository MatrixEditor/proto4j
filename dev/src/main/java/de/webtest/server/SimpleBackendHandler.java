package de.webtest.server; //@date 30.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.requests.HttpRequestController;
import de.proto4j.annotation.http.requests.HttpRequestListener;
import de.proto4j.annotation.http.requests.HttpResponseBody;

@HttpRequestController(mapping = "/java")
public class SimpleBackendHandler {

    @HttpRequestListener(path = "hello")
    @HttpResponseBody
    public String handle(HttpExchange exchange) {
        return "Hello World";
    }
}
