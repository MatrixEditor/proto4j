package de.prototest.other; //@date 12.02.2022

import de.proto4j.stream.GenericSequencedStream;
import de.proto4j.stream.SequenceStream;

public class Test {
    public static void main(String[] args) {

        Class<?>[]               arr    = new Class<?>[]{Integer.class, String.class};
        SequenceStream<Class<?>> stream = new GenericSequencedStream<>(arr);

        stream.map(Class::getSimpleName).forEach(System.out::println);
    }
}
