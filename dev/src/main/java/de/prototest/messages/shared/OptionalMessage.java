package de.prototest.messages.shared; //@date 05.02.2022

import de.proto4j.annotation.message.Component;
import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.message.NoArgsConstructor;
import de.proto4j.annotation.message.OptionalField;

@Message
@NoArgsConstructor
public class OptionalMessage {

    @Component(ord = 1)
    @OptionalField
    private int optionalInt;

    public int getOptionalInt() {
        return optionalInt;
    }
}
