package de.proto4j.security.cert; //@date 08.02.2022

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class CertificateExchange implements Closeable {

    private final SocketChannel channel;

    private volatile boolean closed = false;

    public CertificateExchange(SocketChannel channel) {
        this.channel = channel;
    }

    public abstract void init() throws IOException;

    public abstract CertificateSpec exchange() throws IOException, ClassNotFoundException;

    public abstract void close() throws IOException;

    public abstract void reset() throws IOException;

    public SocketChannel getChannel() {
        return channel;
    }

    public boolean isClosed() {
        return closed;
    }

    protected void setClosed(boolean closed) {
        this.closed = closed;
    }
}
