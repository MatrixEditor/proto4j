package de.prototest.echo.shared; //@date 30.01.2022

import de.proto4j.annotation.message.Component;
import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.message.NoArgsConstructor;

@Message
@NoArgsConstructor
public class EchoMessage {

    @Component(ord = 1)
    private int counter = 0;

    public void increment() {
        counter++;
    }

    public int get() {
        return counter;
    }
}
