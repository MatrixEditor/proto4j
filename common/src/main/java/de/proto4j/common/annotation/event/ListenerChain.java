package de.proto4j.common.annotation.event;//@date 31.12.2021

import de.proto4j.common.annotation.AnnotationUtil;
import de.proto4j.common.annotation.Item;

import java.util.ArrayList;
import java.util.List;

public class ListenerChain<EVENT> {

    @Item(name = "listeners", hasSetter = false, isAccessible = false)
    private final List<Object> listeners = new ArrayList<>();

    /**
     * Processes the given input by processing every individual listener that is
     * stored. The process starts from 0 to the amount of listeners.
     *
     * @param event the input
     * @throws Exception if an error occurs
     */
    public void process(EVENT event) throws Exception {
        process0(event, false);
    }

    /**
     * Processes the given input by processing every individual listener that is
     * stored. The process starts from 0 to the amount of listeners. Additionally,
     * the listener which handles the input is removed afterwards.
     *
     * @param event the input
     * @throws Exception if an error occurs
     */
    public void processOnRemove(EVENT event) throws Exception {
        process0(event, true);
    }

    private void process0(EVENT event, boolean remove) throws Exception {
        if (amount() > 0) {
            for (int i = 0; i < listeners.size(); i++) {
                Object        o  = listeners.get(i);
                EventListener el = AnnotationUtil.lookup(o.getClass(), EventListener.class, o.getClass()::getAnnotation);

                if (el != null) {
                    AnnotationUtil.supply(el.method(), o, event);
                    if (remove) remove(i);
                }
                else remove(i);
            }
        }
    }

    /**
     * Returns the amount of stored rules in this Engine.
     *
     * @return the amount of stored rules
     */
    public int amount() {
        return listeners.size();
    }

    /**
     * Removes a rule form the stored ones at the index position.
     *
     * @param index the position of the rule
     */
    public void remove(int index) {
        if (index >= 0 && index < listeners.size())
            listeners.remove(index);
    }

    /**
     * Returns the Rule at the given index position.
     *
     * @param index the position
     * @return the listener at the given index
     */
    public Object get(int index) {
        if (index >= 0 && index < listeners.size())
            return listeners.get(index);
        else
            return null;
    }

    /**
     * Adds a listener to the stored ones.
     *
     * @param listener the listener to add
     */
    public void add(Object listener) {
        if (listener != null) {
            if (AnnotationUtil.lookup(listener.getClass(), EventListener.class) != null
                    && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }
}
