package de.proto4j.network;//@date 12.02.2022

import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.SimpleBeanCache;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.TypeContext;
import de.proto4j.network.objects.client.ObjectClient;
import de.proto4j.stream.InterruptedStream;
import de.proto4j.stream.SequenceStream;
import de.proto4j.stream.Streams;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

public interface EnvironmentBuilder {

    InterruptedStream<SimpleBeanCache> buildInterrupted();

    ObjectContext.Handler buildHandler(Method m, ObjectClient oc, SimpleBeanCache sbc);

    void buildContext(SimpleBeanCache cache, ObjectClient ref);

    ExecutorService buildThreadPool(Class<?> c);

    SequenceStream<Class<?>> buildBeans(boolean ignoreValues);

    BeanManager buildManager();

    ObjectClient buildBase();

    TypeContext finish();

    default void buildConfiguration() {}

    default void iterCaches(SequenceStream<SimpleBeanCache> stream,
                            InterruptedStream<SimpleBeanCache> interruptedStream) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(interruptedStream);

        stream.forEach(interruptedStream::yield);
    }

    default SequenceStream<Class<?>> buildBeans() {
        return buildBeans(true);
    }

    default SequenceStream<Method> buildRequestHandlers(Class<?> c, Predicate<Method> predicate) {
        return Streams.prepare(c.getDeclaredMethods()).slice(predicate);
    }

}
