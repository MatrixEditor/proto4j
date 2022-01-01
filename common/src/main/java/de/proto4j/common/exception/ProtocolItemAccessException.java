package de.proto4j.common.exception; //@date 29.12.2021

public class ProtocolItemAccessException extends IProtocolException {

    /**
     * Constructs a new ProtocolException with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ProtocolItemAccessException() {
    }

    /**
     * Constructs a new ProtocolException with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public ProtocolItemAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new ProtocolException with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since 1.4
     */
    public ProtocolItemAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ProtocolException with the specified cause and a detail
     * message of {@code (cause==null ? null : cause.toString())} (which
     * typically contains the class and detail message of {@code cause}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since 1.4
     */
    public ProtocolItemAccessException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ProtocolException with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param message the detail message.
     * @param cause the cause.  (A {@code null} value is permitted,
     *         and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression whether or not suppression is enabled
     *         or disabled
     * @param writableStackTrace whether or not the stack trace should
     *         be writable
     * @since 1.7
     */
    public ProtocolItemAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
