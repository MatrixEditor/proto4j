package de.proto4j.annotation; //@date 12.02.2022

import de.proto4j.annotation.http.Http;
import de.proto4j.annotation.http.WebServer;
import de.proto4j.annotation.http.requests.HttpRequestController;
import de.proto4j.annotation.http.requests.HttpRequestListener;
import de.proto4j.annotation.http.requests.HttpResponseBody;
import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.server.Configuration;
import de.proto4j.annotation.server.TypeClient;
import de.proto4j.annotation.server.TypeServer;
import de.proto4j.annotation.server.requests.*;
import de.proto4j.annotation.threding.CommandExecutor;
import de.proto4j.annotation.threding.Parallel;
import de.proto4j.annotation.threding.SupplyParallel;
import de.proto4j.annotation.threding.ThreadPooling;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public final class Markup {

    private Markup() {}

    public static boolean isHttp(AnnotatedElement e) {
        return check(e, Http.class);
    }

    public static boolean isHttpController(AnnotatedElement e) {
        return check(e, HttpRequestController.class);
    }

    public static boolean isHttpListener(AnnotatedElement e) {
        return check(e, HttpRequestListener.class);
    }

    public static boolean isCommandExecutor(AnnotatedElement e) {
        return check(e, CommandExecutor.class);
    }

    public static boolean isWebServer(AnnotatedElement e) {
        return check(e, WebServer.class);
    }

    public static boolean isHttpResponseBody(AnnotatedElement e) {
        return check(e, HttpResponseBody.class);
    }

    private static boolean check(AnnotatedElement e, Class<? extends Annotation> a) {
        return e.isAnnotationPresent(a);
    }

    public static WebServer getWebServerMarkup(AnnotatedElement v) {
        return get(v, WebServer.class);
    }

    public static HttpRequestController getHttpControllerMarkup(AnnotatedElement c) {
        return get(c, HttpRequestController.class);
    }

    public static HttpRequestListener getHttpListenerMarkup(AnnotatedElement c) {
        return get(c, HttpRequestListener.class);
    }

    public static HttpResponseBody getHttpResponseBodyMarkup(AnnotatedElement c) {
        return get(c, HttpResponseBody.class);
    }

    private static <A extends Annotation> A get(AnnotatedElement c, Class<A> a) {
        return c.getDeclaredAnnotation(a);
    }

    public static Message getMessageMarkup(AnnotatedElement c) {
        return get(c, Message.class);
    }

    public static boolean isMessage(AnnotatedElement e) {
        return check(e, Message.class);
    }

    public static Configuration getConfigurationMarkup(AnnotatedElement c) {
        return get(c, Configuration.class);
    }

    public static boolean isConfiguration(AnnotatedElement e) {
        return check(e, Configuration.class);
    }

    public static TypeClient getTypeClientMarkup(AnnotatedElement c) {
        return get(c, TypeClient.class);
    }

    public static boolean isTypeClient(AnnotatedElement e) {
        return check(e, TypeClient.class);
    }

    public static ConnectionHandler getConnectionHandlerMarkup(AnnotatedElement c) {
        return get(c, ConnectionHandler.class);
    }

    public static boolean isConnectionHandler(AnnotatedElement e) {
        return check(e, ConnectionHandler.class);
    }

    public static Controller getControllerMarkup(AnnotatedElement c) {
        return get(c, Controller.class);
    }

    public static boolean isController(AnnotatedElement e) {
        return check(e, Controller.class);
    }

    public static RequestHandler getRequestHandlerMarkup(AnnotatedElement c) {
        return get(c, RequestHandler.class);
    }

    public static boolean isRequestHandler(AnnotatedElement e) {
        return check(e, RequestHandler.class);
    }

    public static ResponseBody getResponseBodyMarkup(AnnotatedElement c) {
        return get(c, ResponseBody.class);
    }

    public static boolean isResponseBody(AnnotatedElement e) {
        return check(e, ResponseBody.class);
    }

    public static void requireTypeClient(AnnotatedElement e) {
        if (!check(e, TypeClient.class))
            throw new IllegalArgumentException("object is not a TypeClient.");
    }

    public static ThreadPooling getThreadPoolingMarkup(AnnotatedElement c) {
        return get(c, ThreadPooling.class);
    }

    public static boolean isThreadPooling(AnnotatedElement e) {
        return check(e, ThreadPooling.class);
    }

    public static void requireThreadPooling(AnnotatedElement e) {
        if (!check(e, ThreadPooling.class))
            throw new IllegalArgumentException("object is not a ThreadPooling-Argument.");
    }

    public static Parallel getParallelMarkup(AnnotatedElement c) {
        return get(c, Parallel.class);
    }

    public static boolean isParallel(AnnotatedElement e) {
        return check(e, Parallel.class);
    }

    public static void requireParallel(AnnotatedElement e) {
        if (!check(e, Parallel.class))
            throw new IllegalArgumentException("object is not a Parallel-Markup.");
    }

    public static SupplyParallel getSupplyParallelMarkup(AnnotatedElement c) {
        return get(c, SupplyParallel.class);
    }

    public static boolean isSupplyParallel(AnnotatedElement e) {
        return check(e, SupplyParallel.class);
    }

    public static void requireSupplyParallel(AnnotatedElement e) {
        if (!check(e, SupplyParallel.class))
            throw new IllegalArgumentException("object is not a SupplyParallel.Markup.");
    }

    public static TypeServer getTypeServerMarkup(AnnotatedElement c) {
        return get(c, TypeServer.class);
    }

    public static boolean isTypeServer(AnnotatedElement e) {
        return check(e, TypeServer.class);
    }

    public static void requireTypeServer(AnnotatedElement e) {
        if (!check(e, TypeServer.class))
            throw new IllegalArgumentException("object is not a TypeServer.");
    }

    public static Param getParamMarkup(AnnotatedElement c) {
        return get(c, Param.class);
    }

    public static boolean isParam(AnnotatedElement e) {
        return check(e, Param.class);
    }

    public static void requireParam(AnnotatedElement e) {
        if (!check(e, Param.class))
            throw new IllegalArgumentException("object is not a Param.");
    }

    public static void requireRequestHandler(AnnotatedElement e) {
        if (!check(e, RequestHandler.class))
            throw new IllegalArgumentException("object is not a RequestHandler.");
    }
}
