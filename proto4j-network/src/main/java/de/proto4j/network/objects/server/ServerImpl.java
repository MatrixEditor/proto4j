package de.proto4j.network.objects.server; //@date 28.01.2022

import de.proto4j.annotation.server.requests.selection.Selector;
import de.proto4j.internal.logger.LogMessage;
import de.proto4j.internal.logger.Logger;
import de.proto4j.internal.logger.PrintColor;
import de.proto4j.internal.logger.PrintService;
import de.proto4j.internal.method.MethodLookup;
import de.proto4j.network.objects.*;
import de.proto4j.security.asymmetric.Proto4jAsymKeyProvider;
import de.proto4j.security.cert.CertificateExchange;
import de.proto4j.security.cert.CertificateSpec;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

class ServerImpl {

    private static final Logger LOGGER = PrintService.createLogger(ObjectServer.class);

    private final List<ObjectContext<SelectorContext>> contextList;
    private final List<Class<?>>                       readableMessages;

    private final Object       connectionLock = new Object();
    private final ObjectServer wrapper;

    private final InetSocketAddress                    address;
    private final ServerSocketChannel                  ssChan;
    private final Map<SocketChannel, ObjectConnection> allConnections;

    private final Dispatcher dispatcher;


    private KeyPair         keyPair;
    private CertificateSpec certificateSpec;
    private ExecutorService threadPool;
    private Executor        executor;

    private volatile boolean finished    = false;
    private volatile boolean terminating = false;
    private          boolean bound       = false;
    private          boolean started     = false;

    private volatile long time;

    private Thread dispatcherThread;

    public ServerImpl(ObjectServer wrapper, InetSocketAddress address, int backlog) throws IOException {
        this.address = address;
        this.wrapper = wrapper;
        ssChan       = ServerSocketChannel.open();
        if (address != null) {
            ServerSocket socket = ssChan.socket();
            socket.bind(address, backlog);
            bound = true;

            LOGGER.info(PrintColor.LIGHT_GREY, LogMessage.of("server created on port: %s", address.getPort()));
        }

        allConnections   = Collections.synchronizedMap(new HashMap<>());
        time             = System.currentTimeMillis();
        contextList      = new LinkedList<>();
        dispatcher       = new Dispatcher();
        readableMessages = new LinkedList<>();
    }

    public void bind(InetSocketAddress address, int backlog) throws IOException {
        if (bound) {
            throw new IOException("Server already bound to an address!");
        }
        if (address == null) throw new NullPointerException("address is null");

        ServerSocket s = ssChan.socket();
        s.bind(address, backlog);
        bound = true;
    }

    public void start() {
        if (!bound || finished || started) throw new IllegalStateException("Wrong state for server!");
        if (executor == null && threadPool == null) throw new IllegalStateException("No executor defined!");

        try {
            keyPair         = Proto4jAsymKeyProvider.newProto4jKeyPair();
            certificateSpec = Proto4jAsymKeyProvider.getInstance(keyPair.getPublic());
        } catch (KeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("could not initialize keys");
        }

        dispatcherThread = new Thread(null, dispatcher, "ObjectServer::Dispatcher", 0, false);
        started          = true;
        dispatcherThread.start();

        LOGGER.info(PrintColor.DARK_GREEN, LogMessage.simpleMessage("Server started!"));
    }

    public void stop(int delay) {
        if (delay < 0) throw new IllegalArgumentException("negative delay parameter");
        terminating = true;
        LOGGER.info(PrintColor.LIGHT_GREY, LogMessage.simpleMessage("Terminating server"));

        try {
            ssChan.close();
        } catch (IOException e) {/**/}
        long latest = System.currentTimeMillis() + delay * 1000L;
        while (System.currentTimeMillis() < latest) {
            delay();
            if (finished) {
                break;
            }
        }
        finished = true;
        synchronized (connectionLock) {
            allConnections.forEach((s, c) -> c.close());
        }
        if (dispatcherThread != null) {
            try {
                dispatcherThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //log exception
            }
        }
        LOGGER.info(PrintColor.LIGHT_GREY, LogMessage.simpleMessage("Server stopped!"));
    }

