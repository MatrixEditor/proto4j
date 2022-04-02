package de.proto4j.network.objects; //@date 08.02.2022

import de.proto4j.internal.io.KeyBasedWriter;
import de.proto4j.serialization.DescProviderFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.Key;

public class ObjectWriter extends KeyBasedWriter {

    public static final char END_BYTE = '\t';

    public ObjectWriter(SocketChannel chan, Key key) {
        super(chan, key);
    }

    @Override
    public void write(Object message) throws IOException {
        if (isClosed()) throw new IOException("stream is closed");

        if (message == null) throw new NullPointerException("cannot write null-object");
        //try {
            StringBuffer buf = DescProviderFactory.allocate(message);
            /*
            if (buf.capacity() % 16 != 0) {
                int cap = buf.capacity();
                while (cap % 16 != 0) {
                    buf.append(RF);
                    cap++;
                }
            }


            Cipher    c   = Proto4jAsymKeyProvider.getCipherInstance();
            c.init(Cipher.ENCRYPT_MODE, getKey());

            c.update(buf.toString().getBytes(StandardCharsets.UTF_16));

             */
            buf.append(END_BYTE);
            byte[] bytes = buf.toString().getBytes();
            write(bytes);
            /*
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("could not write data!");
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
             */
    }
}
