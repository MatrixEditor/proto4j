package de.prototest.messages.shared; //@date 05.02.2022

import de.proto4j.annotation.message.Component;
import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.message.RepeatedField;

import java.util.LinkedList;
import java.util.List;

@Message
public class ArrayMessage {

    @Component(ord = 1)
    @RepeatedField
    private int[] ints = new int[2];

    @Component(ord = 2)
    @RepeatedField
    private List<String> l = new LinkedList<>();

    public List<String> list() {
        return l;
    }

    public int[] array() {
        return ints;
    }
}
