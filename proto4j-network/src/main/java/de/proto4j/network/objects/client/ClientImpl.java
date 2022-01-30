package de.proto4j.network.objects.client; //@date 29.01.2022

import de.proto4j.annotation.selection.Selector;
import de.proto4j.annotation.server.Configuration;
import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.network.objects.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class ClientImpl {

    private final Map<SocketChannel, ObjectConnection> connections = new HashMap<>();

    private final List<Class<?>>                          messageTypes = new LinkedList<>();
    private final List<ObjectContext<? extends Selector>> contexts     = new LinkedList<>();

    private final ObjectClient wrapper;
    private final List<String> configuration;

    private volatile boolean finished    = false;
    private volatile boolean terminating = false;

    private ExecutorService service;
    private Thread          dispatcherThread;


    public ClientImpl(ObjectClient client, List<String> conf) throws IOException {
        configuration = conf;
        wrapper       = client;
    }

    public void connectTo(SocketAddress remote) {
        if (remote == null) throw new IllegalArgumentException("address has to be not null");

        try {
            SocketChannel channel = SocketChannel.open(remote);
            if (channel != null) {
                channel.configureBlocking(false);

                Thread t = new Thread(new Dispatcher(channel));
                t.start();
            }
        } catch (IOException e) {
            throw new IllegalCallerException("could not connect to specified address");
        }
    }

    public void start() {
        if (finished || terminating) throw new IllegalStateException(
                "Client is in the wrong state to start");

        if (service == null) throw new NullPointerException("service not defined");

        if (messageTypes.isEmpty()) throw new IllegalStateException("no message types defined");
    }

    public void stop(int delay) {
        if (delay < 0) throw new IllegalArgumentException("negative delay parameter");
        terminating = true;
        connections.forEach((c, o) -> {
            try {
                if (c.isConnected()) {
                    c.close();
                }
            } catch (IOException e) {/**/}
        });

        long latest = System.currentTimeMillis() + delay * 1000L;
        while (System.currentTimeMillis() < latest) {
            delay();
            if (finished) {
                break;
            }
        }
        finished = true;
        if (dispatcherThread != null) {
            try {
                dispatcherThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //log
            }
        }
        if (service != null) {
            if (!service.isTerminated()) {
                service.shutdownNow();
            }
        }
    }

    public ObjectContext<? extends Selector> createContext(Selector s, ObjectContext.Handler handler) {
        if (s == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }
        ObjectContextImpl<? extends Selector> ctx = new ObjectContextImpl<>(s, handler, wrapper);
        contexts.add(ctx);
        return ctx;
    }

    public synchronized ObjectContext<? extends Selector> createContext(Class<? extends Selector> o,
                                                                        ObjectContext.Handler handler) {
        if (o == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }
        try {
            Selector mapping = o.getDeclaredConstructor().newInstance();

            ObjectContextImpl<? extends Selector> ctx = new ObjectContextImpl<>(mapping, handler, wrapper);
            contexts.add(ctx);
            return ctx;
        } catch (ReflectiveOperationException e) {
            // log handler not added
        }
        return null;
    }

    public synchronized void removeContext(Object mapping) {
        if (mapping != null) {
            for (ObjectContext<?> oc : contexts) {
                if (oc.equals(mapping)) {
                    contexts.remove(oc);
                    break;
                }
            }
        }
    }

    public synchronized void removeContext(ObjectContext<?> oc) {
        if (oc != null) {
            contexts.remove(oc);
        }
    }

    private void delay() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {/**/}
    }

    private ObjectContext<? extends Selector> findBySelector(Object message) {
        for (ObjectContext<? extends Selector> oc : contexts) {
            if (oc != null) {
                if (oc.getMapping().canSelect(message)) {
                    return oc;
                }
            }
        }
        return null;
    }

    public ExecutorService getService() {
        return service;
    }

    public void setService(ExecutorService service) {
        this.service = service;
    }

    public List<Class<?>> getMessageTypes() {
        return messageTypes;
    }

    public List<String> getConfiguration() {
        return configuration;
    }

    public Map<SocketChannel, ObjectConnection> getConnections() {
        return connections;
    }

    private final class Dispatcher implements Runnable {

        private final Proto4jReader rin;
        private final Proto4jWriter rout;
        private final SocketChannel channel;

        public Dispatcher(SocketChannel channel) {
            rin          = new Proto4jReader(channel, getMessageTypes());
            rout         = new Proto4jWriter(channel);
            this.channel = channel;
        }

        @Override
        public void run() {
            if (configuration.contains(Configuration.BY_CONNECTION)) {
                if (!finished && channel.isConnected()) {
                    SocketAddress remote;
                    try {
                        remote = channel.getRemoteAddress();
                    } catch (IOException e) {
                        remote = channel.socket().getRemoteSocketAddress();
                    }
                    ObjectContext<?> oc = findBySelector(remote);
                    if (oc != null) {
                        ObjectConnection conn = new ObjectConnection();
                        conn.setParameters(rout, channel, rin, oc);

                        connections.put(channel, conn);

                        ObjectExchange ex = new ObjectExchangeImpl(conn, null);
                        new InternalInvokerHelper(ex, channel).run();
                    }
                }
            } else {
                while (!finished && channel.isConnected()) {
                    if (terminating) continue;


                    Object message = null;
                    if (channel.isConnected()) try {
                        message = rin.readMessage();
                    } catch (IOException s) {
                        if (s instanceof SocketException) {
                            // stop this client by breaking this loop
                            break;
                        }
                    }

                    if (message != null) {
                        ObjectContext<?> oc = findBySelector(message);
                        if (oc != null) {
                            ObjectConnection conn = new ObjectConnection();
                            conn.setParameters(rout, channel, rin, oc);

                            ObjectExchange ex = new ObjectExchangeImpl(conn, message);
                            service.execute(new InternalInvokerHelper(ex, channel));
                        }
                    }
                }
                stop(0);
            }
        }
    }

    private class InternalInvokerHelper implements Runnable {

        private final ObjectExchange exchange;
        private final SocketChannel  channel;

        private InternalInvokerHelper(ObjectExchange exchange, SocketChannel channel) {
            this.exchange = exchange;
            this.channel  = channel;
        }

        @Override
        public void run() {
            if (!finished && channel.isConnected()) {
                if (terminating || exchange == null) return;

                exchange.getContext().getHandler().handle(exchange);
            }
        }
    }
}
