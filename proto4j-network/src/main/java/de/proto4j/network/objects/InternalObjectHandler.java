package de.proto4j.network.objects; //@date 12.02.2022

import de.proto4j.annotation.Markup;
import de.proto4j.annotation.threding.ParallelExecutor;
import de.proto4j.annotation.threding.ParallelSupplier;
import de.proto4j.annotation.threding.Threads;
import de.proto4j.internal.io.Proto4jWriter;
import de.proto4j.internal.logger.Logger;
import de.proto4j.internal.logger.PrintColor;
import de.proto4j.internal.logger.PrintService;
import de.proto4j.internal.method.MethodLookup;
import de.proto4j.network.objects.client.ObjectClient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InternalObjectHandler implements ObjectContext.Handler {

    private static final Logger logger = PrintService.createLogger(InternalObjectHandler.class);

    private final ContextCache cache;
    private final ObjectClient client;

    public InternalObjectHandler(ContextCache cache, ObjectClient oc) {
        this.cache  = cache;
        this.client = oc;
    }

    @Override
    public void handle(ObjectExchange exchange) {
        if (exchange == null || cache == null || client == null) return;

        Method m = cache.getMethod();

        Object  response     = null;
        boolean fromParallel = Markup.isParallel(m) || Markup.isSupplyParallel(m);

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
            if (Markup.isParallel(m)) {
                ParallelExecutor pe = Threads.newParallelThreadExecutor();
                pe.execute(() -> invokeMethod(args, m));
            } else {
                ParallelSupplier ps = Threads.newSingleThreadSupplier();
                response = ps.supplyAsync(() -> invokeMethod(args, m));
            }
        }

        if (Markup.isConnectionHandler(m)) return;

        if (Markup.isResponseBody(m)) {
            // this check prevents exceptions to be thrown from the underlying Proto4jWriter.
            if (response != null && client.getMessageTypes().contains(response.getClass())) {
                try {
                    Proto4jWriter w = exchange.getResponseBody();
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
