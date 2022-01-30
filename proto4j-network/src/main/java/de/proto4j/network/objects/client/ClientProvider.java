package de.proto4j.network.objects.client; //@date 29.01.2022

import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.selection.FirstParameterSelector;
import de.proto4j.annotation.selection.Selector;
import de.proto4j.annotation.server.Configuration;
import de.proto4j.annotation.server.TypeClient;
import de.proto4j.annotation.server.requests.ConnectionHandler;
import de.proto4j.annotation.server.requests.Controller;
import de.proto4j.annotation.server.requests.RequestHandler;
import de.proto4j.annotation.server.requests.ResponseBody;
import de.proto4j.annotation.threding.*;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.internal.logger.Logger;
import de.proto4j.internal.logger.PrintColor;
import de.proto4j.internal.logger.PrintService;
import de.proto4j.internal.model.Reflections;
import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.MapBeanManager;
import de.proto4j.network.objects.AddressSelector;
import de.proto4j.network.objects.ContextCache;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.ObjectExchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public final class ClientProvider {

    public static ClientContext createClient(Class<?> mainClass) {
        if (mainClass == null) throw new NullPointerException("main-class can not be null");

        if (!mainClass.isAnnotationPresent(TypeClient.class))
            throw new IllegalArgumentException("Main-class is not a client");

        List<String> conf = new LinkedList<>();


        BeanManager  manager = new MapBeanManager();

        Set<Class<?>> beans = Reflections.getClassesFromMain(mainClass);
        if (Configuration.Lookup.areValuesIgnored(mainClass)) {
            conf.add(Configuration.IGNORE_VALUES);
            Reflections.removeNonMessageClasses(mainClass, beans);
        }

        conf.add(Configuration.Lookup.isByConnection(mainClass) ? Configuration.BY_CONNECTION : Configuration.BY_VALUE);

        for (Class<?> c : Reflections.findByAnnotationAsSet(beans, x -> {
            return x.isAnnotationPresent(Controller.class);
        })) {
            manager.mapIfAbsent(c, Controller.class);
        }

        Set<Class<?>> messageTypes =
                Reflections.findByAnnotationAsSet(beans, c -> c.isAnnotationPresent(Message.class));

        ObjectClient client;
        try {
            client = ObjectClient.create();
        } catch (IOException e) {
            throw new IllegalStateException("couldn't create client: " + e.getMessage(), e);
        }
        client.getMessageTypes().addAll(messageTypes);

        if (mainClass.isAnnotationPresent(ThreadPooling.class)) {
            conf.add("threadPooling");

            ThreadPooling es = mainClass.getDeclaredAnnotation(ThreadPooling.class);
            try {
                ExecutorService e;
                if (es.parallelism() == -1) e = es.poolType().getDeclaredConstructor().newInstance();
                else e = es.poolType().getDeclaredConstructor(int.class).newInstance(es.parallelism());

                client.setThreadPool(e);
            } catch (ReflectiveOperationException ex) {/**/}
        } else client.setThreadPool(new ForkJoinPool());

        manager.findAll(Controller.class).iterator().forEachRemaining(c -> {
            for (Method m : c.getMappedClass().getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) continue;

                if ((conf.contains(Configuration.BY_CONNECTION) && m.isAnnotationPresent(ConnectionHandler.class))
                        || m.isAnnotationPresent(RequestHandler.class)) {
                    Class<? extends Selector> s;
                    if (m.isAnnotationPresent(RequestHandler.class))
                        s = m.getDeclaredAnnotation(RequestHandler.class).selectorType();
                    else s = AddressSelector.class;

                    ContextCache          cc  = new ContextCache(m, c.getInstance(), s, c.getMappedClass());
                    InternalClientHandler ich = new InternalClientHandler(cc, client);
                    if (s == FirstParameterSelector.class) {

                        Selector inst = new FirstParameterSelector(m.getParameterTypes()[0]);
                        client.createContext(inst, ich);
                    } else if (s == AddressSelector.class) {

                        Controller con = c.getMappedClass().getDeclaredAnnotation(Controller.class);
                        if (con.value().length() == 0) client.createContext(cc.getSelectorType(), ich);
                        else {
                            Selector selector = new AddressSelector(con.value());
                            client.createContext(selector, ich);
                        }
                    } else client.createContext(cc.getSelectorType(), ich);
                }
            }
        });

        // this method actually has no side effects -> there are only a few
        // checks that has to be done to make sure no errors would display.
        client.getConfiguration().addAll(conf);

        client.start();
        return new ClientContext(client, manager, beans, mainClass, conf);

    }

    private static class InternalClientHandler implements ObjectContext.Handler {

        private static final Logger logger = PrintService.createLogger(InternalClientHandler.class);

        private final ContextCache cache;
        private final ObjectClient client;

        private InternalClientHandler(ContextCache cache, ObjectClient client) {
            this.cache  = cache;
            this.client = client;
        }

        @Override
        public void handle(ObjectExchange exchange) {
            if (exchange == null || cache == null || client == null) return;

            Method m = cache.getMethod();

            Object response = null;
            boolean fromParallel = m.isAnnotationPresent(Parallel.class)
                    || m.isAnnotationPresent(SupplyParallel.class);


            if (!fromParallel) {
                if (m.getParameterCount() == 1) {
                    response = invokeMethod(exchange, m);
                }
            } else {
                if (m.isAnnotationPresent(Parallel.class)) {
                    ParallelExecutor pe = Threads.newParallelThreadExecutor();
                    pe.execute(() -> invokeMethod(exchange, m));
                } else {
                    ParallelSupplier ps = Threads.newSingleThreadSupplier();
                    response = ps.supplyAsync(() -> invokeMethod(exchange, m));
                }
            }
            if (cache.getMethod().isAnnotationPresent(ConnectionHandler.class)) return;

            if (m.isAnnotationPresent(ResponseBody.class)) {
                if (response != null && client.getMessageTypes().contains(response.getClass())) {
                    try {
                        Proto4jWriter w = (Proto4jWriter) exchange.getResponseBody();
                        w.write(response);
                    } catch (IOException e) {
                        logger.except(PrintColor.DARK_RED, e);
                    }
                }
            }
        }

        private Object invokeMethod(ObjectExchange exchange, Method m) {
            if (ObjectExchange.class.isAssignableFrom(m.getParameterTypes()[0])) {
                try {
                    return m.invoke(cache.getInvoker(), exchange);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    logger.except(PrintColor.DARK_RED, e);
                }
            } else if (exchange.getMessage().getClass().isAssignableFrom(m.getParameterTypes()[0])) {
                try {
                    return m.invoke(cache.getInvoker(), exchange.getMessage());
                } catch (InvocationTargetException | IllegalAccessException e) {
                    logger.except(PrintColor.DARK_RED, e);
                }
            }
            return null;
        }
    }
}
