package de.proto4j.json; //@date 13.11.2021

import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

public class JsonObject extends JsonArray {

    public static final String EMPTY_OBJECT_TAG = "";

    protected JsonObject() {
        this(Collections.emptyList());
    }

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

    @Override
    public boolean contains(String tag) {
        if (getTag().length() > 0 && tag.equals(getTag())) return true;
        else {
            for (JsonProperty p : properties()) {
                if (p.isSimpleProperty()) {
                    if (p.asProperty().getTag().equals(tag)) return true;
                } else if (p.isObject() && p.asObject().contains(tag)) return true;
                else if (p.isArray() && p.asArray().contains(tag)) return true;
            }
        }
        return false;
    }

    public JsonProperty get(String tag) {
        if (tag == null) return this;

        if (tag.length() > 0) {
            if (getTag().length() > 0 && getTag().equals(tag)) return this;

            for (JsonProperty p : properties()) {
                if (p.isSimpleProperty() || p.isArray()) {
                    if (p.getTag().equals(tag)) return p;
                } else {
                    if (p.asObject().contains(tag)) return p.asObject().get(tag);
                }
            }
        }
        return null;
    }

    @Override
    public String toJson() {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        properties().forEach(p -> sj.add(p.toJson()));
        if (getTag().length() == 0) return sj.toString();
        return "\""+getTag()+"\":" + sj.toString();
    }
}
