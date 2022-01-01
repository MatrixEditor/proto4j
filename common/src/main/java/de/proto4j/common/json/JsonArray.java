package de.proto4j.common.json; //@date 13.11.2021

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JsonArray implements JsonProperty, Iterable<JsonProperty> {

    private final List<JsonProperty> propertyList;
    private String                                     tag;

    protected JsonArray(String tag, Collection<? extends JsonProperty> properties) {
        this.tag = tag;
        this.propertyList = new ArrayList<>();

        propertyList.addAll(properties);
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void modifyTag(String tag) {
        this.tag = tag;
    }

    public List<JsonProperty> properties() {
        return propertyList;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public JsonArray asArray() {
        return this;
    }

    @Override
    public String toJson() {
        return null;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<JsonProperty> iterator() {
        return propertyList.iterator();
    }
}
