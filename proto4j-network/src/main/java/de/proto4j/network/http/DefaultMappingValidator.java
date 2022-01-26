package de.proto4j.network.http; //@date 25.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.Param;
import de.proto4j.annotation.validation.BaseValidator;

public class DefaultMappingValidator implements BaseValidator {

    public boolean ensureRightPath(HttpExchange exchange,
                                   @Param(name = "{$ARequestListener.path}") String path) {
        if (exchange != null && path != null) {
            String rPath = exchange.getRequestURI().getPath();
            if (path.length() == 0) {
                //...
            }
        }
        return false;
    }
}
