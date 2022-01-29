package proto4j.json;//@date 13.11.2021

public interface JsonProperty {

    String getTag();

    void modifyTag(String tag);

    boolean isSimpleProperty();

    boolean isArray();

    boolean isObject();

    default JsonObject asObject() {
        throw new IllegalArgumentException();
    }

    default JsonArray asArray() {
        throw new IllegalArgumentException();
    }

    default JsonSimpleProperty asProperty() {
        throw new IllegalArgumentException();
    }

    String toJson();
}
