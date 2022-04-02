package de.proto4j.network.http; //@date 25.01.2022

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.proto4j.annotation.Markup;
import de.proto4j.annotation.http.requests.HttpResponseType;
import de.proto4j.internal.Packages;
import de.proto4j.internal.model.bean.*;
import de.proto4j.network.http.invocation.HttpExchangeProcessor;
import de.proto4j.network.http.invocation.ParameterProcessor;
import de.proto4j.network.http.response.EntityResponseHandler;
import de.proto4j.network.http.response.ResponseInvocationHandler;
import de.proto4j.network.http.response.StringResponseHandler;
import de.proto4j.stream.SequenceStream;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public final class WebServlet {

    public static HttpServerContext runHttpServer(Class<?> mainClass) throws IOException {
        if (mainClass != null && Markup.isWebServer(mainClass)) {
            if (!Markup.isHttp(mainClass)) {
                log(ErrorMessage.HTTP_NOT_DEFINED, mainClass.getSimpleName());
            }

            InetSocketAddress address  = new InetSocketAddress(Markup.getWebServerMarkup(mainClass).port());
            HttpServer        server   = HttpServer.create(address, 0);
            BeanManager       manager  = new MapBeanManager();
            Object            executor = null;

            SequenceStream<Class<?>> stream = Packages.readClasses(mainClass);

            stream.slice(Markup::isHttpController).forEach(x -> BeanManaging.mapHttpController(manager, x));

            Map<String, RouteCache> webRoutes = createRoutes(manager.findAll(Markup::isHttpController));
            webRoutes.forEach((s, rc) -> server.createContext(s, new ServerInvocationHandler(rc)));

            try {
                Class<?> e = stream.find(Markup::isCommandExecutor);
                if (e.isAssignableFrom(Executor.class)) {
                    executor = e.getDeclaredConstructor().newInstance();
                }
            } catch (Exception _e) { /*just ignore that, maybe a log entry*/ }

            server.setExecutor(executor != null ? (Executor) executor : null); //maybe add annotation here
            synchronized (WebServlet.class) {
                server.start();
            }

            return makeContext(server, mainClass, executor != null ? executor.getClass() : null,
                               webRoutes.keySet(), UnmodifiableBeanManager.of(manager));
        }
        return null;
    }

    private static HttpServerContext makeContext(HttpServer s, Class<?> mainClass, Class<?> e, Set<String> routes,
                                                 UnmodifiableBeanManager manager) {

        return new HttpServerContextImpl(manager, s, routes, e, mainClass);
    }

    private static void log(ErrorMessage em, Object... format) {
        System.out.printf(em.getMessage(), format);
    }

    private static Map<String, RouteCache> createRoutes(SequenceStream<SimpleBeanCache> beanList) {
        Map<String, RouteCache> routes = new HashMap<>();
        if (beanList.size() == 0) return routes;

        beanList.forEach(c -> {
            String baseMapping = Markup.getHttpControllerMarkup(c.getMappedClass()).mapping();
            if (baseMapping.endsWith("/")) baseMapping = baseMapping.substring(baseMapping.length() - 1);

            for (Method m : c.getMappedClass().getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) continue;

                if (Markup.isHttpListener(m)) {
                    String path = Markup.getHttpListenerMarkup(m).path();
                    if (path.length() == 0 || path.equals("/")) path = baseMapping;
                    else path = baseMapping + "/" + path;

                    if (!routes.containsKey(path)) {
                        routes.put(path, new RouteCache(c.getMappedClass(), c.getInstance(), path, m));
                    }
                }
            }
        });
        return routes;
    }

    private static class ServerInvocationHandler implements HttpHandler {

        private final ParameterProcessor<HttpExchange, Object> processor = new HttpExchangeProcessor();

        private final ResponseInvocationHandler<ResponseEntity<?>> entityHandler = new EntityResponseHandler();
        private final ResponseInvocationHandler<String>            stringHandler = new StringResponseHandler();


        private final RouteCache routeCache;


        private ServerInvocationHandler(RouteCache routeCache) {this.routeCache = routeCache;}

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            //maybe route check
            Method m = routeCache.getMethod();
            if (m != null) {
                m.setAccessible(true);
                Object response = null;

                if (m.getParameterCount() == 0) {
                    response = trycatch(m);
                } else {
                    if (isFirstParameter(m.getParameters())) {
                        response = trycatch(m, new Object[]{exchange});
                    } else {
                        Object[] args = resolveArgs(exchange, m.getParameters());
                        response = trycatch(m, args);
                    }
                }

                if (response != null && Markup.isHttpResponseBody(m)) {

                    HttpResponseType type = Markup.getHttpResponseBodyMarkup(m).value();
                    if (response instanceof String) {
                        stringHandler.handle((String) response, exchange, type);
                    } else if (response instanceof ResponseEntity) {
                        entityHandler.handle((ResponseEntity<?>) response, exchange, type);
                    }
                }
            }
        }

        private Object[] resolveArgs(HttpExchange exchange, Parameter[] parameters) {
            Object[] p = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter p0 = parameters[i];
                p[i] = processor.process(p0, exchange);
            }
            return p;
        }

        private boolean isFirstParameter(Parameter[] parameters) {
            //X.class.isAssignableFrom(Y.class) <-> X.isSuperclassOf(Y.class)
            return (HttpExchange.class.isAssignableFrom(parameters[0].getType()));
        }

        private Object trycatch(Method m) {
            return trycatch(m, new Object[0]);
        }

        private Object trycatch(Method m, Object[] args) {
            try {

                if (args.length == 0)
                    return m.invoke(routeCache.getInstance());
                else
                    return m.invoke(routeCache.getInstance(), args);

            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    log(ErrorMessage.EXECUTION_ERROR, routeCache.getMappedClass().getSimpleName(),
                        ((InvocationTargetException) e).getTargetException().getMessage());
                } else {
                    log(ErrorMessage.EXECUTION_ERROR,
                        routeCache.getMappedClass().getSimpleName(), e.getMessage());
                }
            }
            return null;
        }

    }

    private static class HttpServerContextImpl implements HttpServerContext {

        private final BeanManager manager;
        private final HttpServer  server;
        private final Set<String> routes;
        private final Class<?>    executor;
        private final Class<?>    main;

        private HttpServerContextImpl(BeanManager manager, HttpServer server, Set<String> routes, Class<?> executor,
                                      Class<?> main) {
            this.manager  = manager;
            this.server   = server;
            this.routes   = routes;
            this.executor = executor;
            this.main     = main;
        }

        @Override
        public Class<?> getExecutorType() {
            return executor;
        }

        @Override
        public Class<?> getMainClass() {
            return main;
        }

        @Override
        public HttpServer getServer() {
            return server;
        }

        @Override
        public BeanManager getBeans() {
            return manager;
        }

        public Set<String> getWebRoutes() {
            return routes;
        }
    }
}
