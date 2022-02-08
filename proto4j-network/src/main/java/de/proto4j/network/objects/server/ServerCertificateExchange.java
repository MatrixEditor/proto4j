package de.proto4j.network.objects.server; //@date 08.02.2022

import de.proto4j.security.cert.CertificateExchange;
import de.proto4j.security.cert.CertificateSpec;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.SocketChannel;

final class ServerCertificateExchange extends CertificateExchange {

    private final CertificateSpec certificateSpec;

    private ObjectOutputStream out;

    public ServerCertificateExchange(SocketChannel channel, final CertificateSpec spec) {
        super(channel);
        certificateSpec = spec;
    }

    @Override
    public void init() throws IOException {
        if (out == null && !isClosed()) {
            synchronized (ServerCertificateExchange.class) {
                out = new ObjectOutputStream(getChannel().socket().getOutputStream());
            }
        }
    }

    @Override
    public CertificateSpec exchange() throws IOException, ClassNotFoundException {
        if (!isClosed()) {
            if (!getChannel().isOpen()) {
                throw new IOException("channel is closed!");
            }

            out.writeObject(certificateSpec);
            return certificateSpec;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (!isClosed()) {
            setClosed(true);
            out = null;
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
