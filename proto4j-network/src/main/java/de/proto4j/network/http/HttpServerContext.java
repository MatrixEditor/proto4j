package de.proto4j.network.http;//@date 27.01.2022

import com.sun.net.httpserver.HttpServer;
import de.proto4j.annotation.documentation.Info;
import de.proto4j.annotation.documentation.UnsafeOperation;
import de.proto4j.internal.model.bean.BeanManager;

import java.util.Set;
import java.util.concurrent.Executor;

public interface HttpServerContext {

    Class<?> getExecutorType();

    Class<?> getMainClass();

    @UnsafeOperation
    HttpServer getServer();

    @Info("UnmodifiableBeanManager")
    BeanManager getBeans();

    Set<String> getWebRoutes();

}
