package de.prototest.messages.client; //@date 05.02.2022

import de.proto4j.annotation.server.requests.RequestHandler;
import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.network.objects.ObjectExchange;
import de.prototest.messages.shared.ArrayMessage;

import java.io.IOException;

//@Controller("127.0.0.1")
public class ArrayMessageHandler {

    @RequestHandler
    public void handle(ObjectExchange exchange) throws IOException {
        Proto4jReader in  = exchange.getRequestBody();
        Proto4jWriter out = exchange.getResponseBody();

        ArrayMessage am = new ArrayMessage();
        am.array()[0] = 100;
        am.list().add("hello");
        am.list().add("world");

        out.write(am);

        ArrayMessage back = (ArrayMessage) in.readMessage();
        assert back != null;
    }
}
