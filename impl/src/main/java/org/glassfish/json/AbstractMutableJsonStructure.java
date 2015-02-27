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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonPointer;
import javax.json.JsonPointer.PointerToken;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.MutableJsonStructure;

/**
 * 
 * @author Hendrik Saly
 *
 */
abstract class AbstractMutableJsonStructure implements MutableJsonStructure {

    private final ValueType valueType;
    private final Ancestor ancestor;

    AbstractMutableJsonStructure(ValueType valueType, Ancestor ancestor) {
        if (valueType == null) {
            throw new IllegalArgumentException();
        }

        this.valueType = valueType;
        this.ancestor = ancestor;
        throwIfNotStructure();

    }

    @Override
    public final Ancestor getAncestor() {
        return ancestor;
    }

    @Override
    public JsonStructure toJsonStructure() {
        throwIfNotStructure();
        return null;
    }

    @Override
    public int size() {
        throwIfNotStructure();
        return -1;
    }

    @Override
    public Set<String> getKeys() {
        throwIfNotObject();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure get(String key) {
        throwIfNotObject();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure set(String key, MutableJsonStructure value) {
        throwIfNotObject();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure remove(String key) {
        throwIfNotObject();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure add(String key, MutableJsonStructure value) {
        throwIfNotObject();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure get(int index) {
        throwIfNotArray();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure set(int index, MutableJsonStructure value) {
        throwIfNotArray();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure remove(int index) {
        throwIfNotArray();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure add(int index, MutableJsonStructure value) {
        throwIfNotArray();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure add(MutableJsonStructure value) {
        throwIfNotArray();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure set(int index, JsonValue value) {
        throwIfNotArray();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure add(int index, JsonValue value) {
        throwIfNotArray();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure set(String key, JsonValue value) {
        throwIfNotObject();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure add(String key, JsonValue value) {
        throwIfNotObject();
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public MutableJsonStructure add(JsonValue value) {
        throwIfNotArray();
        return null;
    }

    @Override
    public final JsonPointer getCurrentJsonPointer() {
        Ancestor ancestor = getAncestor();

        if (ancestor == null) {
            return JsonPointer.WHOLE_DOCUMENT_POINTER;
        }

        return new JsonPointer(ancestor.getMutableJsonStructure().getCurrentJsonPointer(), "/" + ancestor);

    }

    /*
     *
     * if pointer denotes a value, else fail
     */
    @Override
    public final JsonValue getLeaf(JsonPointer jsonPointer) {
        throwIfNotStructure();
        
        boolean arraySpecial = "-".equals(jsonPointer.getLastToken().toString());

        if (arraySpecial) {
            throw new JsonException("'-' does not exist");
        }

        try {
            MutableJsonStructure lastMutable = get(jsonPointer.getParent());
            return lastMutable.isJsonArray() ? lastMutable.getLeaf(jsonPointer.getLastToken().getTokenAsIndex(lastMutable.size()))
                    : lastMutable.getLeaf(jsonPointer.getLastToken().getToken());

        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    /*
     *
     * if pointer denotes a structure, else fail
     */
    @Override
    public final MutableJsonStructure get(JsonPointer jsonPointer) {
        throwIfNotStructure();

        try {
            boolean arraySpecial = "-".equals(jsonPointer.getLastToken().toString());

            if (arraySpecial && !isArray()) {
                throw new JsonException(JsonMessages.MUTABLE_ARRAY_ONLY());
            }

            List<PointerToken> tks = jsonPointer.getTokens();

            MutableJsonStructure lastMutable = this;

            for (Iterator iterator = tks.iterator(); iterator.hasNext();) {
                PointerToken pointerToken = (PointerToken) iterator.next();

                if (lastMutable.isJsonArray()) {
                    lastMutable = lastMutable.get(pointerToken.getTokenAsIndex(lastMutable.size()));
                } else {
                    lastMutable = lastMutable.get(pointerToken.getToken());
                }

            }

            return lastMutable;

        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure remove(JsonPointer jsonPointer) {
        throwIfNotStructure();

        try {
            MutableJsonStructure denoted = get(jsonPointer.getParent());

            if (denoted.isJsonArray()) {
                denoted.remove(jsonPointer.getLastToken().getTokenAsIndex(denoted.size()));
            } else {
                denoted.remove(jsonPointer.getLastToken().getToken());
            }
            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure set(JsonPointer jsonPointer, MutableJsonStructure value) {
        throwIfNotStructure();

        try {
            MutableJsonStructure denoted = get(jsonPointer.getParent());

            if (denoted.isJsonArray()) {
                denoted.set(jsonPointer.getLastToken().getTokenAsIndex(denoted.size()), value);
            } else {
                denoted.set(jsonPointer.getLastToken().getToken(), value);
            }
            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure set(JsonPointer jsonPointer, JsonValue value) {
        throwIfNotStructure();

        try {
            MutableJsonStructure denoted = get(jsonPointer.getParent());

            if (denoted.isJsonArray()) {
                denoted.set(jsonPointer.getLastToken().getTokenAsIndex(denoted.size()), value);
            } else {
                denoted.set(jsonPointer.getLastToken().getToken(), value);
            }
            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure add(JsonPointer jsonPointer, int index, MutableJsonStructure value) {
        throwIfNotArray();

        try {
            MutableJsonStructure denoted = get(jsonPointer);
            denoted.add(index, value);

            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure add(JsonPointer jsonPointer, int index, JsonValue value) {
        throwIfNotArray();
        try {

            MutableJsonStructure denoted = get(jsonPointer);
            if (value.getValueType().equals(JsonValue.ValueType.ARRAY) || value.getValueType().equals(JsonValue.ValueType.OBJECT)) {

                denoted.add(index, ((JsonStructure) value).toMutableJsonStructure());
            } else {
                denoted.add(index, value);
            }

            return this;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure set(JsonPointer jsonPointer, String value) {
        throwIfNotStructure();

        try {
            MutableJsonStructure denoted = get(jsonPointer.getParent());

            if (denoted.isJsonArray()) {
                denoted.set(jsonPointer.getLastToken().getTokenAsIndex(denoted.size()), value);
            } else {
                denoted.set(jsonPointer.getLastToken().getToken(), value);
            }
            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure set(JsonPointer jsonPointer, Number value) {
        throwIfNotStructure();

        try {
            MutableJsonStructure denoted = get(jsonPointer.getParent());

            if (denoted.isJsonArray()) {
                denoted.set(jsonPointer.getLastToken().getTokenAsIndex(denoted.size()), value);
            } else {
                denoted.set(jsonPointer.getLastToken().getToken(), value);
            }
            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure add(JsonPointer jsonPointer, int index, String value) {
        throwIfNotArray();

        try {
            MutableJsonStructure denoted = get(jsonPointer);
            denoted.add(index, value);

            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure add(JsonPointer jsonPointer, int index, Number value) {
        throwIfNotArray();

        try {
            MutableJsonStructure denoted = get(jsonPointer);
            denoted.add(index, value);

            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public final MutableJsonStructure set(String key, String value) {
        throwIfNotObject();
        return set(key, new JsonStringImpl(value));
    }

    @Override
    public final MutableJsonStructure set(String key, Number value) {
        throwIfNotObject();
        return set(key, toJsonNumber(value));
    }

    @Override
    public final MutableJsonStructure add(String key, String value) {
        throwIfNotObject();
        return add(key, new JsonStringImpl(value));
    }

    @Override
    public final MutableJsonStructure add(String key, Number value) {
        throwIfNotObject();
        return add(key, toJsonNumber(value));
    }

    @Override
    public final MutableJsonStructure set(int index, String value) {
        throwIfNotArray();
        return set(index, new JsonStringImpl(value));
    }

    @Override
    public final MutableJsonStructure set(int index, Number value) {
        throwIfNotArray();
        return set(index, toJsonNumber(value));
    }

    @Override
    public final MutableJsonStructure add(int index, String value) {
        throwIfNotArray();
        return add(index, new JsonStringImpl(value));
    }

    @Override
    public final MutableJsonStructure add(int index, Number value) {
        throwIfNotArray();
        return add(index, toJsonNumber(value));
    }

    @Override
    public final MutableJsonStructure add(String value) {
        throwIfNotArray();
        return add(new JsonStringImpl(value));
    }

    @Override
    public final MutableJsonStructure add(Number value) {
        throwIfNotArray();
        return add(toJsonNumber(value));
    }

    @Override
    public boolean isJsonArray() {
        throwIfNotStructure();
        return isArray();
    }

    @Override
    public String getLeafAsString(String key) {
        return JsonString.class.cast(getLeaf(key)).getString();
    }

    @Override
    public int getLeafAsInt(String key) {
        return JsonNumber.class.cast(getLeaf(key)).intValueExact();
    }

    @Override
    public boolean getLeafAsBoolean(String key) {
        JsonValue val = getLeaf(key);
        switch (val.getValueType()) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
                throw new ClassCastException();
        }
    }

    @Override
    public boolean isLeafNull(String key) {
        JsonValue val = getLeaf(key);
        switch (val.getValueType()) {
            case NULL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String getLeafAsString(int index) {
        return JsonString.class.cast(getLeaf(index)).getString();
    }

    @Override
    public int getLeafAsInt(int index) {
        return JsonNumber.class.cast(getLeaf(index)).intValueExact();
    }

    @Override
    public boolean getLeafAsBoolean(int index) {
        JsonValue val = getLeaf(index);
        switch (val.getValueType()) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
                throw new ClassCastException();
        }
    }

    @Override
    public boolean isLeafNull(int index) {
        JsonValue val = getLeaf(index);
        switch (val.getValueType()) {
            case NULL:
                return true;
            default:
                return false;
        }
    }

    private static JsonNumber toJsonNumber(Number number) {
        if (number instanceof BigDecimal) {
            return JsonNumberImpl.getJsonNumber((BigDecimal) number);
        } else if (number instanceof BigInteger) {
            return JsonNumberImpl.getJsonNumber((BigInteger) number);
        } else if (number instanceof Double) {
            return JsonNumberImpl.getJsonNumber((Double) number);
        } else if (number instanceof Float) {
            return JsonNumberImpl.getJsonNumber((Float) number);
        } else if (number instanceof Long) {
            return JsonNumberImpl.getJsonNumber((Long) number);
        } else {
            return JsonNumberImpl.getJsonNumber((Integer) number);
        }
    }

    protected final boolean isObject() {
        return valueType.equals(ValueType.OBJECT);
    }

    protected final boolean isArray() {
        return valueType.equals(ValueType.ARRAY);
    }

    protected final boolean isStructure() {
        return isArray() || isObject();
    }

    protected final void throwIfNotStructure() {
        if (!isStructure()) {
            throw new JsonException(JsonMessages.MUTABLE_STRUCTURE_ONLY());
        }
    }

    protected final void throwIfNotArray() {
        if (!isArray()) {
            throw new JsonException(JsonMessages.MUTABLE_ARRAY_ONLY());
        }
    }

    protected final void throwIfNotObject() {
        if (!isObject()) {
            throw new JsonException(JsonMessages.MUTABLE_OBJECT_ONLY());
        }
    }

    @Override
    public MutableJsonStructure getParent() {
        if (ancestor == null) {
            return null;
        }

        return ancestor.getMutableJsonStructure();
    }

    @Override
    public MutableJsonStructure add(JsonPointer jsonPointer, String key, MutableJsonStructure value) {
        throwIfNotObject();

        try {
            MutableJsonStructure denoted = get(jsonPointer);
            denoted.add(key, value);

            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public MutableJsonStructure add(JsonPointer jsonPointer, String key, JsonValue value) {
        throwIfNotObject();
        try {

            MutableJsonStructure denoted = get(jsonPointer);
            if (value.getValueType().equals(JsonValue.ValueType.ARRAY) || value.getValueType().equals(JsonValue.ValueType.OBJECT)) {

                denoted.add(key, ((JsonStructure) value).toMutableJsonStructure());
            } else {
                denoted.add(key, value);
            }

            return this;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public MutableJsonStructure add(JsonPointer jsonPointer, String key, String value) {
        throwIfNotObject();

        try {
            MutableJsonStructure denoted = get(jsonPointer);
            denoted.add(key, value);

            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public MutableJsonStructure add(JsonPointer jsonPointer, String key, Number value) {
        throwIfNotObject();

        try {
            MutableJsonStructure denoted = get(jsonPointer);
            denoted.add(key, value);

            return this;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(JsonMessages.POINTER_EXCEPTION(), e);
        }
    }

    @Override
    public boolean isLeaf(JsonPointer jsonPointer) {
        //can be done better, not very performant here
        try {
            return getLeaf(jsonPointer) != null;
        } catch (JsonException e) {
            try {
                get(jsonPointer);
                return false;
            } catch (JsonException e1) {
                throw e1;
            }
        }
    }

    @Override
    public boolean exists(JsonPointer jsonPointer) {
        //can be done better, not very performant here
        try {
            isLeaf(jsonPointer);
            return true;
        } catch (JsonException e) {
            return false;
        }
    }

    @Override
    public boolean exists(String key) {
        return getKeys().contains(key);
    }

    @Override
    public boolean exists(int index) {
        return index < size();
    }
    
    @Override
    public MutableJsonStructure copy() {
        //can be done better, not very performant here
        return toJsonStructure().toMutableJsonStructure();
    }

}
