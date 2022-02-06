package de.prototest.messages.shared; //@date 05.02.2022

import de.proto4j.serialization.Serializer;

import java.io.IOException;

public class ExampleSerializer implements Serializer {

    @Override
    public String serialize(Object o) throws IOException {
        return ((ExampleObject)o).getSomeMessage();
    }

    @Override
    public Object read(String serialized) throws IOException {
        ExampleObject o = new ExampleObject();
        o.setSomeMessage(serialized);
        return o;
    }
}
