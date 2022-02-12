package de.proto4j.network.objects; //@date 28.01.2022

import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;

import java.net.InetSocketAddress;

public abstract class ObjectExchange {

    protected ObjectExchange() {}

    public abstract void close();

    public abstract Proto4jReader getRequestBody();

    public abstract Proto4jWriter getResponseBody();

    public abstract InetSocketAddress getRemoteAddress();

    public abstract InetSocketAddress getLocalAddress();

    protected abstract void setStreams(Proto4jReader i, Proto4jWriter o);

    public abstract ObjectPrincipal getPrincipal();

    public abstract ObjectContext<?> getContext();

    public abstract Object getMessage();

}
