package de.proto4j.common.io; //@date 29.12.2021

import de.proto4j.common.ProtocolFactory;
import de.proto4j.common.annotation.AnnotatedObject;
import de.proto4j.common.annotation.IServerSocket;
import de.proto4j.common.annotation.ISocket;
import de.proto4j.common.annotation.Item;

import java.io.IOException;
import java.net.ServerSocket;

@IServerSocket(accept = @ISocket.SocketMethod(name = "accept", returnClass = ObjectSocket.class))
public class ObjectServerSocket implements ProtocolFactory.FactoryServerSocket<ObjectSocket>, AnnotatedObject {

    @Item(name = "serverSocket.low", hasSetter = false)
    private final ServerSocket serverSocket;

    @Item(name = "alive")
    private boolean alive = true;

    @Item(name = "client.handler")
    private Class<?> clientHandler;

    @Item(name = "network.handler")
    private ProtocolFactory.FactoryNetworkHandler networkHandler;

    public ObjectServerSocket(Integer port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public ObjectSocket accept() throws Exception {
        if (!serverSocket.isClosed())
            return new ObjectSocket(serverSocket.accept());
        throw new IOException("ServerSocket is closed!");
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setHandler(Class<?> handler) {
        if (handler != null) this.clientHandler = handler;
    }
}
