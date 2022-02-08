package de.proto4j.internal.io; //@date 08.02.2022

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.util.Collection;

public abstract class KeyBasedReader extends Proto4jReader {

    private final Key key;

    public KeyBasedReader(SocketChannel chan, Collection<Class<?>> readableClasses, Key key) {
        super(chan, readableClasses);
        this.key = key;
    }

    public abstract Object readMessage() throws IOException;

    protected Key getKey() {
        return key;
    }
}
