package proto4j.json; //@date 13.11.2021

import java.util.*;

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

    public boolean contains(String tag) {
        return getTag().length() > 0 && tag.equals(getTag());
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
        StringJoiner sj = new StringJoiner(",", "[", "]");
        propertyList.forEach(p -> sj.add(p.toJson()));
        if (getTag().length() == 0) return sj.toString();
        return "\""+getTag()+"\":" + sj.toString();
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
