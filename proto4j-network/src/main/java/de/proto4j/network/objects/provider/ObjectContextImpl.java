package de.proto4j.network.objects.provider; //@date 28.01.2022

import de.proto4j.network.objects.ObjectAuthenticator;
import de.proto4j.network.objects.ObjectContext;

import java.util.HashMap;
import java.util.Map;

class ObjectContextImpl<E> implements ObjectContext<E> {

    private final ObjectServer server;
    private final Map<String, Object> conf = new HashMap<>();

    private final E mapping;

    private ObjectAuthenticator authenticator;

    private Handler handler;

    public ObjectContextImpl(E mapping, Handler handler, ObjectServer server) {
        this.server = server;
        if (mapping == null || handler == null || server == null) {
            throw new NullPointerException("Mapping, Handler or Server == null");
        }
        this.mapping = mapping;
        this.handler = handler;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(Handler handler) {
        if (handler == null) throw new NullPointerException("Handler == null");
        if (this.handler != null) throw new IllegalArgumentException("Handler already set!");
        this.handler = handler;
    }

    @Override
    public E getMapping() {
        return mapping;
    }

    public ObjectAuthenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public ObjectServer getServer() {
        return server;
    }

    @Override
    public Map<String, Object> attributes() {
        return conf;
    }

    public void setAuthenticator(ObjectAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
}
