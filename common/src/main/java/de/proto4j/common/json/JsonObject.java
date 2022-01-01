package de.proto4j.common.json; //@date 13.11.2021

import java.util.Collection;

public class JsonObject extends JsonArray {

    public static final String EMPTY_OBJECT_TAG = null;

    protected JsonObject(Collection<? extends JsonProperty> properties) {
        super(EMPTY_OBJECT_TAG, properties);
    }

    public JsonObject(String tag, Collection<? extends JsonProperty> properties) {
        super(tag, properties);
    }

    public void add(JsonProperty jp) {
        properties().add(jp);
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public JsonObject asObject() {
        return this;
    }
}
