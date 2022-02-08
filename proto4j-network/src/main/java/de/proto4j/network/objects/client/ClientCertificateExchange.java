package de.proto4j.network.objects.client; //@date 08.02.2022

import de.proto4j.security.cert.CertificateExchange;
import de.proto4j.security.cert.CertificateSpec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.SocketChannel;

final class ClientCertificateExchange extends CertificateExchange {

    private ObjectInputStream oin;

    public ClientCertificateExchange(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void init() throws IOException {
        if (oin == null && !isClosed()) {
            synchronized (ClientCertificateExchange.class) {
                oin = new ObjectInputStream(getChannel().socket().getInputStream());
            }
        }
    }

    @Override
    public CertificateSpec exchange() throws IOException, ClassNotFoundException {
        if (!isClosed()) {
            if (!getChannel().isOpen()) {
                throw new IOException("channel is closed!");
            }
            Object o = oin.readObject();
            if (!(o instanceof CertificateSpec)) {
                throw new IOException("could not read object");
            }
            return (CertificateSpec) o;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (!isClosed()) {
            setClosed(true);
            oin = null;
        }
    }

    @Override
    public void reset() throws IOException {
        if (isClosed()) {
            setClosed(false);
            init();
        }
    }
}
