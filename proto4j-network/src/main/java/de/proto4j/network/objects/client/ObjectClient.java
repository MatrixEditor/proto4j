package de.proto4j.network.objects.client; //@date 29.01.2022

import de.proto4j.annotation.selection.Selector;
import de.proto4j.network.objects.ObjectConnection;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.SelectorContext;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public abstract class ObjectClient {

    public static ObjectClient create() throws IOException {
        return create(new LinkedList<>());
    }

    static ObjectClient create(List<String> conf) throws IOException {
        return new ObjectClientImpl(conf);
    }

    public abstract ObjectContext<SelectorContext> createContext(Parameter[] parameters, ObjectContext.Handler handler);

    public abstract ObjectContext<SelectorContext> createContext(Selector selector, ObjectContext.Handler handler);

    public abstract ObjectContext<SelectorContext> createContext(Class<? extends Selector> mapping,
                                                                    ObjectContext.Handler handler);

    public abstract void start();

    public abstract void stop(int delay);

    public abstract ExecutorService getThreadPool();

    public abstract void setThreadPool(ExecutorService e);

    public abstract List<Class<?>> getMessageTypes();

    public abstract void removeContext(Object mapping);

    public abstract void removeContext(ObjectContext<?> ctx);

    public abstract void connectTo(SocketAddress remote);

    protected abstract List<String> getConfiguration();
}
