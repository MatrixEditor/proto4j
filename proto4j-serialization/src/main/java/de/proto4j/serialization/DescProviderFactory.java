package de.proto4j.serialization; //@date 31.01.2022

import de.proto4j.annotation.message.AllArgsConstructor;
import de.proto4j.annotation.message.Component;
import de.proto4j.annotation.message.PacketModifier;
import de.proto4j.serialization.desc.FieldDesc;
import de.proto4j.serialization.desc.MessageDesc;
import de.proto4j.serialization.desc.TypeSpec;
import de.proto4j.serialization.desc.TypeSpecField;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static de.proto4j.serialization.desc.Member.*;
import static de.proto4j.serialization.desc.MessageDesc.forType;

public final class DescProviderFactory {

    public static final char LF = '\n';
    public static final char RF = '\r';

    public static final String DEFAULT_DELIMITER = "-";

    public static final String LF_REPLACEMENT        = "%n";
    public static final String RF_REPLACEMENT        = "%r";
    public static final String DELIMITER_REPLACEMENT = "%M";

    public static StringBuffer allocate(Object message) throws IOException {
        if (message == null) throw new NullPointerException("message is null");

        Class<?>     messageClass = message.getClass();
        StringBuffer buf          = new StringBuffer();
        MessageDesc  desc         = new MessageDesc();

        desc.setMessageClass(messageClass);

        if (messageClass.isAnnotationPresent(AllArgsConstructor.class)) desc.addModifier(ALL_ARGS_MODIFIER);
        else desc.addModifier(NO_ARGS_MODIFIER);

        for (Field f0 : messageClass.getDeclaredFields()) {
            if (Modifier.isStatic(f0.getModifiers())) continue;

            if (!f0.canAccess(message)) f0.setAccessible(true);

            if (PacketModifier.isComponent(f0)) {
                FieldDesc fieldDesc;

                if (f0.isAnnotationPresent(TypeSpec.class)) {
                    fieldDesc = new TypeSpecField();
                    fieldDesc.addModifier(TYPE_SPEC_MODIFIER);
                    fieldDesc.setType(f0.getType());
                    ((TypeSpecField)fieldDesc).setStYPE(f0.getDeclaredAnnotation(TypeSpec.class).value());

                } else {
                    fieldDesc = forType(f0.getType().getName());
                    if (PacketModifier.isAny(f0)) fieldDesc.addModifier(FieldDesc.ANY_TYPE_MODIFIER);
                }

                fieldDesc.setOrdinal(f0.getDeclaredAnnotation(Component.class).ord());

                if (PacketModifier.isOptional(f0)) fieldDesc.addModifier(OPTIONAL_MODIFIER);
                else {
                    if (PacketModifier.isOneOf(f0)) fieldDesc.addModifier(ONE_OF_MODIFIER);
                    if (PacketModifier.isRepeated(f0)) fieldDesc.addModifier(REPEATED_MODIFIER);
                }

                try {
                    fieldDesc.setValue(f0.get(message));
                } catch (ReflectiveOperationException e) {/**/}

                if (!fieldDesc.hasModifier(TYPE_SPEC_MODIFIER)) fieldDesc.setType(f0.getType());
                desc.getFields().add(fieldDesc);
            }

        }
        buf.append(desc.serialize());
        return buf;
    }

    public static Object convert(byte[] decryptedData, Set<Class<?>> readable) throws IOException {
        if (decryptedData.length == 0)
            throw new IllegalArgumentException("data cannot be 0");

        //if (decryptedData.length % Proto4jWriter.SHARED_KEY.length != 0)
        //throw new IllegalArgumentException("data has to be a multiple of sharedKey.length");
        int offs = 0;
        while (decryptedData[offs] < 0) {
            offs++;
        }
        String      rep  = new String(Arrays.copyOfRange(decryptedData, offs, decryptedData.length));
        rep = rep.replaceAll("\u0000", "");
        MessageDesc desc = new MessageDesc();
        desc.read(rep);

        try {
            String[] h = desc.getName().split(":");

            // This prevents the system from loading untrusted data simply by checking
            // if the class-simpleName is contained in the name-set
            Set<String> simpleNames = readable.stream().map(Class::getSimpleName).collect(Collectors.toSet());
            if (!simpleNames.contains(h[1]))
                throw new IllegalArgumentException("unknown message class: " + h[1]);

            Class<?> messageClass = Class.forName(h[0]);
            if (!readable.contains(messageClass))
                throw new IllegalArgumentException("unknown message class: " + messageClass.getSimpleName());

            if (desc.hasModifier(NO_ARGS_MODIFIER)) {
                Object message = messageClass.getDeclaredConstructor().newInstance();
                for (FieldDesc f : desc.getFields()) {
                    for (Field f0 : message.getClass().getDeclaredFields()) {
                        if (Modifier.isStatic(f0.getModifiers())) continue;

                        if (PacketModifier.isComponent(f0)) {
                            if (f.getOrdinal() == f0.getDeclaredAnnotation(Component.class).ord()) {
                                f0.setAccessible(true);
                                f0.set(message, f.getValue());
                                break;
                            }
                        }
                    }
                }
                return message;
            } else {
                Object[] args = desc.getFields().stream()
                                    .sorted(Comparator.comparingInt(FieldDesc::getOrdinal))
                                    .map(FieldDesc::getValue)
                                    .toArray();

                Class<?>[] types = getTypes(args);

                try {
                    Constructor<?> constructor = messageClass.getConstructor(types);
                    return constructor.newInstance(args);
                } catch (ReflectiveOperationException e) {
                    System.err.println(e.toString());
                    /**/
                }
                return null;//TODO
            }

        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("class is not implemented!");

        } catch (ReflectiveOperationException e) {
            throw new IllegalCallerException("could not initialize message object");
        }
    }

    public static Class<?>[] getTypes(Object[] args) {
        Class<?>[] t = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            t[i] = args[i].getClass();
        }
        return t;
    }

    public static boolean checkParameterClasses(Class<?>[] parameterTypes, Object... params) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> c = parameterTypes[i];
            if (!c.isAssignableFrom(params[i].getClass())) return false;

        }
        return true;
    }
}
