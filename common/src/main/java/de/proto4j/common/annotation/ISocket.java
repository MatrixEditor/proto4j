package de.proto4j.common.annotation;//@date 29.12.2021

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Objects which are used as a {@code Socket} implement this annotation. The
 * three main methods are {@code send}, {@code receive} and {@code close}. Through
 * {@link SocketMethod}s the structure of the methods are defined:
 * <pre>
 *     {@code @}ISocket(
 *          send = {@code @}SocketMethod(name = "send", params = {YourPacket.class})
 *          ...
 *      )
 *      class YourSocket {
 *          ...
 *          public void send(YourPacket p) {
 *              ...
 *          }
 *      }
 * </pre>
 * All methods should be implemented to run without any errors. A parameter-check
 * is done before a method is called with given arguments to prevent errors with
 * wrong parameters.
 *
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ISocket {

    /**
     * @return the defined send-method
     */
    SocketMethod send();

    /**
     * @return the defined close-method
     */
    SocketMethod close() default @SocketMethod(name = "close");

    /**
     * @return the defined receive-method
     */
    SocketMethod receive();

    /**
     * Methods in defined {@code Sockets} should use this annotation to describe
     * the methods that are used later on to send, receive or close.
     */
    public static @interface SocketMethod {
        String name();
        Class<?>[] params() default {};
        Class<?> returnClass() default void.class;
    }

}
