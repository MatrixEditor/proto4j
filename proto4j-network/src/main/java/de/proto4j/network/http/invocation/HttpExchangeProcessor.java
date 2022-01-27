package de.proto4j.network.http.invocation; //@date 25.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.requests.RequestParam;

import java.lang.reflect.Parameter;

public class HttpExchangeProcessor implements ParameterProcessor<HttpExchange, Object> {

    public static final String HEADER_USER_AGENT = "header.user-agent";
    public static final String BODY              = "body";

    @Override
    public Object process(Parameter p0, HttpExchange exchange) {
        if (p0 != null && exchange != null) {
            if (p0.isAnnotationPresent(RequestParam.class)) {
                String name = p0.getDeclaredAnnotation(RequestParam.class).name();

                for (HttpExchangeRequestConstant n : HttpExchangeRequestConstant.values()) {
                    if (n.getName().equals(name)) {
                        Object o = n.apply(exchange);
                        if (o.getClass().isAssignableFrom(p0.getType())) {
                            return o;
                        }
                    }
                }
            }
        }
        return null;
    }

}
