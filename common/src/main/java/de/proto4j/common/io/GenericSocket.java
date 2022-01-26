package de.proto4j.common.io; //@date 02.01.2022

import de.proto4j.common.ProtocolUtil;
import de.proto4j.common.annotation.AnnotationUtil;
import de.proto4j.common.annotation.ISocket;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

@ISocket(
        send = @ISocket.SocketMethod(name = "send", params = {Object.class}),
        receive = @ISocket.SocketMethod(name = "waitForInput", returnClass = Object.class)
)
public class GenericSocket<S_IN extends InputStream, S_OUT extends OutputStream> {

    private final Object socketLock = new Object();
    private final Object inputLock  = new Object();
    private final Object outputLock = new Object();

    private Socket socket;
    private S_IN   in;
    private S_OUT  out;

    public GenericSocket(Class<S_IN> i, Class<S_OUT> o) throws IOException, NoSuchMethodException {
        this(new Socket(), i, o);
    }

    public GenericSocket(String host, Integer port, Class<S_IN> i, Class<S_OUT> o) throws IOException,
            NoSuchMethodException {
        this(new Socket(host, port), i, o);
    }

    public GenericSocket(Socket s, Class<S_IN> i, Class<S_OUT> o) throws IOException, NoSuchMethodException {
        Objects.requireNonNull(i);
        Objects.requireNonNull(o);

        socket = s;
        out = (S_OUT) ProtocolUtil.getInstance().newObject(o, socket.getInputStream());
        in  = (S_IN) ProtocolUtil.getInstance().newObject(i, socket.getInputStream());
    }

    /**
     * Waits for an Input on the underlying Socket.
     *
     * @return the input and null when an exception is thrown
     * @throws IOException            If any of the usual Input/Output related
     *                                exceptions occur.
     * @throws ClassNotFoundException If the class of a serialized object cannot
     *                                be found.
     */
    public Object waitForInput(String method, Object...params) throws IOException, ClassNotFoundException {
        if (!socket.isClosed()) {
            synchronized (socketLock) {
                return AnnotationUtil.supply(method, getIn(), params);
            }
        }
        throw new IOException("Connection is closed");
    }

    /**
     * Sends a message over the network to the end of the SocketChannel.
     *
     * @throws IOException Any of the usual Input/Output related exceptions
     */
    public void send(Object...params) throws IOException {
        if (!socket.isClosed() && params != null) {
            AnnotationUtil.supply("send", getOut(), params);
        }
    }

    /**
     * Close socket.
     *
     * @throws IOException the io exception
     */
    public void closeSocket() throws IOException {
        synchronized (this.socketLock) {
            if (socket != null) {
                closeObj(socket);
                socket = null;
            }
        }
    }

    /**
     * Close output.
     *
     * @throws IOException the io exception
     */
    public void closeOutput() throws IOException {
        synchronized (this.outputLock) {
            if (out != null) {
                closeObj(out);
                out = null;
            }
        }
    }

    /**
     * Close input.
     *
     * @throws IOException the io exception
     */
    public void closeInput() throws IOException {
        synchronized (this.inputLock) {
            if (in != null) {
                closeObj(in);
                in = null;
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    private S_IN getIn() {
        return in;
    }

    private S_OUT getOut() {
        return out;
    }

    private void closeObj(Closeable closeable) throws IOException {
        closeable.close();
    }

    /**
     * Closes this channel.
     *
     * <p> If this channel is already closed then invoking this method has no
     * effect.
     *
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException {
        closeSocket();

        closeInput();
        closeOutput();
    }

}
