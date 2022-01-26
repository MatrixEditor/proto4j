package de.proto4j.common.annotation; //@date 29.12.2021

import de.proto4j.common.PrintColor;
import de.proto4j.common.PrintService;
import de.proto4j.common.exception.ProtocolItemNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class AnnotationUtil {

    public static <T, R extends Annotation> R lookup(Class<T> t, Class<R> c) {
        return lookup(t, c, t::getDeclaredAnnotation);
    }

    public static <T, R extends Annotation> R lookup(Class<T> t, Class<R> c, Function<Class<R>, R> getter) {
        Objects.requireNonNull(t);
        Objects.requireNonNull(c);

        return getter.apply(c);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R invokeOrNull(Method m, T t, Object... params) {
        try {
            return (R) m.invoke(t, params);
        } catch (InvocationTargetException e) {
            PrintService.logError((Exception) e.getTargetException(), PrintColor.DARK_RED);
        } catch (IllegalAccessException e) {
            PrintService.logError(e, PrintColor.DARK_RED);
        }
        return null;
    }

    public static <R> R supply(String name, Object t, Object... params) {
        for (Method m : t.getClass().getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                if (!m.canAccess(t)) m.setAccessible(true);
                if (params.length == m.getParameterCount()) {
                    Class<?>[] parameterTypes = m.getParameterTypes();
                    if (checkParameterClasses(parameterTypes, params))
                        return invokeOrNull(m, t, params);
                }
            }
        }
        throw new NoSuchElementException("Execution of '" + name + "' failed!");
    }

    public static boolean checkParameterClasses(Class<?>[] parameterTypes, Object...params) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> c = parameterTypes[i];
            if (!c.isAssignableFrom(params[i].getClass())) return false;

        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R get(String name, T t) throws IllegalAccessException, ProtocolItemNotFoundException {
        return (R) fieldOf(name, t, i -> i.hasGetter() && i.isAccessible()).get(t);
    }

    public static <T, E> void set(String name, E element, T ref) throws ProtocolItemNotFoundException,
            IllegalAccessException {
        Field f_ = fieldOf(name, ref, i -> i.hasSetter() && i.isAccessible());

        if (f_.getType().isInstance(element) || f_.getType().isAssignableFrom(element.getClass())) {
            f_.set(ref, element);
        }
    }

    public static boolean contains(String name, Object ref) {
        return contains(name, null, ref);
    }

    public static boolean contains(String name, Class<?> type, Object ref) {
        try {
            Field f_ = fieldOf(name, ref);
            if (type != null) {
                return f_.getGenericType().getTypeName().equals(type.getTypeName());
            }
            return true;
        } catch (ProtocolItemNotFoundException e) {
            return false;
        }
    }

    private static <T> Field fieldOf(String itemName, T t) throws ProtocolItemNotFoundException {
        return fieldOf(itemName, t, x -> true);
    }

    private static <T> Field fieldOf(String itemName, T t, Predicate<Item> tester) throws ProtocolItemNotFoundException {
        Objects.requireNonNull(t);
        Objects.requireNonNull(itemName);

        for (Field f_ : t.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f_.getModifiers())) continue;

            if (!f_.canAccess(t))
                f_.setAccessible(true);

            Item i = f_.getAnnotation(Item.class);
            if (i != null) {
                if (i.name().equals(itemName) && tester.test(i)) {
                    return f_;
                }
            }
        }
        throw new ProtocolItemNotFoundException(itemName);
    }
}
