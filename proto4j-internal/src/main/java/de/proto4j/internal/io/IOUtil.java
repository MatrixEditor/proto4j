package de.proto4j.internal.io; //@date 29.01.2022

import de.proto4j.annotation.message.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class IOUtil {

    public static final char LF = '\n';
    public static final char RF = '\r';

    public static final String DEFAULT_DELIMITER = "-";

    public static final String LF_REPLACEMENT        = "%n";
    public static final String RF_REPLACEMENT        = "%r";
    public static final String DELIMITER_REPLACEMENT = "%M";

    public static final int OPTIONAL_MODIFIER  = 0b00000001;
    public static final int ANY_TYPE_MODIFIER  = 0b00000010;
    public static final int ONE_OF_MODIFIER    = 0b00000100;
    public static final int REPEATED_MODIFIER  = 0b00001000;
    public static final int TYPE_SPEC_MODIFIER = 0b00010000;

    private static final List<Class<?>> primitiveTypes =
            List.of(int.class, Integer.class, byte.class, Byte.class, double.class, Double.class,
                    short.class, Short.class, float.class, Float.class, char.class, Character.class,
                    boolean.class, Boolean.class);

    private static final Map<Class<?>, Function<String, ?>> mappings = new HashMap<>();

    static {
        mappings.put(Integer.class, Integer::parseInt);
        mappings.put(int.class, Integer::parseInt);
        mappings.put(Boolean.class, Boolean::parseBoolean);
        mappings.put(boolean.class, Boolean::parseBoolean);
        mappings.put(char.class, s -> s.charAt(0));

        mappings.put(String.class, s -> s.replaceAll(DELIMITER_REPLACEMENT, DEFAULT_DELIMITER)
                                         .replaceAll(RF_REPLACEMENT, "\r")
                                         .replaceAll(LF_REPLACEMENT, "\n"));
    }

    private IOUtil() {
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
    public static StringBuffer allocate(Object message) {
        if (message == null) throw new NullPointerException("message is null");

        Class<?> messageClass = message.getClass();
        if (!messageClass.isAnnotationPresent(Message.class)) {
            throw new IllegalArgumentException("Object is not an instance of Message.class");
        }

        StringBuffer buf  = new StringBuffer();
        MessageDesc  desc = new MessageDesc();

        String sName = "::" + messageClass.getSimpleName();
        if (sName.length() > 2) {
            desc.setHeader(messageClass.getName() + sName);
        } else desc.setHeader(messageClass.getName());

        for (Field f0 : messageClass.getDeclaredFields()) {
            if (Modifier.isStatic(f0.getModifiers())) continue;

            if (!f0.canAccess(message)) f0.setAccessible(true);

            if (PacketModifier.isComponent(f0)) {
                FieldDesc fieldDesc = new FieldDesc();

                fieldDesc.setOrd(f0.getDeclaredAnnotation(Component.class).ord());

                if (PacketModifier.isAny(f0)) fieldDesc.addModifier(ANY_TYPE_MODIFIER);
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
                    fieldDesc.value = f0.get(message);
                } catch (ReflectiveOperationException e) {/**/}
                desc.getFields().add(fieldDesc);
            }

        }
        buf.append(toString(desc));
        return buf;
    }

    public static Object convert(byte[] decryptedData, Set<Class<?>> readable) {
        if (decryptedData.length == 0)
            throw new IllegalArgumentException("data cannot be 0");

        //if (decryptedData.length % Proto4jWriter.SHARED_KEY.length != 0)
          //throw new IllegalArgumentException("data has to be a multiple of sharedKey.length");

        String      rep  = new String(decryptedData);
        MessageDesc desc = from(rep);

        try {
            String[] h = desc.getHeader().split("[:][:]");

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
                        if (f.ord == f0.getDeclaredAnnotation(Component.class).ord()) {
                            f0.setAccessible(true);
                            f0.set(message, f.value);
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

    private static MessageDesc from(String rep) {
        if (rep == null) throw new NullPointerException("representation is null");
        MessageDesc messageDesc = new MessageDesc();

        StringTokenizer t = new StringTokenizer(rep, "\n");
        for (int i = 0; t.hasMoreElements(); i++) {
            String s = t.nextToken();

            if (i == 0) messageDesc.setHeader(s);
            else {
                if (i == 1) {
                    StringTokenizer tF = new StringTokenizer(s, "\r");

                    while (tF.hasMoreElements()) {
                        messageDesc.getFields().add(fieldFrom(tF.nextToken()));
                    }
                    break;
                }
            }
        }
        return messageDesc;
    }

    private static FieldDesc fieldFrom(String s) {
        if (s == null) throw new NullPointerException("representation is null");
        String[] values = s.split("[-]");

        if (values.length != 5) {
            throw new IllegalArgumentException("representation is damaged");
        }

        FieldDesc desc = new FieldDesc();
        desc.setOrd(Integer.parseInt(values[0]));
        desc.modifier = Integer.parseInt(values[1].substring(1, values[1].length() - 1));

        try {
            Class<?> type = Class.forName(values[2]);
            if (mappings.containsKey(type)) {
                desc.value = mappings.get(type).apply(values[4]);
            } else throw new UnsupportedOperationException("not implemented!");
        } catch (ClassNotFoundException e) {/**/}
        return desc;
    }

    private static String toString(MessageDesc desc) {
        StringBuilder sb = new StringBuilder();
        if (desc == null) throw new NullPointerException("description is null");

        sb.append(desc.header).append(LF);
        for (FieldDesc fd : desc.getFields()) {
            sb.append(toString(fd));
        }
        sb.append(LF).append(RF);
        return new String(sb);
    }

    private static String toString(FieldDesc fieldDesc) {
        if (fieldDesc == null) {
            throw new NullPointerException("field is null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(fieldDesc.ord).append("-[").append(fieldDesc.modifier).append("]-");
        sb.append(fieldDesc.value.getClass().getName()).append(DEFAULT_DELIMITER);

        if (fieldDesc.value instanceof String) {
            String r = ((String) fieldDesc.value).replaceAll(DEFAULT_DELIMITER, "%M")
                                                 .replaceAll("\r", "%r")
                                                 .replaceAll("\n", "%n");
            sb.append(r.length()).append(DEFAULT_DELIMITER).append(r).append(DEFAULT_DELIMITER).append(RF);
        } else if (primitiveTypes.contains(fieldDesc.value.getClass())) {
            String rep = fieldDesc.value.toString();
            sb.append(rep.length()).append(DEFAULT_DELIMITER).append(rep).append(DEFAULT_DELIMITER).append(RF);
        } else {
            if (fieldDesc.hasModifier(TYPE_SPEC_MODIFIER)) {
                try {
                    Serializer inst = fieldDesc.type.getDeclaredConstructor().newInstance();
                    String     s    = inst.serialize(fieldDesc.value);
                    sb.append(s.length()).append(DEFAULT_DELIMITER).append(s).append(DEFAULT_DELIMITER).append(RF);
                } catch (ReflectiveOperationException | IOException e) {/**/}
            } else {
                throw new UnsupportedOperationException("Any type not implemented!");
            }
        }
        return new String(sb);
    }

    private static class FieldDesc {
        private int modifier;
        private int ord;

        private Object                      value;
        private Class<? extends Serializer> type;

        public void addModifier(int m) {
            if (!hasModifier(m)) {
                modifier |= m;
            }
        }

        public boolean hasModifier(int m) {
            return (modifier & m) != 0;
        }

        public void setType(Class<? extends Serializer> type) {
            this.type = type;
        }

        public void setOrd(int ord) {
            this.ord = ord;
        }

    }

    private static class MessageDesc {
        private final List<FieldDesc> fields = new LinkedList<>();

        private String header;

        public MessageDesc() {
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public List<FieldDesc> getFields() {
            return fields;
        }
    }
}
