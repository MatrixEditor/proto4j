package de.yz.gen; //@date 27.01.2022

import de.proto4j.annotation.server.TypeServer;
import de.proto4j.annotation.threding.ThreadPooling;
import de.proto4j.internal.AllowAutoConfiguration;
import de.proto4j.network.objects.ServerProvider;
import de.proto4j.network.objects.TypeServerContext;

import java.io.IOException;

@TypeServer(port = 9999)
@AllowAutoConfiguration
@ThreadPooling
public class HelloWorldEchoServer {

    public static void main(String[] args) throws IOException {
        TypeServerContext ctx = ServerProvider.runServer(HelloWorldEchoServer.class);
    }
}
