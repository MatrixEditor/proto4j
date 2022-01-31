package de.proto4j.internal.io.desc; //@date 31.01.2022

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import static de.proto4j.internal.io.desc.DescProviderFactory.LF;
import static de.proto4j.internal.io.desc.DescProviderFactory.RF;

public class MessageDesc implements ObjectDesc {

    private final List<FieldDesc> fields = new LinkedList<>();
    private Class<?> messageClass;

    public List<FieldDesc> getFields() {
        return fields;
    }

    public Class<?> getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(Class<?> messageClass) {
        this.messageClass = messageClass;
    }

    @Override
    public String getName() {
        return String.join("::", messageClass.getName(), messageClass.getSimpleName());
    }

    @Override
    public String serialize() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append(LF);
        for (FieldDesc fd : getFields()) {
            sb.append(fd.serialize());
        }
        sb.append(LF).append(RF);
        return new String(sb);
    }

    @Override
    public MessageDesc read(String serialized) throws IOException {
        if (serialized == null) throw new NullPointerException("representation is null");

        StringTokenizer t = new StringTokenizer(serialized, "\n");
        for (int i = 0; t.hasMoreElements(); i++) {
            String s = t.nextToken();

            if (i == 0) {
                try {
                    messageClass = Class.forName(s.split("[:][:]")[0]);
                } catch (ClassNotFoundException e) {
                    throw new IllegalCallerException("could not initialize messageClass");
                }
            } else {
                if (i == 1) {
                    StringTokenizer tF = new StringTokenizer(s, "\r");

                    while (tF.hasMoreElements()) {
                        String tk = tF.nextToken();
                        try {
                            Class<?>  type = Class.forName(tk.split("[-]")[2].split("[&]")[0]);
                            FieldDesc desc = DescProviderFactory.forType(type);

                            getFields().add((FieldDesc) desc.read(tk));
                        } catch (ClassNotFoundException e) {
                            throw new IllegalCallerException();
                        }
                    }
                    break;
                }
            }
        }
        return this;
    }
}
