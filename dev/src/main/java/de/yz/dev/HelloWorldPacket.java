package de.yz.dev; //@date 23.01.2022

import de.proto4j.annotation.message.*;

@Message
@NoArgsConstructor
public class HelloWorldPacket {

    @OptionalField
    @Component(ord = 0)
    private String message;

    @AnyType
    @OptionalField
    @Component(ord = 1)
    private Object any_object;

    @RepeatedField
    @OptionalField
    @Component(ord = 2)
    private Integer[] some_int_array;

    public HelloWorldPacket() {}

    public String getMessage() {
        return message;
    }
}
