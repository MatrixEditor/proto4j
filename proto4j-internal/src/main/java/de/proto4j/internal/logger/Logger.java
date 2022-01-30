package de.proto4j.internal.logger;//@date 30.01.2022

public interface Logger {

    enum Level {
        DEBUG,
        INFO,
        LOG,
        NONE,
        WARNING,
        EXCEPTION
    }

    default void debug(PrintColor color, LogMessage logMessage) {
        log(Level.DEBUG, color, logMessage);
    }

    default void info(PrintColor color, LogMessage logMessage) {
        log(Level.INFO, color, logMessage);
    }

    default void warning(PrintColor color, LogMessage logMessage) {
        log(Level.WARNING, color, logMessage);
    }

    default void except(PrintColor color, Throwable t) {
        except(color, LogMessage.readException(t));
    }

    default void except(PrintColor color, LogMessage logMessage) {
        log(Level.EXCEPTION, color, logMessage);
    }

    void log(Level level, PrintColor color, LogMessage logMessage);
}
