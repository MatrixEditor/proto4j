package de.prototest.messages.client; //@date 05.02.2022

import de.proto4j.annotation.AnnotationLookup;
import de.proto4j.annotation.server.Configuration;
import de.proto4j.annotation.server.TypeClient;
import de.proto4j.network.objects.client.ClientContext;
import de.proto4j.network.objects.client.ClientProvider;

import java.io.IOException;
import java.net.InetSocketAddress;

@TypeClient
@Configuration({AnnotationLookup.CONF_BY_CONNECTION, AnnotationLookup.CONF_IGNORE_VALUES})
public class SimpleClient {

    public static void main(String[] args) throws IOException {
        ClientContext ctx = ClientProvider.createClient(SimpleClient.class);
        ctx.getClient().connectTo(new InetSocketAddress("127.0.0.1", 3333));

    }
}