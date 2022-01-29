package de.yz.gen; //@date 29.01.2022

import de.proto4j.annotation.message.Component;
import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.message.NoArgsConstructor;

@Message
@NoArgsConstructor
public class HelloWorldMessage {

    @Component(ord = 1)
    private String message = "ui";

    @Override
    public String toString() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
