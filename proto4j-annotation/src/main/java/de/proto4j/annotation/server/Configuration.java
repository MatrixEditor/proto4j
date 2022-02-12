package de.proto4j.annotation.server;//@date 29.01.2022

import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.server.requests.Controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In order to configure the bean-management this annotation is useful. For
 * implemented configurations see list below.
 * <li>
 *     <b>ignoreValues</b>: ignores all non-context relevant classes and beans.
 *     Note that all classes related to this client or server <b>must</b> be in
 *     the same package or lower - not above!
 *     <pre>
 *         Client       : a.b.Client.java
 *
 *         IgnoredClass : a.SomeClass.java
 *         UsedClasses  : a.b.c.SomeOtherClass.java
 *                        a.b.AnotherClass.java
 *                        a.d.SomeMessage.java
 *     </pre>
 *     <i>Important:</i> {@link Message}-Types are still loaded even if this
 *     option is enabled to ensure all types can be read and written.
 * </li>
 * <li>
 *     <b>byConnection</b>: every {@link Controller} is going to be mapped with an
 *     address that should be given with a {@link Configuration}-Annotation.
 * </li>
 * <li>
 *     <b>byValue</b>: A default configuration for Handlers to react to all received
 *     messages once.
 * </li>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Configuration {

    String[] value();

}
