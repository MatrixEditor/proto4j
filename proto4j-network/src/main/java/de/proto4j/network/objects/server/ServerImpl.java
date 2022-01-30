package de.proto4j.network.objects.server; //@date 28.01.2022

import de.proto4j.annotation.selection.Selector;
import de.proto4j.annotation.selection.Selectors;
import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.internal.logger.LogMessage;
import de.proto4j.internal.logger.Logger;
import de.proto4j.internal.logger.PrintColor;
import de.proto4j.internal.logger.PrintService;
import de.proto4j.network.objects.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

class ServerImpl {

    private static final Logger LOGGER = PrintService.createLogger(ObjectServer.class);

    private final List<ObjectContext<? extends Selector>> contextList;
    private final List<Class<?>>                          readableMessages;

    private final Object       connectionLock = new Object();
    private final ObjectServer wrapper;

    private final InetSocketAddress     address;
    private final ServerSocketChannel   ssChan;
    private final Map<SocketChannel, ObjectConnection> allConnections;

    private final Selectors  selectors;
    private final Dispatcher dispatcher;

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
        selectors        = Selectors.newInstance();
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

    public synchronized ObjectContext<? extends Selector> createContext(Class<? extends Selector> o,
                                                                        ObjectContext.Handler handler) {
        if (o == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }
        try {
            Selector mapping = o.getDeclaredConstructor().newInstance();

            ObjectContextImpl<? extends Selector> ctx = new ObjectContextImpl<>(mapping, handler, wrapper);
            contextList.add(ctx);
            return ctx;
        } catch (ReflectiveOperationException e) {
            // log handler not added
        }
        return null;
    }

    public synchronized ObjectContext<? extends Selector> createContext(Selector o,
                                                                        ObjectContext.Handler handler) {
        if (o == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }
        ObjectContextImpl<? extends Selector> ctx = new ObjectContextImpl<>(o, handler, wrapper);
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
        for (ObjectContext<? extends Selector> oc : contextList) {
            if (oc.getMapping().canSelect(message))
                return oc;
        }
        return null;
    }

    public Map<SocketChannel, ObjectConnection> getAllConnections() {
        return allConnections;
    }

    class Dispatcher implements Runnable {

        @Override
        public void run() {
            while (!finished) try {
                if (terminating) continue;

                SocketChannel chan = ssChan.accept();
                if (chan != null) {
                    chan.configureBlocking(false);
                    ObjectConnection oc = new ObjectConnection();
                    oc.setChannel(chan);
                    allConnections.put(chan, oc);

                    handle(chan, oc);
                }
            } catch (IOException ioe) {
                if (ioe instanceof AsynchronousCloseException) {
                    break;
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
            while (!finished) {
                if (terminating) continue;
                ObjectContext<?> context = connection.getContext();

                try {
                    if (context == null) {
                        connection.setRawInput(new Proto4jReader(chan, readableMessages));
                        connection.setRawOutput(new Proto4jWriter(chan));
                    }

                    Object message = ((Proto4jReader) connection.getInputStream()).readMessage();
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

        }
    }
}
