package de.proto4j.json; //@date 13.11.2021

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.*;

/**
 * A small ~125-line JsonParser implementation with a performance of more or
 * less 120 milliseconds per 50k lines of Json-code. Another option could be
 * the usage of the Gson-library. This parser uses the {@link Stack} implementation
 * of the java-framework to navigate over JSON-objects.
 *
 * @see JsonProperty
 * @see JsonSimpleProperty
 * @see JsonArray
 * @see JsonObject
 */
public final class JsonParser {

    private static final int NULL       = 0;
    private static final int PENDING    = 8;
    private static final int PENDING_PR = 9;

    private static final int OJ_ = 1;
    private static final int AR_ = 2;

    public static String fileToString(File file, String delim) throws IOException {
        Objects.requireNonNull(file);

        FileReader     fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        return String.join(delim, br.lines().toArray(String[]::new));
    }

    public JsonObject parse(String script) throws ParseException {
        JsonObject          jsonFile = new JsonObject(Collections.emptyList());
        Stack<Integer>      types    = new Stack<>();
        Stack<JsonProperty> p_s      = new Stack<>();
        StringBuilder       literal  = new StringBuilder();
        String              last     = null;

        char[]  script_chars = script.toCharArray();
        char    c0;
        boolean inLiteral    = false;

        types.add(NULL);
        p_s.add(jsonFile);
        for (int i = 0; i < script_chars.length; i++) {
            c0 = script_chars[i];

            if (!inLiteral && Character.isWhitespace(c0))
                continue;

            if (types.peek() == NULL) {
                if (c0 == '{') {
                    types.add(OJ_);
                } else throw new ParseException("", i);
            }

            if (c0 == '"') {
                inLiteral = !inLiteral;
                // here we have a simple Property
                if (types.peek() == PENDING) {
                    last = literal.toString();
                    types.pop();
                    literal.delete(0, literal.length());

                    if (types.peek() == AR_) {
                        p_s.peek().asArray().properties().add(new JsonSimpleProperty("", last));
                        continue;
                    }
                    types.add(PENDING_PR);
                }
                if (types.peek() != PENDING && types.peek() != PENDING_PR) types.add(PENDING);
                continue;
            }

            if (inLiteral) {
                literal.append(c0);
                continue;
            }

            switch (types.peek()) {
                case NULL:
                    throw new ParseException("", i);

                case PENDING_PR:
                    if (c0 == ':') continue;

                    if (c0 == '[') {
                        types.pop();
                        types.add(AR_);
                        p_s.add(new JsonArray(last, Collections.emptyList()));
                    } else if (c0 == '{') {
                        types.pop(); //remove PENDING
                        types.add(OJ_);
                        p_s.add(new JsonObject(last, Collections.emptyList()));
                    } else {
                        if (Character.isDigit(c0)) {
                            literal.append(c0);
                            continue;
                        }
                        JsonSimpleProperty jsp = new JsonSimpleProperty(last, literal.toString());
                        jsp.setValue(literal.toString());
                        lowerLevel(p_s, i, jsp);
                        types.pop();
                        if (c0 == '}' || c0 == ']') i--;
                    }
                    literal.delete(0, literal.length());
                    last = "";
                    break;

                case OJ_:
                    if (c0 == ',') continue;

                    if (c0 == '}') {
                        types.pop();
                        JsonObject jo = p_s.pop().asObject();
                        lowerLevel(p_s, i, jo);
                    } else if (c0 == ']') throw new ParseException("", i);
                    break;

                case AR_:
                    if (c0 == ',') continue;

                    if (c0 == ']') {
                        types.pop();
                        JsonArray ja = p_s.pop().asArray();
                        lowerLevel(p_s, i, ja);
                    } else if (c0 == '}') {
                        throw new ParseException("Unexpected closing brace", i);
                    } else if (c0 == '{') {
                        types.add(OJ_);
                        p_s.add(new JsonObject(last, Collections.emptyList()));
                        literal.delete(0, literal.length());
                        last = "";
                    } else if (c0 == '[') {
                        types.add(AR_);
                        p_s.add(new JsonArray("", Collections.emptyList()));
                        literal.delete(0, literal.length());
                    }
                    break;
            }
        }
        return jsonFile;
    }

    public JsonObject parse(File file) throws ParseException, IOException {
        return parse(fileToString(file, " "));
    }

    public <T> T load(String script, Class<T> tClass) throws ReflectiveOperationException, ParseException {
        JsonObject o      = parse(script);
        return load0(o, tClass);
    }

