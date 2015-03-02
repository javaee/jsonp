/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.json;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.MutableJsonStructure;
import javax.json.MutableJsonStructure.Ancestor;

import org.glassfish.json.JsonArrayBuilderImpl.JsonArrayImpl;
import org.glassfish.json.api.BufferPool;

/**
 * JsonObjectBuilder impl
 *
 * @author Jitendra Kotamraju
 * @author Hendrik Saly
 */
class JsonObjectBuilderImpl implements JsonObjectBuilder {
    private Map<String, JsonValue> valueMap;
    private final BufferPool bufferPool;

    JsonObjectBuilderImpl(BufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    public JsonObjectBuilder add(String name, JsonValue value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, value);
        return this;
    }

    public JsonObjectBuilder add(String name, String value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, new JsonStringImpl(value));
        return this;
    }

    public JsonObjectBuilder add(String name, BigInteger value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonObjectBuilder add(String name, BigDecimal value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonObjectBuilder add(String name, int value) {
        validateName(name);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonObjectBuilder add(String name, long value) {
        validateName(name);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonObjectBuilder add(String name, double value) {
        validateName(name);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonObjectBuilder add(String name, boolean value) {
        validateName(name);
        putValueMap(name, value ? JsonValue.TRUE : JsonValue.FALSE);
        return this;
    }

    public JsonObjectBuilder addNull(String name) {
        validateName(name);
        putValueMap(name, JsonValue.NULL);
        return this;
    }

    public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
        validateName(name);
        if (builder == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_OBJECT_BUILDER_NULL());
        }
        putValueMap(name, builder.build());
        return this;
    }

    public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
        validateName(name);
        if (builder == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_ARRAY_BUILDER_NULL());
        }
        putValueMap(name, builder.build());
        return this;
    }

    public JsonObject build() {
        Map<String, JsonValue> snapshot = (valueMap == null)
                ? Collections.<String, JsonValue>emptyMap()
                : Collections.unmodifiableMap(valueMap);
        valueMap = null;
        return new JsonObjectImpl(snapshot, bufferPool);
    }

    private void putValueMap(String name, JsonValue value) {
        if (valueMap == null) {
            this.valueMap = new LinkedHashMap<String, JsonValue>();
        }
        valueMap.put(name, value);
    }

    private void validateName(String name) {
        if (name == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_NAME_NULL());
        }
    }

    private void validateValue(Object value) {
        if (value == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_VALUE_NULL());
        }
    }

    static final class JsonObjectImpl extends AbstractMap<String, JsonValue> implements JsonObject {
        private final Map<String, JsonValue> valueMap;      // unmodifiable
        private final BufferPool bufferPool;

        JsonObjectImpl(Map<String, JsonValue> valueMap, BufferPool bufferPool) {
            this.valueMap = valueMap;
            this.bufferPool = bufferPool;
        }

        @Override
        public JsonArray getJsonArray(String name) {
            return (JsonArray)get(name);
        }

        @Override
        public JsonObject getJsonObject(String name) {
            return (JsonObject)get(name);
        }

        @Override
        public JsonNumber getJsonNumber(String name) {
            return (JsonNumber)get(name);
        }

        @Override
        public JsonString getJsonString(String name) {
            return (JsonString)get(name);
        }

        @Override
        public String getString(String name) {
            return getJsonString(name).getString();
        }

        @Override
        public String getString(String name, String defaultValue) {
            try {
                return getString(name);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public int getInt(String name) {
            return getJsonNumber(name).intValue();
        }

        @Override
        public int getInt(String name, int defaultValue) {
            try {
                return getInt(name);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public boolean getBoolean(String name) {
            JsonValue value = get(name);
            if (value == null) {
                throw new NullPointerException();
            } else if (value == JsonValue.TRUE) {
                return true;
            } else if (value == JsonValue.FALSE) {
                return false;
            } else {
                throw new ClassCastException();
            }
        }

        @Override
        public boolean getBoolean(String name, boolean defaultValue) {
            try {
                return getBoolean(name);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public boolean isNull(String name) {
            return get(name).equals(JsonValue.NULL);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.OBJECT;
        }

        @Override
        public Set<Entry<String, JsonValue>> entrySet() {
            return valueMap.entrySet();
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            try (JsonWriter jw = new JsonWriterImpl(sw, bufferPool)) {
                jw.write(this);
            }
            return sw.toString();
        }

        @Override
        public MutableJsonStructure toMutableJsonStructure() {
            return new MutableJsonObject(valueMap, bufferPool, null);
        }
        
        MutableJsonStructure toMutableJsonStructure(Ancestor ancestor) {
            return new MutableJsonObject(valueMap, bufferPool, ancestor);
        }

    }
    
    private static final class MutableJsonObject extends AbstractMutableJsonStructure {

        private final Map<String, GenericJsonValue> mutableMap;
        private final BufferPool bufferPool;

        private MutableJsonObject(Map<String, JsonValue> map, BufferPool bufferPool, Ancestor ancestor) {
            super(JsonValue.ValueType.OBJECT, ancestor);

            this.mutableMap = new LinkedHashMap<String, GenericJsonValue>();

            for (Iterator<Entry<String, JsonValue>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, JsonValue> entry = iterator.next();
                JsonValue value = entry.getValue();
                final Ancestor ca = new AncestorImpl(this, entry.getKey());

                if (value.getValueType().equals(JsonValue.ValueType.ARRAY)) {
                    mutableMap.put(entry.getKey(), new GenericJsonValue(((JsonArrayImpl) value).toMutableJsonStructure(ca)));
                } else if (value.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                    mutableMap.put(entry.getKey(), new GenericJsonValue(((JsonObjectImpl) value).toMutableJsonStructure(ca)));
                } else {
                    mutableMap.put(entry.getKey(), new GenericJsonValue(value, ca));
                }

            }

            this.bufferPool = bufferPool;
        }

        @Override
        public JsonValue getLeaf(String key) {
            throwIfNotObject();

            if (!mutableMap.containsKey(key)) {
                throw new JsonException("no such key: '" + key + "'");
            }

            GenericJsonValue genericJsonValue = mutableMap.get(key);
            if (genericJsonValue.isJsonValue()) {
                return genericJsonValue.getJsonValue();
            }

            throw new JsonException("not a value");

        }

        @Override
        public final MutableJsonStructure set(String key, JsonValue value) {
            throwIfNotObject();

            if (!mutableMap.containsKey(key)) {
                throw new JsonException("no such key: '" + key + "'");
            }

            mutableMap.replace(key, new GenericJsonValue(value, getAncestor()));
            return this;
        }

        @Override
        public final MutableJsonStructure add(String key, JsonValue value) {
            throwIfNotObject();
            mutableMap.put(key, new GenericJsonValue(value, getAncestor()));
            return this;
        }

        @Override
        public MutableJsonStructure get(String key) {
            GenericJsonValue genericJsonValue = mutableMap.get(key);

            if (genericJsonValue == null) {
                throw new JsonException("no such key: '" + key + "'");
            }

            if (!genericJsonValue.isJsonValue()) {
                return genericJsonValue.getMutableStructure();
            }

            throw new JsonException("not a mutable structure");
        }

        @Override
        public MutableJsonStructure set(String key, MutableJsonStructure value) {

            if (!mutableMap.containsKey(key)) {
                throw new JsonException("no such key: '" + key + "'");
            }

            mutableMap.replace(key, new GenericJsonValue(value));
            return this;
        }

        @Override
        public MutableJsonStructure remove(String key) {

            if (!mutableMap.containsKey(key)) {
                throw new JsonException("no such key: '" + key + "'");
            }

            mutableMap.remove(key);
            return this;
        }

        @Override
        public MutableJsonStructure add(String key, MutableJsonStructure value) {
            mutableMap.put(key, new GenericJsonValue(value));
            return this;
        }

        @Override
        public JsonStructure toJsonStructure() {
            JsonObjectBuilder builder = Json.createObjectBuilder();

            for (Iterator<Entry<String, GenericJsonValue>> iterator = mutableMap.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, GenericJsonValue> entry = iterator.next();

                if (entry.getValue().isJsonValue()) {
                    builder.add(entry.getKey(), entry.getValue().getJsonValue());
                } else {
                    builder.add(entry.getKey(), entry.getValue().getMutableStructure().toJsonStructure());
                }
            }

            return builder.build();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mutableMap == null) ? 0 : mutableMap.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MutableJsonObject other = (MutableJsonObject) obj;
            if (mutableMap == null) {
                if (other.mutableMap != null)
                    return false;
            } else if (!mutableMap.equals(other.mutableMap))
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            try (JsonWriter jw = new JsonWriterImpl(sw, bufferPool)) {
                jw.write((JsonObject) this.toJsonStructure());
            }
            return sw.toString();
        }

        @Override
        public int size() {
            return mutableMap.size();
        }

        @Override
        public Set<String> getKeys() {
            return mutableMap.keySet();
        }

        @Override
        public JsonValue getLeaf(@SuppressWarnings("unused") int index) {
            throwIfNotArray();
            return null;
        }

        @Override
        public boolean isLeaf(@SuppressWarnings("unused") int index) {
            throwIfNotArray();
            throw new RuntimeException("cannot happen");
        }

        @Override
        public boolean isLeaf(String key) {

            if (!mutableMap.containsKey(key)) {
                throw new JsonException("no such key: '" + key + "'");
            }

            GenericJsonValue genericJsonValue = mutableMap.get(key);
            return genericJsonValue.isJsonValue();
        }

        @Override
        public MutableJsonStructure set(MutableJsonStructure value) {
            mutableMap.clear();
            mutableMap.putAll(((MutableJsonObject) value).mutableMap);
            return this;
        }
    }
    
}
