package de.proto4j.network.objects; //@date 28.01.2022

import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;

import java.net.InetSocketAddress;
import java.net.Socket;

public class ObjectExchangeImpl extends ObjectExchange {

    private final ObjectConnection connection;
    private final Object           message;

    private Proto4jReader  ris;
    private Proto4jWriter ros;

    private boolean closed;

    private ObjectPrincipal principal;

    public ObjectExchangeImpl(ObjectConnection connection, Object message) {
        this.connection = connection;
        this.message    = message;
        setStreams(connection.getReader(), connection.getWriter());
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
    public Proto4jReader getRequestBody() {
        return ris;
    }

    @Override
    public Proto4jWriter getResponseBody() {
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
    public void setStreams(Proto4jReader i, Proto4jWriter o) {
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
