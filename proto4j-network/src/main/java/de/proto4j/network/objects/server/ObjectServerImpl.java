package de.proto4j.network.objects.server; //@date 28.01.2022

import de.proto4j.annotation.server.requests.selection.Selector;
import de.proto4j.network.objects.ObjectConnection;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.SelectorContext;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

class ObjectServerImpl extends ObjectServer {

    private final ServerImpl server;

    ObjectServerImpl(InetSocketAddress address, int backlog) throws IOException {
        this.server = new ServerImpl(this, address, backlog);
    }

    @Override
    public Executor getExecutor() {
        return server.getExecutor();
    }

    @Override
    public void setExecutor(Executor executor) {
        server.setExecutor(executor);
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
    public ObjectContext<SelectorContext> createContext(Class<? extends Selector> mapping,
                                                        ObjectContext.Handler handler) {
        return server.createContext(mapping, handler);
    }

    public ObjectContext<SelectorContext> createContext(Selector s, ObjectContext.Handler handler) {
        return server.createContext(s, handler);
    }

    @Override
    public ObjectContext<SelectorContext> createContext(Parameter[] parameters, ObjectContext.Handler handler) {
        return server.createContext(parameters, handler);
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

    @Override
    public List<Class<?>> getMessageTypes() {
        return server.getReadableMessages();
    }

    @Override
    public Collection<ObjectConnection> getAllConnections() {
        return Collections.unmodifiableCollection(server.getAllConnections().values());
    }
}
