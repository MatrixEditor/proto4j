package de.proto4j.internal.logger; //@date 30.01.2022

public interface LogMessage {

    static LogMessage of(String f, Object...toFormat) {
        return () -> String.format(f, toFormat);
    }

    static LogMessage simpleMessage(String msg) {
        return () -> msg;
    }

    static LogMessage readException(Throwable t) {
        return t::getMessage;
    }

    String getMessage();
}
