package de.yz.gen; //@date 27.01.2022

import de.proto4j.annotation.server.IOReader;
import de.proto4j.annotation.server.IOWriter;
import de.proto4j.annotation.server.TypeServer;
import de.proto4j.annotation.server.threding.ThreadPooling;
import de.proto4j.internal.AllowAutoConfiguration;
import de.proto4j.internal.RootPackage;
import de.proto4j.network.objects.provider.ObjectServer;

import java.io.IOException;
import java.net.InetSocketAddress;

@TypeServer(port = 9999)
@RootPackage
@AllowAutoConfiguration
@ThreadPooling
@IOWriter
@IOReader
public class GenericJavaServer {

    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        ObjectServer os = ObjectServer.create(new InetSocketAddress(2343), 0);

        os.setExecutor(Runnable::run);
        os.createContext(new Object(), e -> {
        });
    }
}
