package de.proto4j.network.objects;//@date 28.01.2022

import de.proto4j.network.objects.client.ObjectClient;

import java.util.Map;

public interface ObjectContext<E> {

    E getMapping();

    ObjectClient getClient();

    Map<String, Object> attributes();

    Handler getHandler();

    void setHandler(Handler handler);

    public interface Handler {

        public void handle(ObjectExchange exchange);
    }

}
