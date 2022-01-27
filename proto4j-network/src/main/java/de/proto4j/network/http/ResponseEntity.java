package de.proto4j.network.http;//@date 26.01.2022

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.function.Function;

public class ResponseEntity<T> implements Serializable {

    private final int code;

    private long length;

    private T response;

    private Function<T, byte[]> mapper;

    public ResponseEntity(int code) {this.code = code;}

    public static <E> ResponseEntity<E> ok() {
        return new ResponseEntity<>(200);
    }

    public ResponseEntity<T> setContent(T t) {
        if (t != null) this.response = t;
        if (response != null && response instanceof String) {
            //noinspection unchecked
            mapper = (Function<T, byte[]>) (Function<String, byte[]>) String::getBytes;
        }

        return this;
    }

    public ResponseEntity<T> setContentLength(long l) {
        this.length = l;
        return this;
    }

    public byte[] getBytes() {
        if (mapper != null) return mapper.apply(getResponse());
        return null;
    }

    public int getCode() {
        return code;
    }

    public long getLength() {
        return length;
    }

    public T getResponse() {
        return response;
    }

    public void setMapper(Function<T, byte[]> mapper) {
        this.mapper = mapper;
    }
}
