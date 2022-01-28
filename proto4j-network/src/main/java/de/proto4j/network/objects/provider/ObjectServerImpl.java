package de.proto4j.network.objects.provider; //@date 28.01.2022

import de.proto4j.network.objects.ObjectContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

class ObjectServerImpl extends ObjectServer {

    private final ServerImpl server;

    ObjectServerImpl(InetSocketAddress address, int backlog) throws IOException {
        this.server = new ServerImpl(this, address, backlog);
    }

    @Override
    public void setExecutor(Executor executor) {
        server.setExecutor(executor);
    }

    @Override
    public Executor getExecutor() {
        return server.getExecutor();
    }

    @Override
    public ExecutorService getThreadPool() {
        return server.getThreadPool();
    }

    @Override
    public void setThreadPool(ExecutorService threadPool) {
        server.setThreadPool(threadPool);
    }

    @Override
    public void bindTo(InetSocketAddress address, int backlog) throws IOException {
        server.bind(address, backlog);
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop(int delay) {
        server.stop(delay);
    }

    @Override
    public ObjectContext<?> createContext(Object mapping, ObjectContext.Handler handler) {
        return server.createContext(mapping, handler);
    }

    @Override
    public ObjectContext<?> createContext(Object mapping) {
        return server.createContext(mapping);
    }

    @Override
    public void removeContext(Object mapping) {
        server.removeContext(mapping);
    }

    @Override
    public void removeContext(ObjectContext<?> ctx) {
        server.removeContext(ctx);
    }

    @Override
    public InetSocketAddress getAddress() {
        return server.getAddress();
    }
}
