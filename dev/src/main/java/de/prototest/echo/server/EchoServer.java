package de.prototest.echo.server; //@date 30.01.2022

import de.proto4j.annotation.server.TypeServer;
import de.proto4j.annotation.threding.ThreadPooling;
import de.proto4j.internal.AllowAutoConfiguration;
import de.proto4j.internal.RootPackage;
import de.proto4j.network.objects.TypeContext;
import de.proto4j.network.objects.server.ServerProvider;

import java.io.IOException;

@TypeServer(port = 3333)
@AllowAutoConfiguration
@RootPackage
@ThreadPooling
public class EchoServer {

    public static void main(String[] args) throws IOException {
        TypeContext ctx = ServerProvider.runServer(EchoServer.class);
    }

}
