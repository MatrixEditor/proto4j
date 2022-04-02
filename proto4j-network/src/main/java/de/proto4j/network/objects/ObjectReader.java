package de.proto4j.network.objects; //@date 08.02.2022

import de.proto4j.internal.io.KeyBasedReader;
import de.proto4j.serialization.DescProviderFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class ObjectReader extends KeyBasedReader {

    private final Queue<Object> waitingQueue = new LinkedList<>();

    private String last = null;

    public ObjectReader(SocketChannel chan, Collection<Class<?>> readableClasses, Key key) {
        super(chan, readableClasses, key);
    }

    @Override
    public synchronized Object readMessage() throws IOException {
        if (!waitingQueue.isEmpty()) {
            return waitingQueue.poll();
        }
        if (isClosed()) throw new IOException("stream is closed");
        ByteBuffer buffer = ByteBuffer.allocate(2048);

        int len = read(buffer.array());
        byte[] encrypted = new byte[len];
        System.arraycopy(buffer.array(), 0, encrypted, 0, len);

        // Security aspect would start here
        String next = new String(encrypted);
        String[] stream = next.split("[\t]");

        for (String s : stream) {
            String msg = s;
            if (msg.endsWith("\n") || msg.endsWith("\r")) {
                if (last != null) {
                    msg  = last + msg;
                    last = null;
                }
                waitingQueue.add(DescProviderFactory.convert(msg.getBytes(), getReadable()));
            } else {
                last = s;
                break;
            }
        }
        return waitingQueue.poll();
    }

    private int offset(byte[] b, int start) {
        while (b[start] != ObjectWriter.END_BYTE) {
            if (start == b.length - 1) break;
            start++;
        }
        return start;
    }
}
