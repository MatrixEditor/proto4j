package de.proto4j.common.io; //@date 29.12.2021

import de.proto4j.common.*;
import de.proto4j.common.annotation.AnnotatedElement;
import de.proto4j.common.annotation.AnnotationUtil;
import de.proto4j.common.annotation.Item;
import de.proto4j.common.exception.IProtocolException;
import de.proto4j.common.exception.ProtocolClassException;
import de.proto4j.common.exception.ProtocolItemNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class NetworkServer implements ProtocolFactory.FactorySocketHolder, AnnotatedElement {

    private final ProtocolUtil protocolUtil = new ProtocolUtil();

    @Item(name = "protocol.reference", hasSetter = false)
    private final Protocol protocolReference;

    @Item(name = "threadPool", hasSetter = false)
    private final ForkJoinPool threadPool = new ForkJoinPool();

    @Item(name = "server.socket", hasSetter = false)
    private Object serverSocket;

    @Item(name = "handler.pool", hasSetter = false)
    private final Map<Object, Object> handlerPool = new HashMap<>();

    public NetworkServer(Object protocolReference_) throws IProtocolException {
        if (getProtocolUtil().isProtocol(protocolReference_)) {
            protocolReference = (Protocol) protocolReference_;
        } else throw new IProtocolException();
    }

    public void setSocket(Object... params) throws IProtocolException, NoSuchMethodException, IllegalAccessException {
        if (this.serverSocket == null)
            this.serverSocket = ProtocolFactory.createServerSocket(getProtocolReference(), params);
    }

    public void run() throws ProtocolItemNotFoundException, IllegalAccessException {
        if (serverSocket != null) {
            do {
                try {
                    Object networkSocket = getProtocolUtil().accept(serverSocket);
                    //maybe add listener here
                    PrintService.log(PrintColor.BRIGHT_BLACK, "New Connection!");

                    Class<?> x = AnnotationUtil.get("client.handler", serverSocket);
                    if (x != null) {
                        ProtocolFactory.FactoryClientHandler<?> h = (ProtocolFactory.FactoryClientHandler<?>) getProtocolUtil().newObject(x, networkSocket);
                        handlerPool.put(networkSocket, h);
                        execute(h::loop, networkSocket);
                    } else {
                        ProtocolFactory.FactoryNetworkHandler nh = AnnotationUtil.get("network.handler", serverSocket);
                        handlerPool.put(networkSocket, nh);
                        if (nh != null) execute(() -> nh.loop(networkSocket), networkSocket);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (AnnotationUtil.get("alive", serverSocket));
        } else throw new NullPointerException("ServerSocket not set!");
    }

    private void execute(ProtocolFactory.FactoryThrowableRunnable r, Object networkSocket) {
        threadPool.execute(() -> {
            try {
                r.run();
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    InvocationTargetException ex = (InvocationTargetException) e;
                    if (ex.getTargetException() instanceof SocketTimeoutException
                            || ex.getTargetException() instanceof SocketException) try {
                        getProtocolUtil().close(networkSocket);
                        handlerPool.remove(networkSocket);

                        PrintService.log(PrintColor.BRIGHT_BLACK, "Connection closed!");
                    } catch (ReflectiveOperationException | ProtocolClassException ignored) {
                    }
                    else PrintService.logError(
                            (Exception) ((InvocationTargetException) e).getTargetException(),
                            PrintColor.DARK_RED);
                } else PrintService.logError(e, PrintColor.DARK_RED);
            }
        });
    }

    public Protocol getProtocolReference() {
        return protocolReference;
    }

    private ProtocolUtil getProtocolUtil() {
        return protocolUtil;
    }

}
