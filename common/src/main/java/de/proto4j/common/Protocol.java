package de.proto4j.common; //@date 30.12.2021


import de.proto4j.common.annotation.AnnotatedElement;
import de.proto4j.common.annotation.Item;
import de.proto4j.common.exception.ProtocolItemNotFoundException;

import java.io.Serializable;
import java.net.ProtocolFamily;
import java.util.HashMap;
import java.util.Map;

/**
 * The base representation of every protocol-definition that is created by the
 * user or implemented. Protocol-related constants should be declared in another
 * classes. This object contains information about what classes are used for the
 * <li>{@code Socket},
 * <li>{@code ServerSocket},
 * <li>{@code Client},
 * <li>{@code Server},
 * <li>{@code Packet},
 * <li>{@code PacketReader},
 * <li>{@code PacketHeader},
 * <li>and {@code PacketPayload}.</li>
 * <p>
 * Note that not all attributes have to be set in a protocol (everyone is optional). If
 * an attribute is not set, functionalities used in {@link ProtocolFactory} and
 * {@link ProtocolUtil} could not be accessed and throw exceptions. The
 * following tables shows which attributes should be set:
 * <pre>
 *     +-------------------------+------------------------+
 *     |        Attribute        |      Definition        |
 *     +-------------------------+------------------------+
 *     | PACKET_READER_ATTRIBUTE |        optional        |
 *     +-------------------------+------------------------+
 *     |    PACKET_ATTRIBUTE     |        optional        |
 *     +-------------------------+------------------------+
 *     |    HEADER_ATTRIBUTE     |        optional        |
 *     +-------------------------+------------------------+
 *     |    PAYLOAD_ATTRIBUTE    |        optional        |
 *     +-------------------------+------------------------+
 *     |    CLIENT_ATTRIBUTE     | only if SOCKET and     |
 *     |                         | PACKET_READER is       |
 *     |                         | defined                |
 *     +-------------------------+------------------------+
 *     |    SERVER_ATTRIBUTE     | only if SERVER_SOCKET  |
 *     |                         | is set                 |
 *     +-------------------------+------------------------+
 *     |    SOCKET_ATTRIBUTE     |   highly recommended   |
 *     +-------------------------+------------------------+
 *     | SERVER_SOCKET_ATTRIBUTE |        optional        |
 *     '--------------------------------------------------'
 * </pre>
 *
 * @see ProtocolFactory
 * @see ProtocolUtil
 */
public final class Protocol implements Serializable, AnnotatedElement, ProtocolFamily {

    /**
     * The packet-type class defined with this attribute. Important: this class
     * ahs to implement the {@link Serializable} interface. (optional)
     */
    public static final String PACKET_ATTRIBUTE = "attr.packet";

    /**
     * The packet reader class. (must if CLIENT_ATTRIBUTE is defined)
     */
    public static final String PACKET_READER_ATTRIBUTE = "attr.packet.reader";

    /**
     * The packet-header type class. (optional)
     */
    public static final String HEADER_ATTRIBUTE = "attr.header";

    /**
     * The packet-payload type class. (optional)
     */
    public static final String PAYLOAD_ATTRIBUTE = "attr.payload";

    /**
     * The client class. (only if SOCKET_ATTRIBUTE and PACKET_READER_ATTRIBUTE is defined)
     */
    public static final String CLIENT_ATTRIBUTE = "attr.client";

    /**
     * The server class. (only if SERVER_SOCKET_ATTRIBUTE is set)
     */
    public static final String SERVER_ATTRIBUTE = "attr.server";

    /**
     * The default socket class. (highly recommended)
     */
    public static final String SOCKET_ATTRIBUTE = "attr.socket";

    /**
     * The default server-socket class. (optional)
     */
    public static final String SERVER_SOCKET_ATTRIBUTE = "attr.server.socket";

    /**
     * The protocol-name which is used to identify this {@link Protocol}. It only can
     * be accessed by {@code de.proto4j.util.AnnotationUtil.get("name", protocolObject)}.
     *
     * @apiNote {@code de.proto4j.util.AnnotationUtil.get("name", protocolObject)}
     */
    @Item(name = "name", hasSetter = false)
    private final String name;

    /**
     * All necessary attributes defined in this protocol-object. This map only  can be
     * accessed by {@code de.proto4j.util.AnnotationUtil.get("attributes", protocolObject)}.
     *
     * @apiNote {@code de.proto4j.util.AnnotationUtil.get("attributes", protocolObject)}
     */
    @Item(name = "attributes", hasSetter = false)
    private final Map<String, Class<?>> attributes = new HashMap<>();

    /**
     * Creates a new Protocol with the specified name.
     *
     * @param name the protocol name
     */
    public Protocol(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the protocol family.
     *
     * @return the name of the protocol family
     */
    @Override
    public String name() {
        return name;
    }
}
