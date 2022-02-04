package proto4j.serialization.desc;//@date 31.01.2022

import java.io.IOException;

/**
 * The base class for any type of Message-Object descriptions. It is recommended
 * to not implement any own features, because the linking into system invocation
 * is still unsafe and leads to resolving unsafe classes.
 *
 * @see FieldDesc
 * @see MessageDesc
 */
public interface ObjectDesc {

    /**
     * Returns the context-related name of this description.Usually the
     * {@link MessageDesc} returns <code>"ClassName::SimpleClassName"</code>
     * and an implementation of the {@link FieldDesc} its ordinal number.
     *
     * @return the name of this description.
     */
    //Info("context-related")
    String getName();

    /**
     * Tries to serialize this description of a field or message. The message
     * simply contains of its header (see {@link MessageDesc#getName()}) and
     * the serialized field descriptions.
     *
     * @return the serialized string which will be encrypted using AES
     * @throws IOException if an error occurs
     */
    String serialize() throws IOException;

    /**
     * Reads a non-encrypted, serialized string and converts it into an
     * {@link ObjectDesc}. The {@code FieldDesc#read()} is only called by
     * a {@link MessageDesc} to prevent getting errors.
     *
     *
     * @param serialized the serialized string
     * @return an {@link ObjectDesc} instance
     * @throws IOException if any error while reading occurs
     */
    //UnsafeOperation
    ObjectDesc read(String serialized) throws IOException;
}
