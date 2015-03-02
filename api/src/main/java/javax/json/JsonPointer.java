/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package javax.json;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is only a placeholder class and will be replaced by
 * https://java.net/jira/browse/JSON_PROCESSING_SPEC-60 This file is NOT PART of
 * the JSON_PROCESSING_SPEC-67 proposal
 * 
 * @author Hendrik Saly
 *
 */
public class JsonPointer {

    private final static String WHOLE_DOCUMENT_POINTER_VALUE = "";
    public final static JsonPointer WHOLE_DOCUMENT_POINTER = new JsonPointer(
            WHOLE_DOCUMENT_POINTER_VALUE);

    private final String pointerValue;

    public JsonPointer(String pointerValue) {
        this(null, pointerValue);
    }

    public JsonPointer(JsonPointer parent, String pointerValue) {

        if (pointerValue == null) {
            throw new IllegalArgumentException("must not be null");
        }

        if (!WHOLE_DOCUMENT_POINTER_VALUE.equals(pointerValue)
                && !pointerValue.startsWith("/")) {
            throw new IllegalArgumentException("must start with /");
        }

        if (!pointerValue.equals("/") && pointerValue.endsWith("/")) {
            throw new IllegalArgumentException("must not end with /");
        }

        this.pointerValue = (parent == null ? "" : parent.getPointerValue())
                + pointerValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((pointerValue == null) ? 0 : pointerValue.hashCode());
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
        JsonPointer other = (JsonPointer) obj;
        if (pointerValue == null) {
            if (other.pointerValue != null)
                return false;
        } else if (!pointerValue.equals(other.pointerValue))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return pointerValue;
    }

    public String getPointerValue() {
        return pointerValue;
    }

    public JsonPointer getParent() {

        if (WHOLE_DOCUMENT_POINTER_VALUE.equals(pointerValue)) {
            return this;
        }

        int index = pointerValue.lastIndexOf("/");

        if (index < 1)
            return WHOLE_DOCUMENT_POINTER;

        return new JsonPointer(pointerValue.substring(0, index));
    }

    public boolean isParentOf(JsonPointer probablyChildPointer) {
        if (probablyChildPointer.pointerValue.startsWith(this.pointerValue)
                && !this.equals(probablyChildPointer)) {
            return true;
        }

        return false;
    }

    public List<PointerToken> getTokens() {
        return Arrays.asList(pointerValue.split("/")).stream()
                .filter(t -> !t.isEmpty()).map(t -> new PointerToken(t))
                .collect(Collectors.toList());
    }

    public PointerToken getLastToken() {
        List<PointerToken> tokens = getTokens();
        if (tokens.size() == 0) {
            return new PointerToken("");
        }

        return tokens.get(tokens.size() - 1);
    }

    public static class PointerToken {

        private final String token;

        PointerToken(String token) {
            super();

            if (token == null) {
                throw new IllegalArgumentException();
            }

            this.token = token.replace("~1", "/").replace("~0", "~");
        }

        public int getTokenAsIndex(int arraySize) {

            if ("-".equals(token)) {
                return arraySize;
            }

            try {
                return Integer.parseUnsignedInt(token);
            } catch (NumberFormatException e) {
                throw new JsonException("token '" + token + "' is not a number");
            }

        }

        public String getToken() {
            return token;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((token == null) ? 0 : token.hashCode());
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
            PointerToken other = (PointerToken) obj;
            if (token == null) {
                if (other.token != null)
                    return false;
            } else if (!token.equals(other.token))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return token;
        }

    }
}