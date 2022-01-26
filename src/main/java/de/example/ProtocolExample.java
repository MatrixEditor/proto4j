package de.example; //@date 01.01.2022

import de.proto4j.common.PrintColor;
import de.proto4j.common.PrintService;
import de.proto4j.common.Protocol;
import de.proto4j.common.ProtocolFactory;
import de.proto4j.common.ProtocolFactory.ServerBuilder;
import de.proto4j.common.exception.IProtocolException;
import de.proto4j.common.exception.ProtocolItemNotFoundException;
import de.proto4j.common.io.NetworkClient;
import de.proto4j.common.io.NetworkServer;
import de.proto4j.common.io.ObjectServerSocket;
import de.proto4j.common.io.ObjectSocket;

import java.io.IOException;
import java.io.Serializable;

import static de.proto4j.common.ProtocolFactory.ProtocolBuilder.newInstance;
import static de.proto4j.common.ProtocolFactory.ProtocolBuilder.newProtocol;

public class ProtocolExample {

    public static final Protocol protocol = newProtocol("name", NetworkClient.class, ObjectSocket.class,
                                                  ObjectServerSocket.class, NetworkServer.class, null, MyPacket.class);

    public static void main(String[] args) throws IProtocolException, ReflectiveOperationException, IOException {
        ProtocolFactory.ProtocolBuilder builder = newInstance();

        Protocol protocol1 = builder.setName("example")
                          // same as addAttribute(Protocol.SOCKET_ATTRIBUTE, ObjectSocket.class);
                          .setSocket(ObjectSocket.class)
                          .setClient(NetworkClient.class)
                          //object has to implement Serializable and has to be reachable from outside
                          .setPacketType(MyPacket.class)
                          .setServerSocket(ObjectServerSocket.class)
                          .setServer(NetworkServer.class)
                          .create();

        // create a Server (usually it should start before the client connects to it)
        NetworkServer ns = ProtocolFactory.createServer(protocol, protocol);
        ns.setSocket(9999);
        ServerBuilder.from(ns).setClientHandler(MyHandler.class);

        new Thread(() -> {
            try {
                ns.run();
            } catch (ProtocolItemNotFoundException | IllegalAccessException e) {
                PrintService.logError(e, PrintColor.DARK_RED);
            }
        }).start();

        NetworkClient nc = ProtocolFactory.createClient(protocol, protocol);
        nc.setSocket("127.0.0.1", 9999);

        // send packets
        MyPacket packet = new MyPacket();
        nc.send(packet);
    }

    public static class MyHandler extends ProtocolFactory.FactoryClientHandler<ObjectSocket> {

        private final ObjectSocket socket;

        public MyHandler(ObjectSocket socket) {
            super(socket); //this call has no side effects
            this.socket = socket;
        }

        @Override
        public void loop() throws Exception {
            //...
            NetworkClient client = NetworkClient.of(protocol, socket);
            while (true) {
                MyPacket packet = client.receiveAndRead(null);
                System.out.println(packet.getMsg());
            }
        }
    }

    public static class MyPacket implements Serializable {

        public String getMsg() {
            return "Hello World";
        }
    }

}
