package de.proto4j.internal.io.desc;//@date 31.01.2022

import java.io.IOException;

public interface ObjectDesc {

    String getName();

    String serialize() throws IOException;

    ObjectDesc read(String serialized) throws IOException;
}
