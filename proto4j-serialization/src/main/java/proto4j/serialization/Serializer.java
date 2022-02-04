package proto4j.serialization;//@date 29.01.2022

import java.io.IOException;

public interface Serializer {

    String serialize(Object o) throws IOException;

    Object read(String serialized) throws IOException;
}
