package de.proto4j.network.objects; //@date 08.02.2022

import de.proto4j.internal.io.KeyBasedReader;
import de.proto4j.security.asymmetric.Proto4jAsymKeyProvider;
import de.proto4j.serialization.DescProviderFactory;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Collection;

public class ObjectReader extends KeyBasedReader {

    public ObjectReader(SocketChannel chan, Collection<Class<?>> readableClasses, Key key) {
        super(chan, readableClasses, key);
    }

    @Override
    public Object readMessage() throws IOException {
        if (isClosed()) throw new IOException("stream is closed");
        ByteBuffer buffer = ByteBuffer.allocate(2048);

        int len = read(buffer.array());
        try {
            byte[] encrypted = new byte[len];
            System.arraycopy(buffer.array(), 0, encrypted, 0, len);

            Cipher cipher = Proto4jAsymKeyProvider.getCipherInstance();
            cipher.init(Cipher.DECRYPT_MODE, getKey());

            byte[] decrypted = cipher.doFinal(encrypted);
            return DescProviderFactory.convert(decrypted, getReadable());
        } catch (GeneralSecurityException e) {
            //log
        }
        return null;
    }
}
