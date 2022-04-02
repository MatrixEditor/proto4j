package de.proto4j.network.objects.server; //@date 29.01.2022

import de.proto4j.annotation.AnnotationLookup;
import de.proto4j.annotation.Markup;
import de.proto4j.annotation.server.requests.selection.FirstParameterSelector;
import de.proto4j.annotation.server.requests.selection.Selector;
import de.proto4j.internal.Packages;
import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.BeanManaging;
import de.proto4j.internal.model.bean.MapBeanManager;
import de.proto4j.internal.model.bean.SimpleBeanCache;
import de.proto4j.network.EnvironmentBuilder;
import de.proto4j.network.objects.ContextCache;
import de.proto4j.network.objects.InternalObjectHandler;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.TypeContext;
import de.proto4j.network.objects.client.ObjectClient;
import de.proto4j.stream.InterruptedStream;
import de.proto4j.stream.SequenceStream;
import de.proto4j.stream.Streams;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public final class ServerProvider {

    public static TypeContext runServer(Class<?> mainClass) {
        Objects.requireNonNull(mainClass);
        Markup.requireTypeServer(mainClass);

        final int port    = Markup.getTypeServerMarkup(mainClass).port();
        boolean   pooling = Markup.isThreadPooling(mainClass);

        EnvironmentBuilder builder = new ObjectServerEnvBuilder(pooling, port, mainClass);

        ObjectServer os = (ObjectServer) builder.buildBase();
        BeanManager  bm = builder.buildManager();

        builder.buildBeans();
        builder.iterCaches(bm.findAll(BeanManaging::filterController), builder.buildInterrupted());
        builder.buildThreadPool(mainClass);

        os.start();
        return builder.finish();
    }

    public static class ObjectServerEnvBuilder implements EnvironmentBuilder {

        private final boolean pooling;
        private final int     port;

        private final Class<?> main;

        private BeanManager              manager;
        private ObjectServer             server;
        private SequenceStream<Class<?>> classes;

        public ObjectServerEnvBuilder(boolean pooling, int port, Class<?> main) {
            this.pooling = pooling;
            this.port    = port;
            this.main    = main;
        }

        @Override
        public BeanManager buildManager() {
            manager = new MapBeanManager();
            return manager;
        }

        @Override
        public ObjectClient buildBase() {
            try {
                server = ObjectServer.create(new InetSocketAddress(port), 0);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage());
            }
            return server;
        }

        @Override
        public SequenceStream<Class<?>> buildBeans(boolean ignoreValues) {
            SequenceStream<Class<?>> stream = Packages.readClasses(main, true);

            if (ignoreValues) {
                stream = stream.slice(Packages.getPackageFilter(main));
            }

            stream.slice(Markup::isController).forEach(c -> BeanManaging.mapController(manager, c));
            stream.slice(Markup::isMessage).forEach(server.getMessageTypes()::add);
            classes = stream;
            return stream;
        }

        @Override
        public InterruptedStream<SimpleBeanCache> buildInterrupted() {
            InterruptedStream<SimpleBeanCache> is = Streams.prepareInterruptedStream();
            is.forEach(x -> buildContext(x, server));
            return is;
        }

        @Override
        public void buildContext(SimpleBeanCache cache, ObjectClient ref) {
            buildRequestHandlers(cache.getMappedClass(), AnnotationLookup.HandlerLookup::isServerRequestHandler)
                    .forEach(m -> buildHandler(m, ref, cache));
        }

        @Override
        public ObjectContext.Handler buildHandler(Method m, ObjectClient oc, SimpleBeanCache sbc) {
            Markup.requireRequestHandler(m);
            Class<? extends Selector> s = Markup.getRequestHandlerMarkup(m).selectorType();

            ContextCache cc = new ContextCache(m, sbc.getInstance(), s, sbc.getMappedClass());

            ObjectContext.Handler handler = new InternalObjectHandler(cc, server);
            if (s == FirstParameterSelector.class) {
                Selector inst = new FirstParameterSelector(m.getParameterTypes()[0]);
                server.createContext(inst, handler);
            } else if (s == Selector.class) {
                server.createContext(m.getParameters(), handler);
            } else server.createContext(cc.getSelectorType(), handler);
            return handler;
        }

        @Override
        public ExecutorService buildThreadPool(Class<?> c) {
            if (!pooling) {
                ExecutorService e = new ForkJoinPool();
                server.setThreadPool(e);
                return e;
            }
            Markup.requireThreadPooling(c);
            Class<? extends ExecutorService> exec = Markup.getThreadPoolingMarkup(c).poolType();

            int parallelism = Markup.getThreadPoolingMarkup(c).parallelism();

            ExecutorService e = null;
            try {

                if (parallelism == -1) e = exec.getDeclaredConstructor().newInstance();
                else e = exec.getDeclaredConstructor(int.class).newInstance(parallelism);

                server.setThreadPool(e);
            } catch (ReflectiveOperationException ex) {/**/}
            return e;
        }

        @Override
        public TypeContext finish() {
            return new ServerContextImpl(server, manager, classes, main);
        }
    }

    public static class ServerContextImpl implements TypeContext {
        private final ObjectServer  server;
        private final BeanManager   manager;
        private final Set<Class<?>> classes;
        private final Class<?>      main;

        private ServerContextImpl(ObjectServer server, BeanManager manager, SequenceStream<Class<?>> stream,
                                  Class<?> main) {
            this.server  = server;
            this.manager = manager;
            this.classes = new HashSet<>();
            this.main    = main;

            stream.forEach(classes::add);
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
