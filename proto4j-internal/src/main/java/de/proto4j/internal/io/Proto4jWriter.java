package de.proto4j.internal.io; //@date 28.01.2022

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Proto4jWriter extends OutputStream {

    private final SocketChannel channel;
    private final byte[]        one;
    private       ByteBuffer    buf;
    private       boolean       closed;

    public Proto4jWriter(SocketChannel chan) {
        if (chan == null) throw new NullPointerException();
        this.channel = chan;

        closed = false;
        one    = new byte[1];
        buf    = ByteBuffer.allocate(4096);
    }

    public boolean isClosed() {
        return closed;
    }

    public abstract void write(Object message) throws IOException;

    @Override
    public synchronized void write(int b) throws IOException {
        one[0] = (byte) b;
        write(one, 0, 1);
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (closed) throw new IOException("Stream is closed!");

        int cap = buf.capacity();
        if (cap < len) {
            buf = ByteBuffer.allocate(2 * (cap + (len - cap)));
        }
        buf.clear();
        buf.put(b, off, len);
        buf.flip();

        int n, l = len;
        while ((n = channel.write(buf)) < l) {
            l -= n;
            if (l == 0) return;
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        channel.close();
        closed = true;
    }
}
