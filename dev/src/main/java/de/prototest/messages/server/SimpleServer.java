package de.prototest.messages.server; //@date 05.02.2022

import de.proto4j.annotation.server.TypeServer;
import de.proto4j.internal.AllowAutoConfiguration;
import de.proto4j.network.objects.server.ServerProvider;

import java.io.IOException;

@TypeServer(port = 3333)
@AllowAutoConfiguration
public class SimpleServer {
    public static void main(String[] args) throws IOException {
        ServerProvider.runServer(SimpleServer.class);
    }
}
