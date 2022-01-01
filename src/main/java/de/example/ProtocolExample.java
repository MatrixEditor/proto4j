package de.example; //@date 01.01.2022

import de.proto4j.common.Protocol;
import de.proto4j.common.ProtocolFactory;
import de.proto4j.common.ProtocolFactory.ServerBuilder;
import de.proto4j.common.exception.IProtocolException;
import de.proto4j.common.io.NetworkClient;
import de.proto4j.common.io.NetworkServer;
import de.proto4j.common.io.ObjectServerSocket;
import de.proto4j.common.io.ObjectSocket;

import java.io.Serializable;
import java.net.UnknownHostException;

public class ProtocolExample {
    public static void main(String[] args) throws IProtocolException, ReflectiveOperationException {
        ProtocolFactory.ProtocolBuilder builder = ProtocolFactory.ProtocolBuilder.newInstance();

        Protocol p = builder.setName("example")
                            // same as addAttribute(Protocol.SOCKET_ATTRIBUTE, ObjectSocket.class);
                            .setSocket(ObjectSocket.class)
                            .setClient(NetworkClient.class)
                            //object has to implement Serializable and has to be reachable from outside
                            .setPacketType(MyPacket.class)
                            .setServerSocket(ObjectServerSocket.class)
                            .setServer(NetworkServer.class)
                            .create();

        NetworkClient nc = ProtocolFactory.createClient(p, p);
        nc.setSocket("127.0.0.1", 9999);

        MyPacket packet = new MyPacket();
        nc.send(packet);

        NetworkServer ns = ProtocolFactory.createServer(p, p);
        ns.setSocket(9999);

        ServerBuilder.from(ns)
                     .setClientHandler(MyHandler.class)
                     //or
                     .setNetworkHandler((socket) -> {
                         //implement loop method here
                     });

        ns.run();

    }

    public static class MyHandler extends ProtocolFactory.FactoryClientHandler<ObjectSocket> {

        public MyHandler(ObjectSocket socket) {
            super(socket); //this call has no side effects
        }

        @Override
        public void loop() throws Exception {
            //...
        }
    }

    public static class MyPacket implements Serializable {

        public String getMsg() {
            return "Hello World";
        }
    }

}
