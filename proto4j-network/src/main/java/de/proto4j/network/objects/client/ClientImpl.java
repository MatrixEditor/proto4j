package de.proto4j.network.objects.client; //@date 29.01.2022

import de.proto4j.annotation.AnnotationLookup;
import de.proto4j.annotation.server.requests.selection.Selector;
import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.internal.method.MethodLookup;
import de.proto4j.network.objects.*;
import de.proto4j.security.cert.CertificateExchange;
import de.proto4j.security.cert.CertificateSpec;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class ClientImpl {

    private final Map<SocketChannel, ObjectConnection> connections = new Hashtable<>();
    private final List<ObjectContext<SelectorContext>> contexts    = new LinkedList<>();

    private final List<Class<?>> messageTypes = new LinkedList<>();
    private final List<String>   configuration;

    private final ObjectClient wrapper;

    private volatile boolean finished    = false;
    private volatile boolean terminating = false;

    private ExecutorService service;
    private CertificateSpec certificateSpec;

    public ClientImpl(ObjectClient client, List<String> conf) throws IOException {
        configuration = conf;
        wrapper       = client;
    }

    public void connectTo(SocketAddress remote) {
        if (remote == null) throw new IllegalArgumentException("address has to be not null");

        try {
            SocketChannel channel = SocketChannel.open(remote);
            if (channel != null) {
                channel.configureBlocking(true);

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
        if (service != null) {
            if (!service.isTerminated()) {
                service.shutdownNow();
            }
        }
    }

    public synchronized ObjectContext<SelectorContext> createContext(Parameter[] parameters,
                                                                     ObjectContext.Handler handler) {
        if (parameters == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }

        ObjectContextImpl<SelectorContext> ctx = new ObjectContextImpl<>(SelectorContext.ofMethod(parameters),
                                                                         handler, wrapper);
        contexts.add(ctx);
        return ctx;
    }

    public synchronized ObjectContext<SelectorContext> createContext(Class<? extends Selector> o,
                                                                     ObjectContext.Handler handler) {
        if (o == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }
        try {
            Selector mapping = o.getDeclaredConstructor().newInstance();

            ObjectContextImpl<SelectorContext> ctx = new ObjectContextImpl<>(SelectorContext.ofSelector(mapping),
                                                                             handler, wrapper);
            contexts.add(ctx);
            return ctx;
        } catch (ReflectiveOperationException e) {
            // log handler not added
        }
        return null;
    }

    public synchronized ObjectContext<SelectorContext> createContext(Selector mapping, ObjectContext.Handler handler) {
        if (mapping == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }
        ObjectContextImpl<SelectorContext> ctx = new ObjectContextImpl<>(SelectorContext.ofSelector(mapping),
                                                                         handler, wrapper);
        contexts.add(ctx);
        return ctx;
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

    private ObjectContext<SelectorContext> findBySelector(Object message) {
        for (ObjectContext<SelectorContext> oc : contexts) {
            if (oc != null) {
                if (oc.getMapping().hasDefaultSelection()) {
                    if (MethodLookup.select(message, oc.getMapping().getParameters(), ObjectExchange.class)) {
                        return oc;
                    }
                } else {
                    if (oc.getMapping().getSelector().canSelect(message)) {
                        return oc;
                    }
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

        private final SocketChannel    channel;
        private final ObjectConnection conn;
        private       Proto4jReader    rin;
        private       Proto4jWriter    rout;

        public Dispatcher(SocketChannel channel) {
            this.channel = channel;

            conn = new ObjectConnection();
        }

        @Override
        public void run() {
            CertificateExchange ce = new ClientCertificateExchange(channel);
            try {
                ce.init();
                CertificateSpec cert = ce.exchange();
                if (cert == null) {
                    //do log
                    return;
                }
                certificateSpec = cert;
                rin             = new ObjectReader(channel, getMessageTypes(), certificateSpec.getPublicKey());
                rout            = new ObjectWriter(channel, certificateSpec.getPublicKey());

                conn.setParameters(rout, channel, rin, null);
                connections.put(channel, conn);
            } catch (IOException | ClassNotFoundException e) {
                //  log
                return;
            }


            if (configuration.contains(AnnotationLookup.CONF_BY_CONNECTION)) {
                if (!finished && channel.isConnected()) {
                    SocketAddress remote;
                    try {
                        remote = channel.getRemoteAddress();
                    } catch (IOException e) {
                        remote = channel.socket().getRemoteSocketAddress();
                    }
                    ObjectContext<?> oc = findBySelector(remote);
                    if (oc != null) {
                        conn.setContext(oc);
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
                            conn.setContext(oc);

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
