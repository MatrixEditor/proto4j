package de.proto4j.annotation.message;//@date 29.01.2022

import java.io.IOException;

public interface Serializer {

    String serialize(Object o) throws IOException;
}
