package de.proto4j.network.objects.client; //@date 29.01.2022

import de.proto4j.annotation.AnnotationLookup;
import de.proto4j.annotation.Markup;
import de.proto4j.annotation.server.requests.selection.FirstParameterSelector;
import de.proto4j.annotation.server.requests.selection.Selector;
import de.proto4j.annotation.threding.DirectThreadPool;
import de.proto4j.internal.Packages;
import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.BeanManaging;
import de.proto4j.internal.model.bean.MapBeanManager;
import de.proto4j.internal.model.bean.SimpleBeanCache;
import de.proto4j.network.EnvironmentBuilder;
import de.proto4j.network.objects.*;
import de.proto4j.stream.InterruptedStream;
import de.proto4j.stream.SequenceStream;
import de.proto4j.stream.Streams;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static de.proto4j.annotation.AnnotationLookup.*;

public final class ClientProvider {

    public static ObjectConnection getConnection(String host, ObjectClient client) {
        Collection<ObjectConnection> c = client.getAllConnections();
        if (c.size() == 0) return null;

        return c.stream().filter(oc -> ((InetSocketAddress) oc.getChannel().socket().getRemoteSocketAddress())
                        .getAddress().getHostAddress().equals(host))
                .findFirst()
                .orElse(null);
    }

    public static ClientContext createClient(Class<?> mainClass) {
        Objects.requireNonNull(mainClass);
        Markup.requireTypeClient(mainClass);

        EnvironmentBuilder builder = new ObjectClientEnvBuilder(mainClass);

        ObjectClient client = builder.buildBase();
        BeanManager  bm     = builder.buildManager();

        builder.buildConfiguration();
        builder.buildBeans(AnnotationLookup.ConfigurationLookup.areValuesIgnored(mainClass));

        builder.buildThreadPool(mainClass);
        builder.iterCaches(bm.findAll(BeanManaging::filterController), builder.buildInterrupted());

        client.start();
        return (ClientContext) builder.finish();
    }

    public static class ObjectClientEnvBuilder implements EnvironmentBuilder {

        private final List<String> conf = new LinkedList<>();

        private final Class<?> main;

        private ObjectClient oc;
        private BeanManager  bm;

        private SequenceStream<Class<?>> classes;

        public ObjectClientEnvBuilder(Class<?> main) {this.main = main;}

        @Override
        public BeanManager buildManager() {
            bm = new MapBeanManager();
            return bm;
        }

        @Override
        public ObjectClient buildBase() {
            try {
                oc = ObjectClient.create();
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage());
            }
            return oc;
        }

        @Override
        public void buildConfiguration() {
            conf.add(AnnotationLookup.ConfigurationLookup.isByConnection(main) ? CONF_BY_CONNECTION : CONF_BY_VALUE);
        }

        @Override
        public SequenceStream<Class<?>> buildBeans(boolean ignoreValues) {
            SequenceStream<Class<?>> stream = Packages.readClasses(main, true);

            if (ignoreValues) {
                stream = stream.slice(Packages.getPackageFilter(main));
                conf.add(CONF_IGNORE_VALUES);
            }

            stream.slice(Markup::isController).forEach(x -> BeanManaging.mapController(bm, x));
            stream.slice(Markup::isMessage).forEach(oc.getMessageTypes()::add);

            classes = stream;
            return stream;
        }

        @Override
        public InterruptedStream<SimpleBeanCache> buildInterrupted() {
            InterruptedStream<SimpleBeanCache> is = Streams.prepareInterruptedStream();
            is.forEach(x -> buildContext(x, oc));
            return is;
        }

        @Override
        public void buildContext(SimpleBeanCache cache, ObjectClient ref) {
            buildRequestHandlers(cache.getMappedClass(), AnnotationLookup.HandlerLookup::isServerRequestHandler)
                    .forEach(m -> buildHandler(m, ref, cache));
        }

        @Override
        public ObjectContext.Handler buildHandler(Method m, ObjectClient oc, SimpleBeanCache sbc) {
            Class<? extends Selector> s = getSelectorType(m);

            ContextCache cc = new ContextCache(m, sbc.getInstance(), s, sbc.getMappedClass());
            ObjectContext.Handler handler = new InternalObjectHandler(cc, oc);

            if (s == FirstParameterSelector.class) {
                Selector inst = new FirstParameterSelector(m.getParameterTypes()[0]);
                oc.createContext(inst, handler);
            } else if (s == AddressSelector.class) {

                String path = Markup.getControllerMarkup(sbc.getMappedClass()).value();
                if (path.length() == 0) oc.createContext(cc.getSelectorType(), handler);
                else {
                    Selector selector = new AddressSelector(path);
                    oc.createContext(selector, handler);
                }
            } else if (s == Selector.class) {
                oc.createContext(m.getParameters(), handler);
            } else oc.createContext(cc.getSelectorType(), handler);
            return handler;
        }

        @Override
        public ExecutorService buildThreadPool(Class<?> c) {
            if (!Markup.isThreadPooling(c)) {
                ExecutorService e = new DirectThreadPool();
                oc.setThreadPool(e);
                return e;
            }
            Markup.requireThreadPooling(c);
            Class<? extends ExecutorService> exec = Markup.getThreadPoolingMarkup(c).poolType();

            int parallelism = Markup.getThreadPoolingMarkup(c).parallelism();

            ExecutorService e = null;
            try {

                if (parallelism == -1) e = exec.getDeclaredConstructor().newInstance();
                else e = exec.getDeclaredConstructor(int.class).newInstance(parallelism);

                oc.setThreadPool(e);
            } catch (ReflectiveOperationException ex) {/**/}
            return e;
        }

        @Override
        public TypeContext finish() {
            return new ClientContext(oc, bm, classes, main, conf);
        }

        private Class<? extends Selector> getSelectorType(Method m) {
            Class<? extends Selector> s;
            if (Markup.isRequestHandler(m)) s = Markup.getRequestHandlerMarkup(m).selectorType();
            else s = AddressSelector.class;
            return s;
        }
    }

}
