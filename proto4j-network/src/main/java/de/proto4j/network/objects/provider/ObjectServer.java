package de.proto4j.network.objects.provider; //@date 28.01.2022

import de.proto4j.network.objects.ObjectContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public abstract class ObjectServer {

    protected ObjectServer() {}

    public static ObjectServer create(InetSocketAddress address, int backlog) throws IOException {
        return new ObjectServerImpl(address, backlog);
    }

    public abstract void setExecutor(Executor executor);

    public abstract Executor getExecutor();

    public abstract void setThreadPool(ExecutorService threadPool);

    public abstract ExecutorService getThreadPool();

    public abstract void bindTo(InetSocketAddress address, int backlog) throws IOException;

    public void bindTo(InetSocketAddress address) throws IOException {
        bindTo(address, 0);
    }

    public abstract void start();

    public abstract void stop(int delay);

    public abstract ObjectContext<?> createContext(Object mapping, ObjectContext.Handler handler);

    public abstract ObjectContext<?> createContext(Object mapping);

    public abstract void removeContext(Object mapping);

    public abstract void removeContext(ObjectContext<?> ctx);

    public abstract InetSocketAddress getAddress();
}
