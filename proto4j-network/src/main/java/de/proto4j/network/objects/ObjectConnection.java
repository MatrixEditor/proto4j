package de.proto4j.network.objects; //@date 28.01.2022

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

public class ObjectConnection {

    private ObjectContext<?> context;

    private InputStream  raw;
    private OutputStream out; //raw

    private SocketChannel chan;

    private volatile long creationTime;

    private long time;

    private boolean closed = false;

    public void setParameters(OutputStream rawOut, SocketChannel c, InputStream rawIn,
                              ObjectContext<?> ctx) {
        this.chan = c;
        this.raw = rawIn;
        this.out = rawOut;
        this.context = ctx;
    }

    public SocketChannel getChannel() {
        return chan;
    }

    public void setChannel(SocketChannel chan) {
        this.chan = chan;
    }

    public synchronized void close() {
        if (closed) return;

        closed = true;
        if (!chan.isOpen()) return;

        closeStream(raw);
        closeStream(out);
        closeStream(chan);
    }

    private void closeStream(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            //log
        }
    }

    public InputStream getInputStream() {
        return raw;
    }

    public OutputStream getOutputStream() {
        return out;
    }

    public ObjectContext<?> getContext() {
        return context;
    }

    public void setRawInput(InputStream raw) {
        this.raw = raw;
    }

    public void setRawOutput(OutputStream out) {
        this.out = out;
    }

    public void setContext(ObjectContext<?> context) {
        this.context = context;
    }
}
