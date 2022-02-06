package de.prototest.messages.server; //@date 05.02.2022

import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.annotation.server.requests.RequestHandler;
import de.proto4j.annotation.server.requests.ResponseBody;
import de.proto4j.network.objects.ObjectExchange;

@Controller
public class ServerHandler {

    @RequestHandler
    @ResponseBody
    public Object handle(ObjectExchange exchange) {
        return exchange.getMessage();
    }
}
