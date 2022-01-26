package de.proto4j.common; //@date 29.12.2021

import de.proto4j.common.annotation.AnnotationUtil;
import de.proto4j.common.exception.IProtocolException;
import de.proto4j.common.exception.ProtocolItemNotFoundException;
import de.proto4j.common.io.NetworkServer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Simply a utility class create packets, sockets, clients, server-sockets,
 * server and protocols by a {@link ProtocolBuilder}.
 *
 * @see Protocol
 * @see ProtocolUtil
 */
public final class ProtocolFactory {

    private ProtocolFactory() {}

    public static <P_OUT> P_OUT createPacket(Protocol protocol, Object... params) throws IProtocolException,
            NoSuchMethodException, IllegalAccessException {
        return createUnknown(protocol, Protocol.PACKET_ATTRIBUTE, params);
    }

    public static <P_OUT> P_OUT createSocket(Protocol protocol, Object... params) throws IProtocolException,
            NoSuchMethodException, IllegalAccessException {
        return createUnknown(protocol, Protocol.SOCKET_ATTRIBUTE, params);
    }

    public static <P_OUT> P_OUT createClient(Protocol protocol, Object... params) throws IProtocolException,
            NoSuchMethodException, IllegalAccessException {
        return createUnknown(protocol, Protocol.CLIENT_ATTRIBUTE, params);
    }

    public static <P_OUT> P_OUT createServer(Protocol protocol, Object... params) throws IProtocolException,
            NoSuchMethodException, IllegalAccessException {
        return createUnknown(protocol, Protocol.SERVER_ATTRIBUTE, params);
    }

    public static <P_OUT> P_OUT createServerSocket(Protocol protocol, Object... params) throws IProtocolException,
            NoSuchMethodException, IllegalAccessException {
        return createUnknown(protocol, Protocol.SERVER_SOCKET_ATTRIBUTE, params);
    }

    @SuppressWarnings("unchecked")
    private static <OUT> OUT createUnknown(Protocol protocol, String attrName, Object... params)
            throws IProtocolException, NoSuchMethodException, IllegalAccessException {
        Map<String, Class<?>> attrs      = protocol.get("attributes");
        Class<OUT>            p_outClass = null;
        if (attrs.containsKey(attrName)) {
            p_outClass = (Class<OUT>) attrs.get(attrName);
        }

        if (p_outClass == null)
            throw new IProtocolException("Packet-Attribute not found!");

        return ProtocolUtil.getInstance().newObject(p_outClass, params);
    }

    public static interface FactoryPacketReader<T, R> {
        default R readPacket(T t) throws Exception {return null;}
    }

    public static interface FactoryServerSocket<SOCK> {
        default SOCK accept() throws Exception {return null;}
    }

    public static interface FactorySocketHolder {
        void setSocket(Object... params) throws Exception;
    }

    public static interface FactoryClient extends FactorySocketHolder {
        <P_OUT> P_OUT receiveAndRead(Object reader, Object... params) throws Exception;

        <R> R send(Object... params) throws Exception;

        <R> R close(Object... params) throws Exception;

        <R> R receive(Object... params) throws Exception;
    }

    public static interface FactoryNetworkHandler {

        void loop(Object socket) throws Exception;
    }

    public static interface FactoryThrowableRunnable {
        void run() throws Exception;
    }

    public static abstract class FactoryClientHandler<T> {

        public FactoryClientHandler(T socket) {}

        public abstract void loop() throws Exception;
    }

    public static class ProtocolBuilder {

        private final Map<String, Class<?>> attributes = new HashMap<>();

        private String name;

        public static ProtocolBuilder newInstance() {
            return new ProtocolBuilder();
        }

        public static Protocol newProtocol(String name, Class<?> client, Class<?> socket, Class<?> s_socket,
                                           Class<?> server, Class<?> packetReader, Class<?> packetType) {
            return newProtocol(name, client, socket, s_socket, server, packetReader, packetType, null, null);
        }

