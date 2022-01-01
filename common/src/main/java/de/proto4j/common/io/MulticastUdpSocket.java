package de.proto4j.common.io;//@date 16.11.2021

import de.proto4j.common.PrintColor;
import de.proto4j.common.PrintService;
import de.proto4j.common.annotation.ISocket;
import de.proto4j.common.exception.ProtocolTimeoutException;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;

@ISocket(
    send = @ISocket.SocketMethod(name = "mSend", params = {byte[].class}),
    receive = @ISocket.SocketMethod(name = "prepareReceive", returnClass = DatagramPacket.class)
)
public class MulticastUdpSocket implements Closeable {

    /**
     * The port the socket will be listening on.
     *
     * @since 1.0
     */
    private int listen_port;

    /**
     * When receiving a {@link DatagramPacket} this buffer is used to
     * store the maximum amount of data in a byte-array. Note that every not
     * used byte in the received packet will be filled up with a 0x00.
     *
     * @since 1.0
     */
    private int max_buffer;

    /**
     * The main-socket which is used to capture the traffic on the specified
     * multicast-address.
     *
     * @since 1.0
     */
    private MulticastSocket socket;

    /**
     * The currently used multicast-address
     *
     * @since 1.0
     */
    private InetAddress current_address;

    public MulticastUdpSocket(Integer listen_port, Integer max_buffer,
                              InetAddress current_address) throws IOException {
        this(listen_port, max_buffer, current_address, null);
    }

    public MulticastUdpSocket(Integer listen_port, Integer max_buffer,
                              InetAddress current_address, NetworkInterface nif) throws IOException {
        wrapPort(listen_port);

        if (getListenPort() <= 65535 && getListenPort() >= 1) {
            socket = new MulticastSocket(getListenPort());
            if (nif != null) socket.setNetworkInterface(nif);

            setAddress(current_address);
            if (getCurrentAddress().isMulticastAddress()) {
                socket.joinGroup(getCurrentAddress());
            }
            socket.setReuseAddress(true);
        }

        this.max_buffer = max_buffer > 0 ? max_buffer : 256;
    }

    public DatagramPacket prepareReceive() throws SocketException, ProtocolTimeoutException {
        if (!socket.isClosed()) {
            byte[]         buf = new byte[getMaxBuffer()];
            DatagramPacket dp  = new DatagramPacket(buf, getMaxBuffer());
            try {
                socket.receive(dp);
            } catch (SocketTimeoutException timeout) {
                throw new ProtocolTimeoutException();
            } catch (IOException e) {
                // ignore
            }
            return dp;
        } else throw new SocketException();
    }

    public void mSend(byte[] buf) throws IOException {
        if (buf.length > getMaxBuffer())
            return;

        getSocket().send(new DatagramPacket(buf, buf.length, getCurrentAddress(),
                                            getListenPort()));
        PrintService.log(PrintColor.BRIGHT_BLACK, "Out:", "(to):", getCurrentAddress().getHostAddress(),
                         "| (from):", getHostAddress().equals("0.0.0.0") ? "this" : getHostAddress());
    }

    public String getHostAddress() throws SocketException {
        return getSocket().getInterface().getHostAddress();
    }

    @Deprecated
    public void uSend(byte[] buf, InetAddress dest) throws IOException {
        if (buf.length > getMaxBuffer())
            return;

        buf[5] = buf[5] != 0 ? buf[5] : 1;
        getSocket().send(new DatagramPacket(buf, buf.length, dest, getListenPort()));
        PrintService.log(PrintColor.BRIGHT_BLACK, "Out:", "(to):", dest.getHostAddress(),
                         "| (from):", getHostAddress());
    }

    private void setAddress(InetAddress current_address) throws UnknownHostException {
        this.current_address = current_address;
    }

    private void wrapPort(int i) {
        if (i >= 1 && i <= 65535) listen_port = i;
        else this.listen_port = -1;
    }

    public int getListenPort() {
        return listen_port;
    }

    public int getMaxBuffer() {
        return max_buffer;
    }

    public void setMaxBuffer(int max_buffer) {
        this.max_buffer = max_buffer;
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public InetAddress getCurrentAddress() {
        return current_address;
    }

    public void setSoTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        socket.setReuseAddress(on);
    }

    @Override
    public void close() {
        socket.close();
    }
}
