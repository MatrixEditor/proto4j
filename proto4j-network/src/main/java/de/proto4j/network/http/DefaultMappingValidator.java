package de.proto4j.network.http; //@date 25.01.2022

import com.sun.net.httpserver.HttpExchange;
import de.proto4j.annotation.http.Param;
import de.proto4j.annotation.selection.Selector;

public class DefaultMappingValidator implements Selector {

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