    public <T> T load(File file, Class<T> tClass) throws IOException, ReflectiveOperationException, ParseException {
        return load(fileToString(file, "\n"), tClass);
    }

    public void dump(JsonObject object, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(object.toJson());
            writer.flush();
        }
    }

    public void dump(Object o, File file) throws IOException, IllegalAccessException {
        if (o instanceof JsonObject) dump((JsonObject) o, file);
        else try (FileWriter writer = new FileWriter(file)) {
            StringJoiner sj0 = new StringJoiner("");
            dumpObject0(o, sj0);
            writer.write(sj0.toString());
            writer.flush();
        }
    }

    private void dumpObject0(Object o, StringJoiner sj0) throws IllegalAccessException {
        dumpObject0(o, "", sj0);
    }

    private void dumpObject0(Object o, String __name, StringJoiner sj0) throws IllegalAccessException {
        StringJoiner sj1 = new StringJoiner(", ", __name.length() > 0 ? "\"" +__name + "\":{" : "{", "}");
        for (Field f : o.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                if (!f.canAccess(o)) f.setAccessible(true);
                if (f.getType().isArray()) {
                    StringJoiner sjA = new StringJoiner(",", "\"" +f.getName() + "\":[", "]");
                    dumpArray0(f.get(o), sjA);
                    sj1.add(sjA.toString());
                } else if (f.getType().isPrimitive()) sj1.add(String.format("\"%s\": \"%s\"", f.getName(),
                                                                            f.get(o).toString()));
                else {
                    dumpObject0(f.get(o), f.getName(), sj1);
                }
            }
        }
        sj0.add(sj1.toString());
    }

    private void dumpArray0(Object o, StringJoiner sj0) {
        for (int i = 0; ; i++) try {
            Object o1 = Array.get(o, i);
            if (o1 == null) continue;

            if (o1.getClass().isArray()) {
                StringJoiner sj1 = new StringJoiner(",", "[", "]");
                dumpArray0(o1, sj1);
                sj0.add(sj1.toString());
            } else if (o1.getClass().isPrimitive() || o1.getClass() == String.class) {
                sj0.add(String.format("\"%s\"", o1.toString()));
            }
            else {
                dumpObject0(o1, sj0);
            }
        } catch (IndexOutOfBoundsException | IllegalAccessException ex) { break; }
    }

    private boolean classSet(Field f, Object ref, Object t, Class<?>...types) throws IllegalAccessException {
        for (Class<?> c : types) {
            if (c == f.getType()) {
                f.set(ref, t);
                return true;
            }
        }
        return false;
    }

    private <R> R load0(JsonObject __o, Class<R> rClass) throws ReflectiveOperationException {
        R       r      = rClass.getDeclaredConstructor().newInstance();
        Field[] fields = r.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (!Modifier.isStatic(f.getModifiers())) {
                if (!f.canAccess(r)) f.setAccessible(true);
                JsonProperty p = __o.get(f.getName());
                if (p != null) {
                    if (p.isSimpleProperty()) {
                        if (classSet(f, r, p.asProperty().value(), String.class))
                            continue;

                        if (classSet(f, r, p.asProperty().getAsInt(), int.class, Integer.class))
                            continue;
                    } else {
                        if (p.isObject()) classSet(f, r, load0(p.asObject(), f.getType()), f.getType());
                        else {
                            JsonArray a = p.asArray();
                            Object[] a0 = (Object[]) Array.newInstance(f.getType().componentType(), a.properties().size());
                            List<JsonProperty> properties = a.properties();

                            for (int i = 0; i < properties.size(); i++) {
                                JsonProperty p0 = properties.get(i);
                                try {
                                    if (a0.getClass() == String[].class)
                                        a0[i] = p0.asProperty().getAsString();
                                    else if(a0.getClass() == Integer[].class)
                                        a0[i] = p0.asProperty().getAsInt();
                                    else if(a0.getClass() == Boolean[].class)
                                        a0[i] = p0.asProperty().getAsBoolean();
                                } catch (Exception ex) {
                                    // continue;
                                }
                            }
                            classSet(f, r, a0, a0.getClass());
                        }
                    };
                }
            }
        }
        return r;
    }

    private void lowerLevel(Stack<JsonProperty> p_s, int i, JsonProperty ja) throws ParseException {
        if (!p_s.isEmpty()) {
            if (p_s.peek().isArray()) {
                p_s.peek().asArray().properties().add(ja);
            } else if (p_s.peek().isObject()) {
                p_s.peek().asObject().add(ja);
            } else throw new ParseException("Error in object stack.", i);
        }
    }
}
