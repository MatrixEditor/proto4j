package de.prototest.echo.server; //@date 30.01.2022

import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.annotation.server.requests.RequestHandler;
import de.proto4j.annotation.server.requests.ResponseBody;
import de.prototest.echo.shared.EchoMessage;

@Controller
public class EchoHandler {

    @RequestHandler
    @ResponseBody
    public EchoMessage handle(EchoMessage msg) {
        msg.increment();
        return msg;
    }

}
