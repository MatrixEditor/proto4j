package de.proto4j.common.io; //@date 29.12.2021

import de.proto4j.common.Protocol;
import de.proto4j.common.ProtocolFactory;
import de.proto4j.common.ProtocolUtil;
import de.proto4j.common.annotation.AnnotatedElement;
import de.proto4j.common.annotation.Item;
import de.proto4j.common.exception.IProtocolException;

public class NetworkClient implements ProtocolFactory.FactoryClient, AnnotatedElement {

    @Item(name = "protocol.util.instance", isAccessible = false, hasSetter = false)
    private final ProtocolUtil protocolUtil0 = new ProtocolUtil();

    @Item(name = "protocol.reference", hasSetter = false)
    private final Protocol protocolReference;

    @Item(name = "socket")
    private Object socket;

    public static NetworkClient of(Object protocolReference_, Object socket) throws IProtocolException {
        NetworkClient client = new NetworkClient(protocolReference_);
        client.socket = socket;
        return client;
    }

    public NetworkClient(Object protocolReference_) throws IProtocolException {
        if (getProtocolUtil0().isProtocol(protocolReference_)) {
            protocolReference = (Protocol) protocolReference_;
        } else throw new IProtocolException("<NetworkClient.init()> Object is not a protocol!");
    }

    public <R> R receive(Object... params) throws ReflectiveOperationException, IProtocolException {
        throwNotConnectedException();
        return getProtocolUtil0().receive(socket, params);
    }

    public <R> R receiveAndRead(Object packetReader, Object... params) throws ReflectiveOperationException,
            IProtocolException {
        throwNotConnectedException();
        Object o = receive(params);
        return getProtocolUtil0().readPacket(getProtocolReference(), packetReader,  o);
    }

    public <R> R send(Object... params) throws ReflectiveOperationException, IProtocolException {
        throwNotConnectedException();
        return getProtocolUtil0().send(socket, params);
    }

    public <R> R close(Object... params) throws ReflectiveOperationException, IProtocolException {
        throwNotConnectedException();
        return getProtocolUtil0().close(socket, params);
    }

    public Protocol getProtocolReference() {
        return protocolReference;
    }

    public Object getSocket() {
        return socket;
    }

    public void setSocket(Object... params) throws IProtocolException, NoSuchMethodException, IllegalAccessException {
        if (this.socket == null) this.socket = ProtocolFactory.createSocket(getProtocolReference(), params);
    }

    private void throwNotConnectedException() throws IProtocolException {
        if (socket == null) throw new IProtocolException("Socket has not been set or could not connect!");
    }

    private ProtocolUtil getProtocolUtil0() {
        return protocolUtil0;
    }

}
