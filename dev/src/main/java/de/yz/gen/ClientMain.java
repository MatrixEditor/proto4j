package de.yz.gen; //@date 29.01.2022

import java.io.IOException;

// TODO: 29.01.2022 Add annotations for client
public class ClientMain {

    public static void main(String[] args) throws IOException {
        HelloWorldClient client = new HelloWorldClient();

        //client.makeConnection(null);
        client.send(null);
        HelloWorldMessage m = client.receive();
    }
}
