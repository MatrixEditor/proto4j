package de.proto4j.network.http; //@date 25.01.2022

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.proto4j.annotation.http.Http;
import de.proto4j.annotation.http.Https;
import de.proto4j.annotation.http.WebServer;
import de.proto4j.annotation.http.requests.RequestController;
import de.proto4j.annotation.http.requests.RequestListener;
import de.proto4j.annotation.http.requests.ResponseBody;
import de.proto4j.internal.model.Reflections;
import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.MapBeanManager;
import de.proto4j.internal.model.bean.SimpleBeanCacheList;
import de.proto4j.network.http.invocation.HttpExchangeReference;
import de.proto4j.network.http.invocation.InvocationReference;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class WebServlet {

    public static void runHttpServer(Class<?> mainClass) throws IOException {
        if (mainClass != null && mainClass.isAnnotationPresent(WebServer.class)) {
            if (mainClass.isAnnotationPresent(Https.class)) {
                log(ErrorMessage.HTTP_ON_HTTPS, mainClass.getSimpleName());
            }

            if (!mainClass.isAnnotationPresent(Http.class)) {
                log(ErrorMessage.HTTP_NOT_DEFINED, mainClass.getSimpleName());
            }

            WebServer  ws     = mainClass.getDeclaredAnnotation(WebServer.class);
            HttpServer server = HttpServer.create(new InetSocketAddress(ws.port()), 0);

            BeanManager   manager = new MapBeanManager();
            Set<Class<?>> classes = Reflections.getClassesFromMain(mainClass);

            for (Class<?> controller : Reflections.findByAnnotationAsSet(classes, c0 -> {
                return c0.isAnnotationPresent(RequestController.class);
            })) manager.mapIfAbsent(controller, RequestController.class);

            Map<String, RouteCache> webRoutes = createRoutes(manager.findAll(RequestController.class));
            webRoutes.forEach((s, rc) -> server.createContext(s, new ServerInvocationHandler(rc)));

            server.setExecutor(null);
            synchronized (WebServlet.class) {
                server.start();
            }
        }
    }

    private static void log(ErrorMessage em, Object... format) {
        System.out.printf(em.getMessage(), format);
    }

    private static Map<String, RouteCache> createRoutes(SimpleBeanCacheList beanList) {
        Map<String, RouteCache> routes = new HashMap<>();
        if (beanList.isEmpty()) return routes;

        beanList.iterator().forEachRemaining(c -> {
            String baseMapping = c.getMappedClass().getDeclaredAnnotation(RequestController.class).mapping();
            if (baseMapping.endsWith("/")) baseMapping = baseMapping.substring(baseMapping.length() - 1);

            for (Method m : c.getMappedClass().getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) continue;

                if (m.isAnnotationPresent(RequestListener.class)) {
                    String path = m.getDeclaredAnnotation(RequestListener.class).path();
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

        private final InvocationReference<HttpExchange, Object> processor = new HttpExchangeReference();

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
                        response = trycatch(m, new Object[] {exchange});
                    } else {
                        Object[] args = resolveArgs(exchange, m.getParameters());
                        response = trycatch(m, args);
                    }
                }

                if (response != null && m.isAnnotationPresent(ResponseBody.class)) {
                    switch (m.getDeclaredAnnotation(ResponseBody.class).type()) {
                        case PLAIN_RESPONSE:
                            String finalResponse = String.valueOf(response);
                            try (OutputStream os = exchange.getResponseBody()) {
                                exchange.sendResponseHeaders(200, finalResponse.length());
                                os.write(finalResponse.getBytes());
                            }
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
            return (parameters[0].getType().isAssignableFrom(HttpExchange.class));
        }

        private Object trycatch(Method m) {
            return trycatch(m, new Object[0]);
        }

        private Object trycatch(Method m, Object[] args) {
            try {
                if (args.length == 0) return m.invoke(routeCache.getInstance());
                else return m.invoke(routeCache.getInstance(), args);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    log(ErrorMessage.EXECUTION_ERROR, routeCache.getMappedClass().getSimpleName(),
                        ((InvocationTargetException) e).getTargetException().getMessage());
                } else log(ErrorMessage.EXECUTION_ERROR,
                    routeCache.getMappedClass().getSimpleName(), e.getMessage());
            }
            return null;
        }

    }
}
