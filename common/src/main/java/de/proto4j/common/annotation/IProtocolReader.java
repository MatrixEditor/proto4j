package de.proto4j.common.annotation;//@date 29.12.2021

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link IProtocolReader} is used to indicate that the class this annotation
 * is attached to can read the defined {@code Protocol} packets. To
 * do that the name of the class-method (to execute it), the output-class (PacketType)
 * and the method parameter-classes are needed.
 * <p>
 * The method structure could be as follows:
 * <pre>
 *    {@code @}IProtocolReader(
 *          outputClass = YourPacket.class
 *          inputCLasses = {Parameter1.class, Parameter2.class}
 *          method = "readPacket")
 *     class YourPacketReader {
 *
 *        public YourPacket readPacket(Parameter1 p1, Parameter2 p2) {
 *            ...
 *        }
 *     }
 * </pre>
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IProtocolReader {
    /**
     * @return the method return-value class
     */
    Class<?> outputClass();

    /**
     * @return the method parameter classes
     */
    Class<?>[] inputClasses() default {};

    /**
     * @return the method name
     */
    String method() default "readPacket";
}
