package de.proto4j.common.io; //@date 29.12.2021


import de.proto4j.common.annotation.ISocket;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@ISocket(
    send = @ISocket.SocketMethod(name = "send", params = {Object.class}),
    receive = @ISocket.SocketMethod(name = "waitForInput", returnClass = Object.class)
)
public class ObjectSocket {

    private final Object socketLock = new Object();
    private final Object inputLock  = new Object();
    private final Object outputLock = new Object();

    private Socket             socket;
    private ObjectInputStream  reader;
    private ObjectOutputStream writer;

    /**
     * Instantiates a new Basic network connection.
     *
     * @throws IOException the io exception
     */
    public ObjectSocket() throws IOException {
        this(new Socket());
    }

    /**
     * Instantiates a new Basic network connection.
     *
     * @param host the host
     * @param port the port
     * @throws IOException the io exception
     */
    public ObjectSocket(String host, Integer port) throws IOException {
        this(new Socket(host, port));
    }

    /**
     * Instantiates a new Basic network connection.
     *
     * @param socket the socket
     * @throws IOException the io exception
     */
    public ObjectSocket(Socket socket) throws IOException {
        this.socket = (socket);
        this.socket.setReuseAddress(true);
        this.socket.setSoTimeout(5000);


        //IMPORTANT: At first the output-Stream
        this.writer = (new ObjectOutputStream(socket.getOutputStream()));
        this.reader = (new ObjectInputStream(socket.getInputStream()));
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
    public Object waitForInput() throws IOException, ClassNotFoundException {
        if (!socket.isClosed()) {
            synchronized (socketLock) {
                return getReader().readObject();
            }
        }
        throw new IOException("Connection is closed");
    }

    /**
     * Sends a message over the network to the end of the SocketChannel.
     *
     * @param o the output object
     * @throws IOException Any of the usual Input/Output related exceptions
     */
    public void send(Object o) throws IOException {
        if (!socket.isClosed() && o != null) {
            getWriter().writeObject(o);
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
            if (writer != null) {
                closeObj(writer);
                writer = null;
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
            if (reader != null) {
                closeObj(reader);
                reader = null;
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    private ObjectInputStream getReader() {
        return reader;
    }

    private ObjectOutputStream getWriter() {
        return writer;
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
