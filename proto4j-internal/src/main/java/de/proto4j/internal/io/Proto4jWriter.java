package de.proto4j.internal.io; //@date 28.01.2022

import de.proto4j.annotation.documentation.Info;
import de.proto4j.annotation.documentation.UnsafeOperation;
import de.proto4j.internal.io.desc.DescProviderFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.kerberos.KerberosKey;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import static de.proto4j.internal.io.desc.DescProviderFactory.RF;

public class Proto4jWriter extends OutputStream {
    private final SocketChannel channel;
    private final byte[]        one;

    private ByteBuffer buf;
    private boolean    closed;

    @UnsafeOperation
    @Info("will be generated at runtime in the future")
    public static final byte[] SHARED_KEY = new byte[] {
            37, 100, 79, 6, -99, 30, 78, 33, -44, 126, -34, 35, -126, 109, -101, 85
    };

    public static final String SHARED_CIPHER = "AES/ECB/PKCS5Padding";

    public Proto4jWriter(SocketChannel chan) {
        if (chan == null) throw new NullPointerException();
        this.channel = chan;

        closed = false;
        one    = new byte[1];
        buf    = ByteBuffer.allocate(4096);
    }

    public synchronized void write(Object message) throws IOException {
        if (closed) throw new IOException("stream is closed");

        if (message == null) throw new NullPointerException("cannot write null-object");
        try {
            StringBuffer buf = DescProviderFactory.allocate(message);
            if (buf.capacity() % 16 != 0) {
                int cap = buf.capacity();
                while (cap % 16 != 0) {
                    buf.append(RF);
                    cap++;
                }
            }
            Cipher c = Cipher.getInstance(SHARED_CIPHER);
            SecretKey key = new SecretKeySpec(SHARED_KEY, "AES");
            c.init(Cipher.ENCRYPT_MODE, key);

            byte[] bytes = c.doFinal(buf.toString().getBytes());
            write(bytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            // log
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException("could not write data!");
        }
    }

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
