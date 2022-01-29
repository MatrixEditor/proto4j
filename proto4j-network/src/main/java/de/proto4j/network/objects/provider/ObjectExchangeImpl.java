package de.proto4j.network.objects.provider; //@date 28.01.2022

import de.proto4j.network.objects.ObjectConnection;
import de.proto4j.network.objects.ObjectContext;
import de.proto4j.network.objects.ObjectExchange;
import de.proto4j.network.objects.ObjectPrincipal;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

class ObjectExchangeImpl extends ObjectExchange {

    private final ObjectConnection connection;
    private final Object           message;

    private InputStream  ris;
    private OutputStream ros;

    private boolean closed;

    private ObjectPrincipal principal;

    public ObjectExchangeImpl(ObjectConnection connection, Object message) {
        this.connection = connection;
        this.message    = message;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        connection.close();
    }

    @Override
    public InputStream getRequestBody() {
        return ris;
    }

    @Override
    public OutputStream getResponseBody() {
        return ros;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        Socket s = connection.getChannel().socket();
        return new InetSocketAddress(s.getInetAddress(), s.getPort());
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        Socket s = connection.getChannel().socket();
        return new InetSocketAddress(s.getLocalAddress(), s.getLocalPort());
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        ris = i;
        ros = o;
    }

    @Override
    public ObjectPrincipal getPrincipal() {
        return principal;
    }

    public void setPrincipal(ObjectPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public ObjectContext<?> getContext() {
        return connection.getContext();
    }

    @Override
    public Object getMessage() {
        return message;
    }
}
