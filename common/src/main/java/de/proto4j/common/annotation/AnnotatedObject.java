package de.proto4j.common.annotation;//@date 31.12.2021

import de.proto4j.common.exception.ProtocolItemNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public interface AnnotatedObject {

    default <R extends Annotation> R lookup(Class<R> c) {
        return lookup(c, getClass()::getDeclaredAnnotation);
    }

    default <R extends Annotation> R lookup(Class<R> c, Function<Class<R>, R> getter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(c);

        return getter.apply(c);
    }

    default boolean contains(String itemName) {
        return contains(itemName, null);
    }

    default boolean contains(String itemName, Class<?> type) {
        try {
            Field f_ = fieldOf(itemName);
            if (type != null) {
                return f_.getGenericType().getTypeName().equals(type.getTypeName());
            }
            return true;
        } catch (ProtocolItemNotFoundException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    default <R> R get(String name) throws ProtocolItemNotFoundException, IllegalAccessException {
        return (R) fieldOf(name, i -> i.hasGetter() && i.isAccessible()).get(this);
    }

    default <E> void set(String name, E e) throws ProtocolItemNotFoundException, IllegalAccessException {
        Field f_ = fieldOf(name, i -> i.hasSetter() && i.isAccessible());

        if (f_.getType().isInstance(e)) {
            f_.set(this, e);
        }
    }

    default Field fieldOf(String itemName) throws ProtocolItemNotFoundException {
        return fieldOf(itemName, i -> true);
    }

    default Field fieldOf(String itemName, Predicate<Item> predicate) throws ProtocolItemNotFoundException {
        Objects.requireNonNull(itemName);

        for (Field f_ : getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f_.getModifiers())) continue;

            if (!f_.canAccess(this)) f_.setAccessible(true);

            Item i = f_.getAnnotation(Item.class);
            if (i != null) {
                if (i.name().equals(itemName)) {
                    if (predicate != null) {
                        if (predicate.test(i)) return f_;
                    } else return f_;
                }
            }
        }
        throw new ProtocolItemNotFoundException(itemName);
    }
}
