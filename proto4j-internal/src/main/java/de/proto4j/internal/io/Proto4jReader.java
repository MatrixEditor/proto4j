package de.proto4j.internal.io; //@date 28.01.2022

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class Proto4jReader extends InputStream {

    public static final int BUF_SIZE = 8 * 1024;

    private final Set<Class<?>> readable = new HashSet<>();

    private final SocketChannel channel;
    private final ByteBuffer    buf;
    private final byte[]        one;

    private boolean reset;
    private boolean closed = false, eof = false;

    public Proto4jReader(SocketChannel chan, Collection<Class<?>> readableClasses) {
        if (chan == null) throw new NullPointerException();
        this.channel = chan;
        this.buf     = ByteBuffer.allocate(BUF_SIZE);
        buf.clear();

        one    = new byte[1];
        closed = reset = false;

        readable.addAll(readableClasses);
    }

    public synchronized Object readMessage() throws IOException {
        if (closed) throw new IOException("stream is closed");
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int c;
        do {
            c = read();
            buffer.put((byte) c);
        } while (c != -1);
        try {
            Cipher cipher = Cipher.getInstance(Proto4jWriter.SHARED_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Proto4jWriter.SHARED_KEY, "AES"));

            byte[] decrypted = cipher.doFinal(buffer.array());
            return IOUtil.convert(decrypted, readable);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            //log
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
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

    public Set<Class<?>> getReadable() {
        return readable;
    }
}
