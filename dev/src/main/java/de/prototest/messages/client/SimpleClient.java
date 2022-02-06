package de.prototest.messages.client; //@date 05.02.2022

import de.proto4j.annotation.server.Configuration;
import de.proto4j.annotation.server.TypeClient;
import de.proto4j.network.objects.client.ClientContext;
import de.proto4j.network.objects.client.ClientProvider;

import java.net.InetSocketAddress;

@TypeClient
@Configuration({Configuration.BY_CONNECTION, Configuration.IGNORE_VALUES})
public class SimpleClient {

    public static void main(String[] args) {
        ClientContext ctx = ClientProvider.createClient(SimpleClient.class);
        ctx.getClient().connectTo(new InetSocketAddress("127.0.0.1", 3333));

    }
}