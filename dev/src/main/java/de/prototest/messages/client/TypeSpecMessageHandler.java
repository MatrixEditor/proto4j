package de.prototest.messages.client; //@date 05.02.2022

import de.proto4j.annotation.server.requests.ConnectionHandler;
import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.network.objects.ObjectExchange;
import de.prototest.messages.shared.ExampleObject;
import de.prototest.messages.shared.TypeSpecMessage;

import java.io.IOException;

@Controller("127.0.0.1")
public class TypeSpecMessageHandler {

    @ConnectionHandler
    public void handle(ObjectExchange e) throws IOException {
        Proto4jReader r = (Proto4jReader) e.getRequestBody();
        Proto4jWriter w = (Proto4jWriter) e.getResponseBody();

        TypeSpecMessage m = new TypeSpecMessage();
        m.setObj(new ExampleObject());
        m.getObj().setSomeMessage("Hello World");

        w.write(m);
        TypeSpecMessage m1 = (TypeSpecMessage) r.readMessage();
        assert m1 != null;
    }
}
