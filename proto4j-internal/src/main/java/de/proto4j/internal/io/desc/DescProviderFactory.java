package de.proto4j.internal.io.desc; //@date 31.01.2022

import de.proto4j.annotation.message.Component;
import de.proto4j.annotation.message.Message;
import de.proto4j.annotation.message.PacketModifier;
import de.proto4j.annotation.message.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.ProviderNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;

import static de.proto4j.internal.io.desc.FieldDesc.*;
import static de.proto4j.internal.io.desc.FieldDesc.REPEATED_MODIFIER;

public final class DescProviderFactory {

    public static final char LF = '\n';
    public static final char RF = '\r';

    public static final String DEFAULT_DELIMITER = "-";

    public static final String LF_REPLACEMENT        = "%n";
    public static final String RF_REPLACEMENT        = "%r";
    public static final String DELIMITER_REPLACEMENT = "%M";

    public static FieldDesc forType(Class<?> valueType) {
        if (PrimitiveFieldDesc.primitiveTypes().contains(valueType))
            return new PrimitiveFieldDesc();
        else {
            if (RepeatedFieldDesc.arrayTypes().contains(valueType)
                    || RepeatedFieldDesc.collectionTypes().contains(valueType)) {
                return new RepeatedFieldDesc();
            }
        }
        throw new ProviderNotFoundException();
    }

    /**
     * Creates a {@link StringBuffer} from a given message object. The structure
     * of this buffer is as follows:
     * <pre>
     *     a.b.ClassName::ClassName\n
     *     ord-[attributes]-type-len-VALUE-\r
     *     ...
     *     \n\r
     * </pre>
     *
     * @param message the message instance
     * @return a buffer that represents that instance
     */
    public static StringBuffer allocate(Object message) throws IOException {
        if (message == null) throw new NullPointerException("message is null");

        Class<?> messageClass = message.getClass();
        if (!messageClass.isAnnotationPresent(Message.class)) {
            throw new IllegalArgumentException("Object is not an instance of Message.class");
        }

        StringBuffer buf  = new StringBuffer();
        MessageDesc  desc = new MessageDesc();

        desc.setMessageClass(messageClass);

        for (Field f0 : messageClass.getDeclaredFields()) {
            if (Modifier.isStatic(f0.getModifiers())) continue;

            if (!f0.canAccess(message)) f0.setAccessible(true);

            if (PacketModifier.isComponent(f0)) {
                FieldDesc fieldDesc = forType(f0.getType());

                fieldDesc.setOrdinal(f0.getDeclaredAnnotation(Component.class).ord());

                if (PacketModifier.isAny(f0)) fieldDesc.addModifier(FieldDesc.ANY_TYPE_MODIFIER);
                else if (PacketModifier.hasTypeSpec(f0)) {
                    fieldDesc.addModifier(TYPE_SPEC_MODIFIER);
                    fieldDesc.setType(f0.getDeclaredAnnotation(TypeSpec.class).value());
                }

                if (PacketModifier.isOptional(f0)) fieldDesc.addModifier(OPTIONAL_MODIFIER);
                else {
                    if (PacketModifier.isOneOf(f0)) fieldDesc.addModifier(ONE_OF_MODIFIER);
                    if (PacketModifier.isRepeated(f0)) fieldDesc.addModifier(REPEATED_MODIFIER);
                }

                try {
                    fieldDesc.setValue(f0.get(message));
                } catch (ReflectiveOperationException e) {/**/}
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

        String      rep  = new String(decryptedData);
        MessageDesc desc = new MessageDesc();
        desc.read(rep);

        try {
            String[] h = desc.getName().split("[:][:]");

            // This prevents the system from loading untrusted data simply by checking
            // if the class-simpleName is contained in the name-set
            Set<String> simpleNames = readable.stream().map(Class::getSimpleName).collect(Collectors.toSet());
            if (!simpleNames.contains(h[1]))
                throw new IllegalArgumentException("unknown message class: " + h[1]);

            Class<?> messageClass = Class.forName(h[0]);
            if (!readable.contains(messageClass))
                throw new IllegalArgumentException("unknown message class: " + messageClass.getSimpleName());

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
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("class is not implemented!");

        } catch (ReflectiveOperationException e) {
            throw new IllegalCallerException("could not initialize message object");
        }
    }

}
