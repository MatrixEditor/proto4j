package de.proto4j.internal.io; //@date 28.01.2022

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Proto4jReader extends InputStream {

    public static final int BUF_SIZE = 8 * 1024;
    private SocketChannel channel;
    private ByteBuffer    buf;
    private byte[] one;
    private boolean reset;
    private boolean closed = false, eof = false;

    public Proto4jReader(SocketChannel chan) {
        if (chan == null) throw new NullPointerException();
        this.channel = chan;
        this.buf     = ByteBuffer.allocate(BUF_SIZE);
        buf.clear();

        one    = new byte[1];
        closed = reset = false;
    }

    @Override
    public synchronized int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read() throws IOException {
        int result = read(one, 0, 1);
        return result == 1 ? one[0] & 0xFF : -1;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed!");
        }

        if (eof) return -1;

        assert channel.isBlocking();

        if (off < 0 || len < 0 || len > (b.length - off)) {
            throw new IndexOutOfBoundsException();
        }

        buf.clear();
        if (len < BUF_SIZE) {
            buf.limit(len);
        }
        int returnLen;
        do {
            returnLen = channel.read(buf);
        } while (returnLen == 0);

        if (returnLen == -1) {
            eof = true;
            return -1;
        }

        buf.flip();
        buf.get(b, off, returnLen);
        return returnLen;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized int available() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed!");
        }

        if (eof) return -1;

        return buf.remaining();
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        channel.close();
        closed = true;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (closed) return;
        reset = true;

    }
}
