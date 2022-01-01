package de.proto4j.common.json; //@date 13.11.2021

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Objects;
import java.util.Stack;

/**
 * A small 125-line JsonParser implementation with a performance of more or
 * less 120 milliseconds per 50k lines of Json-code. Another option could be
 * the usage of the Gson-library. This parser uses the {@link Stack} implementation
 * of the java-framework.
 *
 * @see JsonProperty
 */
public class JsonParser {

    public static final int NULL       = 0;
    public static final int PENDING    = 8;
    public static final int PENDING_PR = 9;

    public static final int OJ_ = 1;
    public static final int AR_ = 2;

    public static String fileToString(File file, String delim) throws IOException {
        Objects.requireNonNull(file);

        FileReader     fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        return String.join(delim, br.lines().toArray(String[]::new));
    }

    public static JsonObject parse(String script) throws ParseException {
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
                        literal.delete(0, literal.length());
                    } else if (c0 == '{') {
                        types.pop(); //remove PENDING
                        types.add(OJ_);
                        p_s.add(new JsonObject(last, Collections.emptyList()));
                        literal.delete(0, literal.length());
                    } else {
                        JsonSimpleProperty jsp = new JsonSimpleProperty(last, literal.toString());
                        jsp.setValue(literal.toString());
                        lowerLevel(p_s, i, jsp);
                        types.pop();
                        literal.delete(0, literal.length());
                    }
                    break;

                case OJ_:
                    if (c0 == ',') continue;

                    if (c0 == '}') {
                        types.pop();
                        JsonObject jo = p_s.pop().asObject();
                        if (!p_s.empty()) {
                            if (p_s.peek().isArray()) {
                                p_s.peek().asArray().properties().add(jo);
                            } else if (p_s.peek().isObject()) {
                                p_s.peek().asObject().add(jo);
                            } else throw new ParseException("", i);
                        }
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
                    }
                    break;
            }
        }
        return jsonFile;
    }

    private static void lowerLevel(Stack<JsonProperty> p_s, int i, JsonProperty ja) throws ParseException {
        if (!p_s.isEmpty()) {
            if (p_s.peek().isArray()) {
                p_s.peek().asArray().properties().add(ja);
            } else if (p_s.peek().isObject()) {
                p_s.peek().asObject().add(ja);
            } else throw new ParseException("Error in object stack.", i);
        } else throw new ParseException("", i);
    }
}
