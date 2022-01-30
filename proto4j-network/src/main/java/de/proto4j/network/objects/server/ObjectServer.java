package de.proto4j.network.objects.server; //@date 28.01.2022

import de.proto4j.network.objects.client.ObjectClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class ObjectServer extends ObjectClient {

    protected ObjectServer() {}

    public static ObjectServer create(InetSocketAddress address, int backlog) throws IOException {
        return new ObjectServerImpl(address, backlog);
    }

    public abstract Executor getExecutor();

    public abstract void setExecutor(Executor executor);

    public abstract void bindTo(InetSocketAddress address, int backlog) throws IOException;

    public void bindTo(InetSocketAddress address) throws IOException {
        bindTo(address, 0);
    }

    public abstract InetSocketAddress getAddress();

    @Override
    protected List<String> getConfiguration() {
        return null;
    }

    @Override
    public void connectTo(SocketAddress remote) {}

}
