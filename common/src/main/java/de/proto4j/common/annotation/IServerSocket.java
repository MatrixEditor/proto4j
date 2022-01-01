package de.proto4j.common.annotation;//@date 29.12.2021

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Objects that are used to work as a {@code ServerSocket} have to have this
 * annotation attached to it. The method definitions are the same as descripted
 * in {@link ISocket}.
 *
 * @see ISocket
 * @see ISocket.SocketMethod
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IServerSocket {

    /**
     * @return the socket-accept method which should return a protocol defined
     *         socket.
     */
    ISocket.SocketMethod accept();

    /**
     * @return the close-method which should finally close the {@code ServerSocket}.
     */
    ISocket.SocketMethod close() default @ISocket.SocketMethod(name = "close");
}
