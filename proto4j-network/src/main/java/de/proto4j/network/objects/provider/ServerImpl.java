package de.proto4j.network.objects.provider; //@date 28.01.2022

import de.proto4j.annotation.server.IOReader;
import de.proto4j.annotation.server.IOWriter;
import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.network.objects.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

class ServerImpl {

    private final Object connectionLock = new Object();

    private ExecutorService threadPool;
    private Executor        executor;
    private ObjectServer    wrapper;

    private List<ObjectContext<?>> contextList;

    private InetSocketAddress   address;
    private ServerSocketChannel ssChan;

    private Set<ObjectConnection> allConnections;

    private final Map<Class<? extends Annotation>, Class<?>> conf = new HashMap<>();

    private volatile boolean finished    = false;
    private volatile boolean terminating = false;

    private boolean bound   = false;
    private boolean started = false;

    private volatile long time;

    private Thread dispatcherThread;

    private Dispatcher dispatcher;

    public ServerImpl(ObjectServer wrapper, InetSocketAddress address, int backlog) throws IOException {
        this.address = address;
        this.wrapper = wrapper;
        ssChan = ServerSocketChannel.open();
        if (address != null) {
            ServerSocket socket = ssChan.socket();
            socket.bind (address, backlog);
            bound = true;
        }

        allConnections = Collections.synchronizedSet(new HashSet<>());
        time = System.currentTimeMillis();
        contextList = new LinkedList<>();
        dispatcher = new Dispatcher();
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

        if (!conf.containsKey(IOReader.class) || !conf.containsKey(IOWriter.class)) {
            throw new IllegalStateException("No default I-Reader  and O-Writer defined!");
        }

        dispatcherThread = new Thread(null, dispatcher, "ObjectServer::Dispatcher", 0, false);
        started          = true;
        dispatcherThread.start();
    }

    public void stop(int delay) {
        if (delay < 0) throw new IllegalArgumentException("negative delay parameter");
        terminating = true;
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
            for (ObjectConnection c : allConnections) {
                c.close();
            }
        }
        if (dispatcherThread != null) {
            try {
                dispatcherThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //log exception
            }
        }
    }

    private void delay() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {/**/}
    }

    public synchronized ObjectContext<?> createContext(Object o, ObjectContext.Handler handler) {
        if (o == null || handler == null) {
            throw new NullPointerException("Mapping or Handler == null");
        }
        ObjectContextImpl<?> ctx = new ObjectContextImpl<>(o, handler, wrapper);
        contextList.add(ctx);
        return ctx;
    }

    public synchronized ObjectContext<?> createContext(Object mapping) {
        if (mapping == null) {
            throw new NullPointerException("Mapping == null");
        }
        ObjectContext<?> ctx = new ObjectContextImpl<>(mapping, null, wrapper);
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

    public Map<Class<? extends Annotation>, Class<?>> getConfiguration() {
        return conf;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public final boolean isFinished() {
        return finished;
    }

    class Dispatcher implements Runnable {

        @Override
        public void run() {
            while (!finished) try {
                if (terminating) continue;

                SocketChannel chan = ssChan.accept();
                if (chan != null) {
                    chan.configureBlocking(true);
                    ObjectConnection oc = new ObjectConnection();
                    oc.setChannel(chan);
                    allConnections.add(oc);

                    handle(chan, oc);
                }
            } catch (IOException ioe) {
                // log
            }
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
                // log
            }
        }
    }

    class Worker implements Runnable {
        private final SocketChannel chan;
        private final ObjectConnection connection;
        private ObjectContext<?> context;

        private ObjectExchange ex;
        private InputStream rin;
        private OutputStream rout;

        public Worker(SocketChannel chan, ObjectConnection oc) {
            this.chan = chan;
            connection = oc;
        }

        @Override
        public void run() {
            while (!finished) {
                if (terminating) continue;
                context = connection.getContext();

                try {
                    if (context != null) {
                        rin  = connection.getInputStream();
                        rout = connection.getRawOutputStream();
                    } else {
                        Class<?> ci = conf.get(IOReader.class);
                        Class<?> co = conf.get(IOWriter.class);

                        Constructor<?> cin = constructorLookup(ci, InputStream.class);
                        Constructor<?> out = constructorLookup(co, OutputStream.class);

                        if (cin == null || out == null) return;
                        if (cin.getParameterCount() == 0) connection.setRawInput((InputStream) cin.newInstance());
                        else connection.setRawInput((InputStream) cin.newInstance(new Proto4jReader(chan)));

                        if (out.getParameterCount() == 0) connection.setRawOutput((OutputStream) out.newInstance());
                        else connection.setRawOutput((OutputStream) cin.newInstance(new Proto4jWriter(chan)));
                    }

                    //find context
                    context = findContext();
                    if (context != null) {
                        ObjectExchangeImpl o = new ObjectExchangeImpl(connection);
                        context.getHandler().handle(o);
                    }
                } catch (Exception ex) {
                    // log
                }
            }
        }

        private Constructor<?> constructorLookup(Class<?> ci, Class<?> paramType) {
            try {
                return ci.getDeclaredConstructor(paramType);
            } catch (NoSuchMethodException e) {
                try {
                    return ci.getDeclaredConstructor();
                } catch (NoSuchMethodException exc) {
                    return null;
                }
            }
        }
    }

    private ObjectContext<?> findContext() {
        if (contextList.size() == 1) return contextList.get(0);
        return null;
    }
}
