/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.json.api.BufferPool;

import javax.json.stream.JsonGenerator;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @author Jitendra Kotamraju
 */
public class JsonPrettyGeneratorImpl extends JsonGeneratorImpl {
    private int indentLevel;
    private static final String INDENT = "    ";

    public JsonPrettyGeneratorImpl(Writer writer, BufferPool bufferPool) {
        super(writer, bufferPool);
    }

    public JsonPrettyGeneratorImpl(OutputStream out, BufferPool bufferPool) {
        super(out, bufferPool);
    }

    public JsonPrettyGeneratorImpl(OutputStream out, Charset encoding, BufferPool bufferPool) {
        super(out, encoding, bufferPool);
    }

    @Override
    public JsonGenerator writeStartObject() {
        super.writeStartObject();
        indentLevel++;
        return this;
    }

    @Override
    public JsonGenerator writeStartObject(String name) {
        super.writeStartObject(name);
        indentLevel++;
        return this;
    }

    @Override
    public JsonGenerator writeStartArray() {
        super.writeStartArray();
        indentLevel++;
        return this;
    }

    @Override
    public JsonGenerator writeStartArray(String name) {
        super.writeStartArray(name);
        indentLevel++;
        return this;
    }

    @Override
    public JsonGenerator writeEnd() {
        writeNewLine();
        indentLevel--;
        writeIndent();
        super.writeEnd();
        return this;
    }

    private void writeIndent() {
        for(int i=0; i < indentLevel; i++) {
            writeString(INDENT);
        }
    }

    @Override
    protected void writeComma() {
        super.writeComma();
        if (isCommaAllowed()) {
            writeChar('\n');
            writeIndent();
        }
    }

    @Override
    protected void writeColon() {
        super.writeColon();
        writeChar(' ');
    }

    private void writeNewLine() {
        writeChar('\n');
    }
}
