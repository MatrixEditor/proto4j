package de.proto4j.network.objects.client; //@date 29.01.2022

import de.proto4j.annotation.selection.Selector;
import de.proto4j.network.objects.ObjectConnection;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.SelectorContext;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class ObjectClientImpl extends ObjectClient {

    private final ClientImpl client;

    public ObjectClientImpl(List<String> conf) throws IOException {
        client = new ClientImpl(this, conf);
    }

    @Override
    public ObjectContext<SelectorContext> createContext(Parameter[] parameters, ObjectContext.Handler handler) {
        return client.createContext(parameters, handler);
    }

    @Override
    public ObjectContext<SelectorContext> createContext(Selector selector, ObjectContext.Handler handler) {
        return client.createContext(selector, handler);
    }

    @Override
    public ObjectContext<SelectorContext> createContext(Class<? extends Selector> mapping,
                                                        ObjectContext.Handler handler) {
        return client.createContext(mapping, handler);
    }

    @Override
    public void start() {
        client.start();
    }

    @Override
    public void stop(int delay) {
        client.stop(delay);
    }

    @Override
    public ExecutorService getThreadPool() {
        return client.getService();
    }

    @Override
    public void setThreadPool(ExecutorService e) {
        client.setService(e);
    }

    @Override
    public List<Class<?>> getMessageTypes() {
        return client.getMessageTypes();
    }

    @Override
    public void removeContext(Object mapping) {
        client.removeContext(mapping);
    }

    @Override
    public void removeContext(ObjectContext<?> ctx) {
        client.removeContext(ctx);
    }

    @Override
    public void connectTo(SocketAddress remote) {
        client.connectTo(remote);
    }

    @Override
    public List<String> getConfiguration() {
        return client.getConfiguration();
    }
}
