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

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.MutableJsonStructure;

/**
 * Just a demo to show how {@code MutableJsonStructure} can easily be used to implement RFC 6902
 * There are a testcase for this class (which pass of course) but it uses third party tests (ASL2 license) 
 * and to be safe i will not check in this for now.
 * 
 * This RFC 6902 impl do not support scalar operations yet. Will leave this up to RFC 7159 discussion.
 *
 * Usage
 * JsonPatcherImpl patcher = new JsonPatcherImpl(jsonDocument);
 * patcher.applyPatchOp(patch1);
 * patcher.applyPatchOp(patch2);
 * patcher.applyPatchOp(patch3);
 * patcher.applyPatchOp(patch4);
 * jsonDocument is changed according to patches applied
 *
 * @author Hendrik Saly
 *
 */
public class JsonPatcherImpl {
    //TODO make package private if there is an interface
    private final MutableJsonStructure jsonDocument;

    public JsonPatcherImpl(MutableJsonStructure jsonDocument) {
        super();
        this.jsonDocument = jsonDocument;
    }

    private void addOrCreate(JsonPointer path, JsonValue value) {

        if (path == null) {
            throw new JsonException("path pointer must not be null");
        }

        if (value == null) {
            throw new JsonException("value must not be null");
        }

        if (jsonDocument.exists(path)) {
            //exists

            if (path.equals(JsonPointer.WHOLE_DOCUMENT_POINTER)) {
                jsonDocument.set(((JsonStructure) value).toMutableJsonStructure());
                return;
            }

            MutableJsonStructure parent = jsonDocument.get(path.getParent());
            if (parent.isJsonArray()) {
                parent.add(path.getLastToken().getTokenAsIndex(parent.size()), value);
            } else {
                if (path.getLastToken().getToken().isEmpty()) {
                    parent.add(path.getLastToken().getToken(), value);
                } else {
                    parent.set(path.getLastToken().getToken(), value);
                }
            }
        } else {
            //does not exists
            MutableJsonStructure parent = jsonDocument.get(path.getParent());
            if (parent.isJsonArray()) {
                parent.add(path.getLastToken().getTokenAsIndex(parent.size()), value);
            } else {
                parent.add(path.getLastToken().getToken(), value);
            }
        }
    }

    public void applyPatchOp(JsonObject patchOp) {

        String op = patchOp.getString("op");
        JsonPointer path = new JsonPointer(patchOp.getString("path"));
        boolean pathExists = true;
        boolean pathDenotesLeaf = false;

        try {
            pathDenotesLeaf = jsonDocument.isLeaf(path);
        } catch (Exception e) {
            pathExists = false;
        }

        switch (op) {
            case "add": {
                JsonValue value = patchOp.get("value");
                addOrCreate(path, value);
            }
            ;
            break;
            case "remove":
                jsonDocument.remove(path);
                break;
            case "replace": {
                JsonValue value = patchOp.get("value");
                jsonDocument.remove(path);
                addOrCreate(path, value);
            }
            break;
            case "move": {
                JsonPointer fromPointer = new JsonPointer(patchOp.getString("from"));
                if (jsonDocument.isLeaf(fromPointer)) {
                    JsonValue value = jsonDocument.getLeaf(fromPointer);
                    jsonDocument.remove(fromPointer);
                    addOrCreate(path, value);
                } else {
                    MutableJsonStructure value = jsonDocument.get(fromPointer);
                    jsonDocument.remove(fromPointer);
                    addOrCreate(path, value.toJsonStructure());
                }
            }

            break;
            case "copy": {
                JsonPointer fromPointer = new JsonPointer(patchOp.getString("from"));

                if (jsonDocument.isLeaf(fromPointer)) {
                    JsonValue value = jsonDocument.getLeaf(fromPointer);
                    addOrCreate(path, value);
                } else {
                    MutableJsonStructure value = jsonDocument.get(fromPointer);
                    addOrCreate(path, value.toJsonStructure());
                }

            }
            break;
            case "test": {
                JsonValue value = patchOp.get("value");

                if (pathDenotesLeaf) {
                    if (!jsonDocument.getLeaf(path).equals(value)) {
                        throw new JsonException("test not successful " + value + "!=" + jsonDocument.getLeaf(path));
                    }
                    break;
                }

                if (!pathExists) {
                    throw new JsonException("path does not exist");
                }

                //path denotes a structure
                if (!(value instanceof JsonStructure) || !jsonDocument.get(path).equals(((JsonStructure) value).toMutableJsonStructure())) {
                    throw new JsonException("test not successful " + value + "!=" + jsonDocument.get(path));
                }
            }
            ;
            break;

            default:
                throw new IllegalArgumentException("Unknow op " + op);
        }

    }

    public MutableJsonStructure getJsonDocument() {
        return jsonDocument;
    }

}
