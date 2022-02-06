package de.prototest.messages.shared; //@date 05.02.2022

import de.proto4j.annotation.message.Component;
import de.proto4j.annotation.message.Message;
import de.proto4j.serialization.TypeSpec;

@Message
public class TypeSpecMessage {

    @Component(ord = 1)
    @TypeSpec(ExampleSerializer.class)
    private ExampleObject obj;

    public ExampleObject getObj() {
        return obj;
    }

    public void setObj(ExampleObject obj) {
        this.obj = obj;
    }
}
