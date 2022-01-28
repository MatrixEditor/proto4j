package de.proto4j.network.objects; //@date 28.01.2022

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public abstract class ObjectExchange {

    protected ObjectExchange() {}

    public abstract void close();

    public abstract InputStream getRequestBody();

    public abstract OutputStream getResponseBody();

    public abstract InetSocketAddress getRemoteAddress();

    public abstract InetSocketAddress getLocalAddress();

    protected abstract void setStreams(InputStream i, OutputStream o);

    public abstract ObjectPrincipal getPrincipal();

    public abstract ObjectContext<?> getContext();

}
