package de.proto4j.common.annotation;//@date 29.12.2021

import java.lang.annotation.*;

/**
 * Fields in classes that should be accessed by {@code AnnotationUtil.get(String, Object)}
 * or {@code AnnotationUtil.set(String, Object, Object)} have to be annotated
 * with {@code @Item}. In order to make the field inaccessible or unable to assign
 * a new value to it different checker-methods are used.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Item {
    /**
     * @return the name of this item (can differ from fields' name)
     */
    String name();

    /**
     * @return whether this item can be accessed (same effect as
     *          {@link Item#isAccessible()} )
     *
     * @apiNote AnnotationUtil.get(String, Object)
     */
    boolean hasGetter() default true;

    /**
     * @return whether this item can be assigned to a new value
     *
     * @apiNote AnnotationUtil.set(String, Object, Object)
     */
    boolean hasSetter() default true;

    /**
     * @return whether this item can be accessed
     */
    boolean isAccessible() default true;
}
