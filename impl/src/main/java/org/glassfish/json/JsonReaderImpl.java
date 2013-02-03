package org.glassfish.json;

import javax.json.*;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

class JsonReaderImpl implements JsonReader {
    private final JsonParser parser;
    private boolean readDone;
    private final Map<String, ?> config;

    JsonReaderImpl(Reader reader) {
        this(reader, Collections.<String, Object>emptyMap());
    }

    JsonReaderImpl(Reader reader, Map<String, ?> config) {
        JsonParserFactory factory = Json.createParserFactory(config);
        parser = factory.createParser(reader);
        this.config = factory.getConfigInUse();
    }

    JsonReaderImpl(InputStream in) {
        this(in, Collections.<String, Object>emptyMap());
    }

    JsonReaderImpl(InputStream in, Map<String, ?> config) {
        JsonParserFactory factory = Json.createParserFactory(config);
        parser = factory.createParser(in);
        this.config = factory.getConfigInUse();
    }

    JsonReaderImpl(InputStream in, Charset charset) {
        this(in, charset, Collections.<String, Object>emptyMap());
    }

    JsonReaderImpl(InputStream in, Charset charset, Map<String, ?> config) {
        JsonParserFactory factory = Json.createParserFactory(config);
        parser = factory.createParser(in, charset);
        this.config = factory.getConfigInUse();
    }

    @Override
    public JsonStructure read() {
        if (readDone) {
            throw new IllegalStateException("read/readObject/readArray/close method is already called.");
        }
        readDone = true;
        if (parser.hasNext()) {
            JsonParser.Event e = parser.next();
            if (e == JsonParser.Event.START_ARRAY) {
                return readArray(new JsonArrayBuilderImpl());
            } else if (e == JsonParser.Event.START_OBJECT) {
                return readObject(new JsonObjectBuilderImpl());
            } else {
                throw new JsonException("Cannot read JSON, parsing error. Parsing Event="+e);
            }
        }
        throw new JsonException("Cannot read JSON, possibly empty stream");
    }

    @Override
    public JsonObject readObject() {
        if (readDone) {
            throw new IllegalStateException("read/readObject/readArray/close method is already called.");
        }
        readDone = true;
        if (parser.hasNext()) {
            JsonParser.Event e = parser.next();
            if (e == JsonParser.Event.START_OBJECT) {
                return readObject(new JsonObjectBuilderImpl());
            } else if (e == JsonParser.Event.START_ARRAY) {
                throw new JsonException("Cannot read JSON object, found JSON array");
            } else {
                throw new JsonException("Cannot read JSON object, parsing error. Parsing Event="+e);
            }
        }
        throw new JsonException("Cannot read JSON object, possibly empty stream");
    }

    @Override
    public JsonArray readArray() {
        if (readDone) {
            throw new IllegalStateException("read/readObject/readArray/close method is already called.");
        }
        readDone = true;
        if (parser.hasNext()) {
            JsonParser.Event e = parser.next();
            if (e == JsonParser.Event.START_ARRAY) {
                return readArray(new JsonArrayBuilderImpl());
            } else if (e == JsonParser.Event.START_OBJECT) {
                throw new JsonException("Cannot read JSON array, found JSON object");
            } else {
                throw new JsonException("Cannot read JSON array, parsing error. Parsing Event="+e);
            }
        }
        throw new JsonException("Cannot read JSON array, possibly empty stream");
    }

    @Override
    public Map<String, ?> getConfigInUse() {
        return config;
    }

    @Override
    public void close() {
        readDone = true;
        parser.close();
    }

    private JsonArray readArray(JsonArrayBuilder builder) {
        while(parser.hasNext()) {
            JsonParser.Event e = parser.next();
            switch (e) {
                case START_ARRAY:
                    JsonArray array = readArray(new JsonArrayBuilderImpl());
                    builder.add(array);
                    break;
                case START_OBJECT:
                    JsonObject object = readObject(new JsonObjectBuilderImpl());
                    builder.add(object);
                    break;
                case VALUE_STRING:
                    String  string = parser.getString();
                    builder.add(string);
                    break;
                case VALUE_NUMBER:
                    BigDecimal bd = new BigDecimal(parser.getString());
                    builder.add(bd);
                    break;
                case VALUE_TRUE:
                    builder.add(true);
                    break;
                case VALUE_FALSE:
                    builder.add(false);
                    break;
                case VALUE_NULL:
                    builder.addNull();
                    break;
                case END_ARRAY:
                    return builder.build();
                default:
                    throw new JsonException("Internal Error");
            }
        }
        throw new JsonException("Internal Error");
    }

    private JsonObject readObject(JsonObjectBuilder builder) {
        String key = null;
        while(parser.hasNext()) {
            JsonParser.Event e = parser .next();
            switch (e) {
                case START_ARRAY:
                    JsonArray array = readArray(new JsonArrayBuilderImpl());
                    builder.add(key, array);
                    break;
                case START_OBJECT:
                    JsonObject object = readObject(new JsonObjectBuilderImpl());
                    builder.add(key, object);
                    break;
                case KEY_NAME:
                    key = parser.getString();
                    break;
                case VALUE_STRING:
                    String  string = parser.getString();
                    builder.add(key, string);
                    break;
                case VALUE_NUMBER:
                    BigDecimal bd = new BigDecimal(parser.getString());
                    builder.add(key, bd);
                    break;
                case VALUE_TRUE:
                    builder.add(key, true);
                    break;
                case VALUE_FALSE:
                    builder.add(key, false);
                    break;
                case VALUE_NULL:
                    builder.addNull(key);
                    break;
                case END_OBJECT:
                    return builder.build();
                default:
                    throw new JsonException("Internal Error");
            }
        }
        throw new JsonException("Internal Error");
    }

}