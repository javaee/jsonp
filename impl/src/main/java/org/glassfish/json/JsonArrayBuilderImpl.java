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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

import org.glassfish.json.JsonObjectBuilderImpl.JsonObjectImpl;
import org.glassfish.json.api.BufferPool;

/**
 * JsonArrayBuilder impl
 *
 * @author Jitendra Kotamraju
 * @author Hendrik Saly
 */
class JsonArrayBuilderImpl implements JsonArrayBuilder {
    private ArrayList<JsonValue> valueList;
    private final BufferPool bufferPool;

    JsonArrayBuilderImpl(BufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    public JsonArrayBuilder add(JsonValue value) {
        validateValue(value);
        addValueList(value);
        return this;
    }

    public JsonArrayBuilder add(String value) {
        validateValue(value);
        addValueList(new JsonStringImpl(value));
        return this;
    }

    public JsonArrayBuilder add(BigDecimal value) {
        validateValue(value);
        addValueList(JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonArrayBuilder add(BigInteger value) {
        validateValue(value);
        addValueList(JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonArrayBuilder add(int value) {
        addValueList(JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonArrayBuilder add(long value) {
        addValueList(JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonArrayBuilder add(double value) {
        addValueList(JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    public JsonArrayBuilder add(boolean value) {
        addValueList(value ? JsonValue.TRUE : JsonValue.FALSE);
        return this;
    }

    public JsonArrayBuilder addNull() {
        addValueList(JsonValue.NULL);
        return this;
    }

    public JsonArrayBuilder add(JsonObjectBuilder builder) {
        if (builder == null) {
            throw new NullPointerException(JsonMessages.ARRBUILDER_OBJECT_BUILDER_NULL());
        }
        addValueList(builder.build());
        return this;
    }

    public JsonArrayBuilder add(JsonArrayBuilder builder) {
        if (builder == null) {
            throw new NullPointerException(JsonMessages.ARRBUILDER_ARRAY_BUILDER_NULL());
        }
        addValueList(builder.build());
        return this;
    }

    public JsonArray build() {
        List<JsonValue> snapshot;
        if (valueList == null) {
            snapshot = Collections.emptyList();
        } else {
            // Should we trim to minimize storage ?
            // valueList.trimToSize();
            snapshot = Collections.unmodifiableList(valueList);
        }
        valueList = null;
        return new JsonArrayImpl(snapshot, bufferPool);
    }

    private void addValueList(JsonValue value) {
        if (valueList == null) {
            valueList = new ArrayList<JsonValue>();
        }
        valueList.add(value);
    }

    private void validateValue(Object value) {
        if (value == null) {
            throw new NullPointerException(JsonMessages.ARRBUILDER_VALUE_NULL());
        }
    }

    static final class JsonArrayImpl extends AbstractList<JsonValue> implements JsonArray {
        private final List<JsonValue> valueList;    // Unmodifiable
        private final BufferPool bufferPool;

        JsonArrayImpl(List<JsonValue> valueList, BufferPool bufferPool) {
            this.valueList = valueList;
            this.bufferPool = bufferPool;
        }

        @Override
        public int size() {
            return valueList.size();
        }

        @Override
        public JsonObject getJsonObject(int index) {
            return (JsonObject)valueList.get(index);
        }

        @Override
        public JsonArray getJsonArray(int index) {
            return (JsonArray)valueList.get(index);
        }

        @Override
        public JsonNumber getJsonNumber(int index) {
            return (JsonNumber)valueList.get(index);
        }

        @Override
        public JsonString getJsonString(int index) {
            return (JsonString)valueList.get(index);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
            return (List<T>)valueList;
        }

        @Override
        public String getString(int index) {
            return getJsonString(index).getString();
        }

        @Override
        public String getString(int index, String defaultValue) {
            try {
                return getString(index);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public int getInt(int index) {
            return getJsonNumber(index).intValue();
        }

        @Override
        public int getInt(int index, int defaultValue) {
            try {
                return getInt(index);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public boolean getBoolean(int index) {
            JsonValue jsonValue = get(index);
            if (jsonValue == JsonValue.TRUE) {
                return true;
            } else if (jsonValue == JsonValue.FALSE) {
                return false;
            } else {
                throw new ClassCastException();
            }
        }

        @Override
        public boolean getBoolean(int index, boolean defaultValue) {
            try {
                return getBoolean(index);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public boolean isNull(int index) {
            return valueList.get(index).equals(JsonValue.NULL);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.ARRAY;
        }

        @Override
        public JsonValue get(int index) {
            return valueList.get(index);
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
            return new MutableJsonArray(valueList, bufferPool, null);
        }

        MutableJsonStructure toMutableJsonStructure(Ancestor ancestor) {
            return new MutableJsonArray(valueList, bufferPool, ancestor);
        }
        
    }

    private static final class MutableJsonArray extends AbstractMutableJsonStructure {

        private final List<GenericJsonValue> mutableList;
        private final BufferPool bufferPool;

        private MutableJsonArray(List<JsonValue> list, BufferPool bufferPool, Ancestor ancestor) {
            super(JsonValue.ValueType.ARRAY, ancestor);

            mutableList = new ArrayList<GenericJsonValue>();

            int i = 0;
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                JsonValue value = (JsonValue) iterator.next();
                final Ancestor ca = new AncestorImpl(this, i);

                if (value.getValueType().equals(JsonValue.ValueType.ARRAY)) {
                    mutableList.add(new GenericJsonValue(((JsonArrayImpl) value).toMutableJsonStructure(ca)));
                } else if (value.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                    mutableList.add(new GenericJsonValue(((JsonObjectImpl) value).toMutableJsonStructure(ca)));
                } else {
                    mutableList.add(new GenericJsonValue(value, ca));
                }
                i++;
            }

            this.bufferPool = bufferPool;
        }

        @Override
        public JsonValue getLeaf(int index) {
            throwIfNotArray();
            try {
                GenericJsonValue genericJsonValue = mutableList.get(index);
                if (genericJsonValue.isJsonValue()) {
                    return genericJsonValue.getJsonValue();
                }
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }

            throw new JsonException("not a value");
        }

        @Override
        public final MutableJsonStructure set(int index, JsonValue value) {
            throwIfNotArray();
            try {
                mutableList.set(index, new GenericJsonValue(value, getAncestor()));
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }
            return this;
        }

        @Override
        public final MutableJsonStructure add(int index, JsonValue value) {
            throwIfNotArray();
            try {
                mutableList.add(index, new GenericJsonValue(value, getAncestor()));
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }
            return this;
        }

        @Override
        public final MutableJsonStructure add(JsonValue value) {
            throwIfNotArray();
            mutableList.add(new GenericJsonValue(value, getAncestor()));
            return this;
        }

        @Override
        public MutableJsonStructure get(int index) {
            try {
                GenericJsonValue genericJsonValue = mutableList.get(index);
                if (!genericJsonValue.isJsonValue()) {
                    return genericJsonValue.getMutableStructure();
                }
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }

            throw new JsonException("not a mutable structure");
        }

        @Override
        public MutableJsonStructure set(int index, MutableJsonStructure value) {
            try {
                mutableList.set(index, new GenericJsonValue(value));
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }
            return this;
        }

        @Override
        public MutableJsonStructure remove(int index) {
            try {
                mutableList.remove(index);
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }
            return this;
        }

        @Override
        public MutableJsonStructure add(int index, MutableJsonStructure value) {
            try {
                mutableList.add(index, new GenericJsonValue(value));
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }
            return this;
        }

        @Override
        public MutableJsonStructure add(MutableJsonStructure value) {
            mutableList.add(new GenericJsonValue(value));
            return this;
        }

        @Override
        public JsonStructure toJsonStructure() {

            JsonArrayBuilder builder = Json.createArrayBuilder();

            for (Iterator<GenericJsonValue> iterator = mutableList.iterator(); iterator.hasNext();) {
                GenericJsonValue genericJsonValue = iterator.next();

                if (genericJsonValue.isJsonValue()) {
                    builder.add(genericJsonValue.getJsonValue());
                } else {
                    builder.add(genericJsonValue.getMutableStructure().toJsonStructure());
                }

            }

            return builder.build();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mutableList == null) ? 0 : mutableList.hashCode());
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
            MutableJsonArray other = (MutableJsonArray) obj;
            if (mutableList == null) {
                if (other.mutableList != null)
                    return false;
            } else if (!mutableList.equals(other.mutableList))
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            try (JsonWriter jw = new JsonWriterImpl(sw, bufferPool)) {
                jw.write((JsonArray) this.toJsonStructure());
            }
            return sw.toString();
        }

        @Override
        public int size() {
            return mutableList.size();
        }

        @Override
        public JsonValue getLeaf(@SuppressWarnings("unused") String key) {
            throwIfNotObject();
            return null;
        }
        
        @Override
        public boolean isLeaf(int index) {
            GenericJsonValue genericJsonValue = null;
            try {
                genericJsonValue = mutableList.get(index);
            } catch (IndexOutOfBoundsException e) {
                throw new JsonException("invalid index "+index);
            }

            return genericJsonValue.isJsonValue();
        }

        @Override
        public boolean isLeaf(String key) {
            throwIfNotObject();
            throw new RuntimeException("cannot happen");
        }

        @Override
        public MutableJsonStructure set(MutableJsonStructure value) {
            mutableList.clear();
            mutableList.addAll(((MutableJsonArray) value).mutableList);
            return this;
        }
    }
}

