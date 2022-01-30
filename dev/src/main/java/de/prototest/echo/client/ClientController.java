package de.prototest.echo.client; //@date 30.01.2022

import de.proto4j.annotation.server.requests.ConnectionHandler;
import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.annotation.threding.Parallel;
import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.network.objects.ObjectExchange;
import de.prototest.echo.shared.EchoMessage;

import java.io.IOException;

@Controller("127.0.0.1")
public class ClientController {

    @ConnectionHandler
    @Parallel
    public void onConnectionEstablished(ObjectExchange exchange) throws IOException {
        Proto4jReader in  = (Proto4jReader) exchange.getRequestBody();
        Proto4jWriter  out = (Proto4jWriter) exchange.getResponseBody();

        // counter is at 0
        EchoMessage msg = new EchoMessage();
        msg.increment(); // now at 1

        out.write(msg);
        Object response = in.readMessage();
        if (response instanceof EchoMessage) {
            EchoMessage echo = (EchoMessage) response;
            // counter should be at 2 (see server-handler)
            int c = echo.get();
            System.out.printf("Start: %s -> End: %s", msg.get(), c);
        }
    }
}
