package de.proto4j.common.annotation.event; //@date 31.12.2021

import de.proto4j.common.annotation.AnnotatedElement;
import de.proto4j.common.annotation.Item;

/**
 * The {@link Event} is the base class which represents the Events produced by
 * the communication between the client and the Server or by the server itself.
 * <p><strong>WARNING: Events are not serializable, so no communication of events
 * through the network is possible</strong></p>
 */
public class Event implements AnnotatedElement {

    /**
     * The identifier od this Event.
     */
    @Item(name = "event.id", hasSetter = false)
    private final int id;

    /**
     * The source of this Event.
     */
    @Item(name = "event.src")
    private transient Object source;

    /**
     * Creates a new {@link Event} with the Event-ID.
     *
     * @param id the event-id
     */
    public Event(int id) {
        this(id, null);
    }

    /**
     * Creates a new {@link Event} with the Event-ID and the Event-source.
     *
     * @param id the event-id
     * @param source the event-source
     */
    public Event(int id, Object source) {
        this.id     = id;
        this.source = source;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    protected int getId() {
        return id;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    protected Object getSource() {
        return source;
    }

    /**
     * Sets a new source.
     *
     * @param source the source
     */
    protected void setSource(Object source) {
        this.source = source;
    }
}
