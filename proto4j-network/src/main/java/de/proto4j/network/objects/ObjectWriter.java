package de.proto4j.network.objects; //@date 08.02.2022

import de.proto4j.internal.io.KeyBasedWriter;
import de.proto4j.security.asymmetric.Proto4jAsymKeyProvider;
import de.proto4j.serialization.desc.DescProviderFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import static de.proto4j.serialization.desc.DescProviderFactory.RF;

public class ObjectWriter extends KeyBasedWriter {

    public ObjectWriter(SocketChannel chan, Key key) {
        super(chan, key);
    }

    @Override
    public void write(Object message) throws IOException {
        if (isClosed()) throw new IOException("stream is closed");

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
            Cipher    c   = Proto4jAsymKeyProvider.getCipherInstance();
            c.init(Cipher.ENCRYPT_MODE, getKey());

            byte[] bytes = c.doFinal(buf.toString().getBytes());
            write(bytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            // log
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException("could not write data!");
        }
    }
}
