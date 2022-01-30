package de.prototest.echo.client; //@date 30.01.2022

import de.proto4j.annotation.server.Configuration;
import de.proto4j.annotation.server.TypeClient;
import de.proto4j.network.objects.client.ClientContext;
import de.proto4j.network.objects.client.ClientProvider;

import java.net.InetSocketAddress;

@TypeClient
@Configuration({Configuration.BY_CONNECTION, Configuration.IGNORE_VALUES})
public class EchoClient {

    public static void main(String[] args) {
        ClientContext ctx = ClientProvider.createClient(EchoClient.class);

        //connect to server
        ctx.getClient().connectTo(new InetSocketAddress("127.0.0.1", 3333));
    }
}
