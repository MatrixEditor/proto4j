package de.proto4j.internal.io; //@date 08.02.2022

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.Key;

public abstract class KeyBasedWriter extends Proto4jWriter {

    private final Key key;

    public KeyBasedWriter(SocketChannel chan, Key key) {
        super(chan);
        this.key = key;
    }

    public abstract void write(Object message) throws IOException;

    protected Key getKey() {
        return key;
    }



}
