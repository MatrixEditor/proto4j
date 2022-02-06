package de.proto4j.network.objects.server; //@date 29.01.2022

import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.server.TypeServer;
import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.annotation.server.requests.RequestHandler;
import de.proto4j.annotation.server.requests.ResponseBody;
import de.proto4j.annotation.server.requests.selection.FirstParameterSelector;
import de.proto4j.annotation.server.requests.selection.Selector;
import de.proto4j.annotation.threding.*;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.internal.logger.Logger;
import de.proto4j.internal.logger.PrintColor;
import de.proto4j.internal.logger.PrintService;
import de.proto4j.internal.method.MethodLookup;
import de.proto4j.internal.model.Reflections;
import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.MapBeanManager;
import de.proto4j.network.objects.ContextCache;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.ObjectExchange;
import de.proto4j.network.objects.TypeContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ServerProvider {

    public static TypeContext runServer(Class<?> mainClass) throws IOException {
        if (mainClass == null) throw new NullPointerException("main-class can not be null");

        if (!mainClass.isAnnotationPresent(TypeServer.class))
            throw new IllegalArgumentException("mainClass is not a type-server");

        final int port    = mainClass.getDeclaredAnnotation(TypeServer.class).port();
        boolean   pooling = mainClass.isAnnotationPresent(ThreadPooling.class);

        ObjectServer server = ObjectServer.create(new InetSocketAddress(port), 0);

        BeanManager   manager = new MapBeanManager();
        Set<Class<?>> beans   = Reflections.getClassesFromMain(mainClass);
        Reflections.removeNonMessageClasses(mainClass, beans);

        for (Class<?> cont : Reflections.findByAnnotationAsSet(beans, c -> {
            return c.isAnnotationPresent(Controller.class);
        })) {
            manager.mapIfAbsent(cont, Controller.class);
        }

        Reflections.findByAnnotationAsSet(beans, c -> {
            return c.isAnnotationPresent(Message.class);
        }).forEach(server.getMessageTypes()::add);

        manager.findAll(Controller.class).iterator().forEachRemaining(e -> {
            for (Method m : e.getMappedClass().getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) continue;

                if (m.isAnnotationPresent(RequestHandler.class)) {
                    Class<? extends Selector> s = m.getDeclaredAnnotation(RequestHandler.class).selectorType();

                    ContextCache cc = new ContextCache(m, e.getInstance(), s, e.getMappedClass());

                    InternalServerHandler ish = new InternalServerHandler(cc, server);
                    if (s == FirstParameterSelector.class) {
                        Selector inst = new FirstParameterSelector(m.getParameterTypes()[0]);
                        server.createContext(inst, ish);
                    } else if (s == Selector.class) {
                      server.createContext(m.getParameters(), ish);
                    } else server.createContext(cc.getSelectorType(), ish);
                }
            }
        });

        if (pooling) {
            ThreadPooling es = mainClass.getDeclaredAnnotation(ThreadPooling.class);
            try {
                ExecutorService e;
                if (es.parallelism() == -1) e = es.poolType().getDeclaredConstructor().newInstance();
                else e = es.poolType().getDeclaredConstructor(int.class).newInstance(es.parallelism());

                server.setThreadPool(e);
            } catch (ReflectiveOperationException ex) {/**/}
        } else server.setExecutor(Executors.newSingleThreadExecutor());

        server.start();
        return new ServerContextImpl(server, manager, beans, mainClass);
    }


    private static class InternalServerHandler implements ObjectContext.Handler {

        private static final Logger logger = PrintService.createLogger(InternalServerHandler.class);

        private final ContextCache cache;
        private final ObjectServer server;

        private InternalServerHandler(ContextCache cache, ObjectServer server) {
            this.cache  = cache;
            this.server = server;
        }

        @Override
        public void handle(ObjectExchange exchange) {
            if (exchange == null || cache == null || server == null) return;

            Method m = cache.getMethod();

            Object response = null;
            boolean fromParallel = m.isAnnotationPresent(Parallel.class)
                    || m.isAnnotationPresent(SupplyParallel.class);

            Object[] args;
            if (m.getParameterCount() == 1 && ObjectExchange.class.isAssignableFrom(m.getParameters()[0].getType())) {
                args = new Object[]{exchange};
            } else {
                try {
                    args = MethodLookup.tryCreate(exchange.getMessage(), exchange, m.getParameters());
                } catch (IllegalAccessException e) {
                    //log
                    return;
                }
            }

            if (!fromParallel) {
                response = invokeMethod(args, m);
            } else {
                if (m.isAnnotationPresent(Parallel.class)) {
                    ParallelExecutor pe = Threads.newParallelThreadExecutor();
                    pe.execute(() -> invokeMethod(args, m));
                } else {
                    ParallelSupplier ps = Threads.newSingleThreadSupplier();
                    response = ps.supplyAsync(() -> invokeMethod(args, m));
                }
            }
            if (m.isAnnotationPresent(ResponseBody.class)) {
                // this check prevents exceptions to be thrown from the underlying Proto4jWriter.
                if (response != null && server.getMessageTypes().contains(response.getClass())) {
                    try {
                        Proto4jWriter w = (Proto4jWriter) exchange.getResponseBody();
                        w.write(response);
                    } catch (IOException e) {
                        logger.except(PrintColor.DARK_RED, e);
                    }
                }
            }
        }

        private Object invokeMethod(Object[] args, Method m) {
            try {
                return m.invoke(cache.getInvoker(), args);
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.except(PrintColor.DARK_RED, e);
            }
            return null;
        }
    }

    private static class ServerContextImpl implements TypeContext {
        private final ObjectServer  server;
        private final BeanManager   manager;
        private final Set<Class<?>> classes;
        private final Class<?>      main;

        private ServerContextImpl(ObjectServer server, BeanManager manager, Set<Class<?>> classes, Class<?> main) {
            this.server  = server;
            this.manager = manager;
            this.classes = classes;
            this.main    = main;
        }

        public ObjectServer getServer() {
            return server;
        }

        @Override
        public BeanManager getBeanManager() {
            return manager;
        }

        @Override
        public Set<Class<?>> loadedClasses() {
            return classes;
        }

        @Override
        public Class<?> mainClass() {
            return main;
        }
    }
}