    private void delay() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {/**/}
    }

    public synchronized ObjectContext<SelectorContext> createContext(Parameter[] parameters,
                                                                     ObjectContext.Handler handler) {
        if (parameters == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }

        ObjectContextImpl<SelectorContext> ctx = new ObjectContextImpl<>(SelectorContext.ofMethod(parameters),
                                                                         handler, wrapper);
        contextList.add(ctx);
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
            contextList.add(ctx);
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
        contextList.add(ctx);
        return ctx;
    }

    public synchronized void removeContext(Object mapping) {
        if (mapping != null) {
            for (ObjectContext<?> oc : contextList) {
                if (oc.equals(mapping)) {
                    contextList.remove(oc);
                    break;
                }
            }
        }
    }

    public synchronized void removeContext(ObjectContext<?> oc) {
        if (oc != null) {
            contextList.remove(oc);
        }
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public List<Class<?>> getReadableMessages() {
        return readableMessages;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public final boolean isFinished() {
        return finished;
    }

    private ObjectContext<?> findContext(Object message) {
        for (ObjectContext<SelectorContext> oc : contextList) {
            if (oc.getMapping().hasDefaultSelection()) {
                if (oc.getMapping().getParameters().length == 1) {
                    if (ObjectExchange.class.isAssignableFrom(oc.getMapping().getParameters()[0].getType()))
                        return oc;
                }
                if (MethodLookup.select(message, oc.getMapping().getParameters(), ObjectExchange.class)) {
                    return oc;
                }
            } else {
                if (oc.getMapping().getSelector().canSelect(message)) {
                    return oc;
                }
            }
        }
        return null;
    }

    public Map<SocketChannel, ObjectConnection> getAllConnections() {
        return allConnections;
    }

    class Dispatcher implements Runnable {

        @Override
        public void run() {
            SocketChannel chan = null;
            while (!finished) try {
                if (terminating) continue;

                chan = ssChan.accept();
                if (chan != null) {
                    chan.configureBlocking(true);
                    ObjectConnection oc = new ObjectConnection();
                    oc.setChannel(chan);
                    allConnections.put(chan, oc);

                    LOGGER.info(PrintColor.LIGHT_GREY,
                                LogMessage.of("new connection from (%s)", ((InetSocketAddress) chan.getRemoteAddress())
                                        .getAddress().getHostAddress()));
                    handle(chan, oc);
                }
            } catch (IOException ioe) {
                if (ioe instanceof AsynchronousCloseException) {
                    if (chan != null) allConnections.remove(chan);
                }
                LOGGER.except(PrintColor.DARK_RED, ioe);
            }
            stop(0);
        }

        private void handle(SocketChannel chan, ObjectConnection oc) {
            try {
                Worker e = new Worker(chan, oc);
                if (threadPool != null) {
                    threadPool.execute(e);
                } else {
                    if (executor != null) {
                        executor.execute(e);
                    }
                }
            } catch (Exception e) {
                LOGGER.except(PrintColor.DARK_RED, e);
            }
        }
    }

    class Worker implements Runnable {
        private final SocketChannel    chan;
        private final ObjectConnection connection;

        public Worker(SocketChannel chan, ObjectConnection oc) {
            this.chan  = chan;
            connection = oc;
        }

        @Override
        public void run() {
            CertificateExchange ce = new ServerCertificateExchange(chan, certificateSpec);
            try {
                ce.init();
                CertificateSpec cert = ce.exchange();
                if (cert == null) {
                    //do log
                    return;
                }
                connection.setRawInput(new ObjectReader(chan, getReadableMessages(), keyPair.getPrivate()));
                connection.setRawOutput(new ObjectWriter(chan, keyPair.getPrivate()));

            } catch (IOException | ClassNotFoundException e) {
                //  log
                return;
            }

            while (!finished) {
                if (terminating) continue;
                ObjectContext<?> context = connection.getContext();

                try {
                    Object message = ((ObjectReader) connection.getReader()).readMessage();
                    //find context
                    context = findContext(message);
                    if (context != null) {
                        connection.setContext(context);

                        ObjectExchange ex = new ObjectExchangeImpl(connection, message);
                        context.getHandler().handle(ex);
                    }
                } catch (Exception ex) {
                    if (ex instanceof SocketException) {
                        LogMessage lm = LogMessage.of("%s by %s", ex.getMessage(),
                                                      chan.socket().getLocalAddress().getHostName());
                        LOGGER.except(PrintColor.DARK_RED, lm);
                        break;
                    }
                    LOGGER.except(PrintColor.DARK_RED, ex);
                }
            }
            allConnections.remove(chan);
        }
    }
}
