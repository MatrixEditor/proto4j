package de.proto4j.network.objects; //@date 28.01.2022

import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ObjectConnection {

    private ObjectContext<?> context;

    private Proto4jReader raw;
    private Proto4jWriter out; //raw

    private SocketChannel chan;

    private volatile long creationTime;

    private long time;

    private boolean closed = false;

    public void setParameters(Proto4jWriter rawOut, SocketChannel c, Proto4jReader rawIn,
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

    public Proto4jReader getReader() {
        return raw;
    }

    public Proto4jWriter getWriter() {
        return out;
    }

    public ObjectContext<?> getContext() {
        return context;
    }

    public void setRawInput(Proto4jReader raw) {
        this.raw = raw;
    }

    public void setRawOutput(Proto4jWriter out) {
        this.out = out;
    }

    public void setContext(ObjectContext<?> context) {
        this.context = context;
    }
}
