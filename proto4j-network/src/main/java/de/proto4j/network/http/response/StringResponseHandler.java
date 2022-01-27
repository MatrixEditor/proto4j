package de.proto4j.network.http.response; //@date 26.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.requests.ResponseType;

import java.io.IOException;
import java.io.OutputStream;

public class StringResponseHandler implements ResponseInvocationHandler<String> {

    @Override
    public void handle(String s, HttpExchange exchange, ResponseType responseType) {
        if (s != null && s.length() != 0 && exchange != null) {
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.sendResponseHeaders(200, s.length());
                os.write(s.getBytes());
            } catch (IOException e) {
                // log error to logger
            }
        }
    }
}
