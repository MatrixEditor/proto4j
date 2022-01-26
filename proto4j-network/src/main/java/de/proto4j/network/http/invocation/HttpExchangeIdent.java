package de.proto4j.network.http.invocation; //@date 25.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.documentation.UnsafeOperation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

public enum HttpExchangeIdent {

    REQUEST_INPUT(HttpExchangeReference.BODY, e -> {
        try (BufferedInputStream bis = new BufferedInputStream(e.getRequestBody())) {
            return new String(bis.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) { /**/ }
        return "";
    }, String.class),

    @UnsafeOperation
    REQUEST_HEADER_USER_AGENT(HttpExchangeReference.HEADER_USER_AGENT,
                              e -> e.getRequestHeaders().get("User-agent"), List.class);

    private final String   name;
    private final Class<?> returnType;

    private final Function<HttpExchange, Object> mapper;

    HttpExchangeIdent(String name, Function<HttpExchange, Object> mapper, Class<?> returnType) {
        this.name       = name;
        this.returnType = returnType;
        this.mapper     = mapper;
    }

    public Object apply(HttpExchange e) {
        return mapper.apply(e);
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }
}
