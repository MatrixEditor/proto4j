package de.prototest.echo.client; //@date 30.01.2022

import de.proto4j.annotation.AnnotationLookup;
import de.proto4j.annotation.server.Configuration;
import de.proto4j.annotation.server.TypeClient;
import de.proto4j.network.objects.ObjectConnection;
import de.proto4j.network.objects.client.ClientContext;
import de.proto4j.network.objects.client.ClientProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;

@TypeClient
@Configuration({AnnotationLookup.CONF_BY_CONNECTION, AnnotationLookup.CONF_IGNORE_VALUES})
public class EchoClient {

    public static void main(String[] args) throws IOException {
        ClientContext ctx = ClientProvider.createClient(EchoClient.class);

        //connect to server
        ctx.getClient().connectTo(new InetSocketAddress("127.0.0.1", 3333));
        Collection<ObjectConnection> c = ctx.getClient().getAllConnections();
        System.out.println();
    }
}
