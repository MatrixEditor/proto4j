package de.yz.gen; //@date 29.01.2022

import de.proto4j.internal.io.Proto4jReader;
import de.proto4j.internal.io.Proto4jWriter;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;

public class HelloWorldClient {

    private final SocketChannel chan;
    private final Proto4jWriter writer;
    private final Proto4jReader reader;

    public HelloWorldClient() throws IOException {
        chan   = SocketChannel.open();
        reader = new Proto4jReader(chan, List.of(HelloWorldMessage.class));
        writer = new Proto4jWriter(chan);
    }

    public void makeConnection(SocketAddress remote) throws IOException {
        chan.socket().connect(remote);
    }

    public void send(HelloWorldMessage message) throws IOException {
        writer.write(message);
    }

    public HelloWorldMessage receive() throws IOException {
        return (HelloWorldMessage) reader.readMessage();
    }

}
