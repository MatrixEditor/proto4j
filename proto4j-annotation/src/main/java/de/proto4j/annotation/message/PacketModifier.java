package de.proto4j.annotation.message; //@date 23.01.2022

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class PacketModifier {

    private PacketModifier() {}

    public static boolean isMessage(Class<?> c) {
        return c != null && c.isAnnotationPresent(Message.class);
    }

    public static boolean isMessage(Object o) {
        return isMessage(o.getClass());
    }

    public static boolean hasAllArgsConstructor(Class<?> x) {
        return checkConstructor(x, AllArgsConstructor.class);
    }

    public static boolean hasAllArgsConstructor(Object o) {
        return checkConstructor(o.getClass(), AllArgsConstructor.class);
    }

    public static boolean hasNoArgsConstructor(Class<?> x) {
        return checkConstructor(x, NoArgsConstructor.class);
    }

    public static boolean hasNoArgsConstructor(Object o) {
        return checkConstructor(o.getClass(), NoArgsConstructor.class);
    }

    public static boolean isOptional(AnnotatedElement e) {
        return e != null && e.isAnnotationPresent(OptionalField.class)&& !e.isAnnotationPresent(Deprecated.class);
    }

    public static boolean isRepeated(AnnotatedElement e) {
        return e != null && e.isAnnotationPresent(RepeatedField.class)&& !e.isAnnotationPresent(Deprecated.class);
    }

    public static boolean isOneOf(AnnotatedElement e) {
        return e != null && e.isAnnotationPresent(OneOf.class)&& !e.isAnnotationPresent(Deprecated.class);
    }

    public static boolean isComponent(AnnotatedElement e) {
        return e != null && e.isAnnotationPresent(Component.class) && !e.isAnnotationPresent(Deprecated.class);
    }

    public static boolean isAny(AnnotatedElement e) {
        return e != null && e.isAnnotationPresent(AnyType.class) && !e.isAnnotationPresent(Deprecated.class);
    }

    public static boolean hasTypeSpec(AnnotatedElement e) {
        return e != null && e.isAnnotationPresent(TypeSpec.class) && !e.isAnnotationPresent(Deprecated.class);
    }

    private static boolean checkConstructor(Class<?> c, Class<? extends Annotation> a) {
        if (c != null) {
            for (Constructor<?> con : c.getDeclaredConstructors()) {
                if (con.isAnnotationPresent(a)) return true;
            }
        }
        return false;
    }

    public static void setComponent(Object packet, int ord, Object o) throws IllegalAccessException {
        if (packet != null && ord > 0) {
            if (PacketModifier.isMessage(packet)) {
                for (Field f : packet.getClass().getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())) continue;

                    if (f.isAnnotationPresent(Component.class)) {
                        if (ord == f.getDeclaredAnnotation(Component.class).ord()) {
                            f.set(packet, o);
                        }
                    }
                }
            }
        }
    }

}
