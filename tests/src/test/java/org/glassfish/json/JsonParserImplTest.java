/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import junit.framework.TestCase;

public class JsonParserImplTest extends TestCase {

    public JsonParserImplTest(String testName) {
        super(testName);
    }

    public void testUndefined() {
        String previousValue = System.getProperty(JsonParserImpl.MAX_DEPTH);
        try {
            System.getProperties().remove(JsonParserImpl.MAX_DEPTH);
            int result = JsonParserImpl.propertyStringToInt(JsonParserImpl.MAX_DEPTH, -1);
            assertEquals(-1, result);
        } finally {
            if (previousValue != null) {
                System.setProperty(JsonParserImpl.MAX_DEPTH, previousValue);
            }
        }
    }

    public void testNotInteger() {
        String previousValue = System.getProperty(JsonParserImpl.MAX_DEPTH);
        try {
            System.setProperty(JsonParserImpl.MAX_DEPTH, "String");
            int result = JsonParserImpl.propertyStringToInt(JsonParserImpl.MAX_DEPTH, -10);
            assertEquals(-10, result);
        } finally {
            if (previousValue != null) {
                System.setProperty(JsonParserImpl.MAX_DEPTH, previousValue);
            }
        }
    }

    public void testInteger() {
        String previousValue = System.getProperty(JsonParserImpl.MAX_DEPTH);
        try {
            System.setProperty(JsonParserImpl.MAX_DEPTH, "10");
            int result = JsonParserImpl.propertyStringToInt(JsonParserImpl.MAX_DEPTH, -1);
            assertEquals(10, result);
        } finally {
            if (previousValue != null) {
                System.setProperty(JsonParserImpl.MAX_DEPTH, previousValue);
            }
        }
    }
}