package de.proto4j.network.objects; //@date 28.01.2022

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

public class MessageRequest {

    final static byte EOS = 10;

    private String startLine;
    private SocketChannel channel;
    private InputStream is;
    private OutputStream os;

    public MessageRequest(InputStream is, OutputStream os) throws IOException {
        this.is = is;
        this.os = os;
        doRead();
    }

    private void doRead() throws IOException {
        if (is == null || os == null) {
            throw new IOException();
        }
        do {
            startLine = readNextLine();
            if (startLine == null) return;

        } while (startLine.equals(""));
    }

    public InputStream inputStream() {
        return is;
    }

    public OutputStream outputStream() {
        return os;
    }

    private char[] buf = new char[2048];
    private int pos;
    private StringBuffer lineBuf;

    public String getStartLine() {
        return startLine;
    }

    public String readNextLine() throws IOException {
        boolean gotEOS = false;
        pos = 0; lineBuf = new StringBuffer();

        while (!gotEOS) {
            int c = is.read();
            if (c == -1) return null;

            if (c == EOS) gotEOS = true;
            consume(c);
        }
        lineBuf.append(buf, 0, pos);
        return new String(lineBuf);
    }

    private void consume(int c) {
        if (pos == 2048) {
            lineBuf.append(buf);
            pos = 0;
        }
        buf[pos++] = (char)c;
    }

}
