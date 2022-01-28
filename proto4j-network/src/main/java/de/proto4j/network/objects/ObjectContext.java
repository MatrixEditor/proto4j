package de.proto4j.network.objects;//@date 28.01.2022

import de.proto4j.network.objects.provider.ObjectServer;

import java.util.Map;

public interface ObjectContext<E> {

    E getMapping();

    ObjectServer getServer();

    Map<String, Object> attributes();

    public interface Handler {

        public void handle(ObjectExchange exchange);
    }

    Handler getHandler();

    void setHandler(Handler handler);

}
