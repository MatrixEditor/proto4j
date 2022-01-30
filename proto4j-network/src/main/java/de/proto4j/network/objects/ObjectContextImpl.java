package de.proto4j.network.objects; //@date 28.01.2022

import de.proto4j.network.objects.ObjectAuthenticator;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.client.ObjectClient;

import java.util.HashMap;
import java.util.Map;

public class ObjectContextImpl<E> implements ObjectContext<E> {

    private final ObjectClient        client;
    private final Map<String, Object> conf = new HashMap<>();

    private final E mapping;

    private ObjectAuthenticator authenticator;

    private Handler handler;

    public ObjectContextImpl(E mapping, Handler handler, ObjectClient client) {
        this.client = client;
        if (mapping == null || handler == null || client == null) {
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

    public void setAuthenticator(ObjectAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public ObjectClient getClient() {
        return client;
    }

    @Override
    public Map<String, Object> attributes() {
        return conf;
    }
}