        public static Protocol newProtocol(String name,
                                           Class<?> client, Class<?> socket, Class<?> s_socket, Class<?> server,
                                           Class<?> packetReader, Class<?> packetType, Class<?> payloadType,
                                           Class<?> headerType) {
            return newInstance().setClient(client).setPayloadType(payloadType).setHeaderType(headerType)
                                .setServer(server).setServerSocket(s_socket).setSocket(socket)
                                .setName(name).setPacketType(packetType).setPacketReader(packetReader).create();
        }

        public ProtocolBuilder setClient(Class<?> client) {
            return addAttribute(Protocol.CLIENT_ATTRIBUTE, client);
        }

        public ProtocolBuilder setSocket(Class<?> socket) {
            return addAttribute(Protocol.SOCKET_ATTRIBUTE, socket);
        }

        public ProtocolBuilder setServerSocket(Class<?> s_socket) {
            return addAttribute(Protocol.SERVER_SOCKET_ATTRIBUTE, s_socket);
        }

        public ProtocolBuilder setServer(Class<?> ns) {
            return addAttribute(Protocol.SERVER_ATTRIBUTE, ns);
        }

        public ProtocolBuilder setPacketReader(Class<?> pr) {
            return addAttribute(Protocol.PACKET_READER_ATTRIBUTE, pr);
        }

        public ProtocolBuilder setPacketType(Class<?> packetType) {
            if (Serializable.class.isAssignableFrom(packetType))
                return addAttribute(Protocol.PACKET_ATTRIBUTE, packetType);
            else throw new IllegalArgumentException("Packet-type has to be a member of Serializable.class!");
        }

        public ProtocolBuilder setPayloadType(Class<?> payloadType) {
            return addAttribute(Protocol.PAYLOAD_ATTRIBUTE, payloadType);
        }

        public ProtocolBuilder setHeaderType(Class<?> headerType) {
            return addAttribute(Protocol.HEADER_ATTRIBUTE, headerType);
        }

        public ProtocolBuilder addAttribute(String name, Class<?> c) {
            if (name != null && c != null) attributes.putIfAbsent(name, c);
            return this;
        }

        public ProtocolBuilder setName(String name) {
            if (name != null) this.name = name;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Protocol create() {
            Protocol p = new Protocol(name);
            try {
                ((Map<String, Class<?>>) p.get("attributes")).putAll(attributes);
            } catch (IllegalAccessException | ProtocolItemNotFoundException e) {
                //ignore
            }
            return p;
        }
    }

    public static class ServerBuilder {
        private NetworkServer server;

        private ServerBuilder(NetworkServer s) {
            if (s != null) server = s;
        }

        public static ServerBuilder newBuilder() {
            return new ServerBuilder(null);
        }

        public static ServerBuilder from(NetworkServer server) {
            return new ServerBuilder(server);
        }

        public ServerBuilder setServerProtocol(Object protocol) throws IProtocolException {
            if (server == null) server = new NetworkServer(protocol);
            return this;
        }

        public ServerBuilder setServerSocket(Object... params) throws IProtocolException, NoSuchMethodException,
                IllegalAccessException {
            server.setSocket(params);
            return this;
        }

        public ServerBuilder setNetworkHandler(FactoryNetworkHandler handler)
                throws ProtocolItemNotFoundException, IllegalAccessException {
            AnnotationUtil.set("network.handler", handler, server.get("server.socket"));
            return this;
        }

        public ServerBuilder setClientHandler(Class<? extends FactoryClientHandler<?>> handler)
                throws ProtocolItemNotFoundException, IllegalAccessException {
            AnnotationUtil.set("client.handler", handler, server.get("server.socket"));
            return this;
        }

        public NetworkServer get() {
            if (server == null)
                throw new NullPointerException("Server is null!");
            return server;
        }
    }

}
