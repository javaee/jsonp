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

package org.glassfish.json.tests;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.MutableJsonStructure;
import javax.json.stream.JsonGenerator;

import junit.framework.TestCase;

/**
 * @author Hendrik Saly
 */
public class MutableJsonStructureTest extends TestCase {
    
    public MutableJsonStructureTest(String testName) {
        super(testName);
    }
    
    public void testSimple() throws Exception {
        Reader wikiReader = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("/wiki.json"));
        JsonReader reader = Json.createReader(wikiReader);
        MutableJsonStructure ms = reader.readObject().toMutableJsonStructure();
        reader.close();
        assertNotNull(ms);
        assertEquals(5, ms.size());
        
        ms
        .add("test", "val")
        .add("num", 2);
        
        assertEquals(7, ms.size());
        
        //roundtrip tests
        JsonStructure jsonStructure = ms.toJsonStructure();
        MutableJsonStructure ms2 = jsonStructure.toMutableJsonStructure();
        JsonStructure jsonStructure2 = ms2.toJsonStructure();
        assertEquals(ms,ms2);
        assertEquals(jsonStructure,jsonStructure2);
        
    }
    
    public void testVarious() throws Exception {
        Reader wikiReader = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("/wiki.json"));
        JsonReader reader = Json.createReader(wikiReader);
        MutableJsonStructure ms = reader.readObject().toMutableJsonStructure();
        assertFalse(ms.isJsonArray());
        
        try {
            ms.getKeys().add("test");
            fail();
        } catch (UnsupportedOperationException e) {
            //expected
        }
        
        assertNotSame(ms, ms.copy());
        assertTrue(ms.isLeaf("age"));
        assertFalse(ms.isLeaf("address"));
        assertFalse(ms.isLeafNull("firstName"));
        assertTrue(ms.exists("phoneNumber"));
        assertTrue(ms.exists(new JsonPointer("/phoneNumber/1/type")));
        assertFalse(ms.exists("eMail"));
        assertFalse(ms.exists(new JsonPointer("/phoneNumber/2/type")));
        assertEquals(1, ms.get(new JsonPointer("/phoneNumber/1/type").getParent()).getAncestor().getIndex());
        assertNull(ms.getParent());
        assertEquals("Smith", ms.getLeafAsString("lastName"));
        assertEquals("NY", ms.get("address").getLeafAsString("state"));
        assertEquals(5, ms.getKeys().size());
        assertEquals(5, ms.size());
        assertEquals(4, ms.get("address").size());
        ms.add("additionalAddress", ms.get("address").copy().remove("city").set("state", "CA"));
        ms.set(ms.copy().remove("phoneNumber"));
        assertEquals(5, ms.size());     
    }
    
    public void testEmpty() throws Exception {
        MutableJsonStructure ms = MutableJsonStructure.createNewMutableArray();
        assertTrue(ms.isJsonArray());

        try {
            assertTrue(ms.isLeaf("age"));
            fail();
        } catch (JsonException e) {
            //expected
        }

        assertNull(ms.getParent());
        ms.add("test").add(1).add(0, 0);
        ms.set(1, ms.copy());
        ms.remove(new JsonPointer("/1/2"));
        assertEquals("[0,[0,\"test\"],1]", ms.toJsonStructure().toString());
    }
    
    public void testMutate() throws Exception {
        Reader wikiReader = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("/wiki.json"));
        JsonReader reader = Json.createReader(wikiReader);
        MutableJsonStructure ms = reader.readObject().toMutableJsonStructure();
        reader.close();
        assertNotNull(ms);
        assertEquals(5, ms.size());
        
        MutableJsonStructure m = ms
        .set("firstName", "Mister")
        .set("lastName", "Spock")
        .set(new JsonPointer("/age"), "unknown")
        .add(new JsonPointer("/address"), "deceased", JsonValue.TRUE)
        .get("phoneNumber")
        .add(new JsonPointer(""), 2, JsonValue.NULL)
        .get(1)
        .set("number", "000")
        .getParent()
        .remove(0)
        .getParent()
        ;
        
        //System.out.println(m.getCurrentJsonPointer());
        //System.out.println(pretty(ms));
        
        Reader wikiMutatedReader = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("/wiki_mutated.json"));
        JsonReader mutatedReader = Json.createReader(wikiMutatedReader);
        JsonObject mutatedWikiObject = mutatedReader.readObject();
        mutatedReader.close();
        assertEquals(ms.toJsonStructure(), mutatedWikiObject);
        
    }
    
    public void testPointer() throws Exception {
        Reader wikiReader = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("/facebook.json"));
        JsonReader reader = Json.createReader(wikiReader);
        JsonObject object = reader.readObject();
        MutableJsonStructure ms = object.toMutableJsonStructure();
        reader.close();
        assertNotNull(ms);
        assertEquals(2, ms.size());
        assertEquals("[data, paging]", ms.getKeys().toString());
        
        String comment = ms.get("data").get(1).get("actions").get(0).getLeafAsString("name");
        assertEquals("Comment", comment);
       
        assertEquals("Comment", JsonString.class.cast(ms.getLeaf(new JsonPointer("/data/1/actions/0/name"))).getString());
        assertEquals(false, ms.get(new JsonPointer("/data/1/actions/0")).isJsonArray());
        assertEquals(true, ms.get(new JsonPointer("/data/1/actions")).isJsonArray());
        assertEquals("540006262732189_558758910856924", ((JsonObject)ms.get(new JsonPointer("/data/2")).toJsonStructure()).getString("id"));
        ms.set(new JsonPointer("/data/2/id"), "new_id");
        assertEquals("new_id", ms.get(new JsonPointer("/data/2")).getLeafAsString("id"));
    }
    
    public void testFullJson() throws Exception {
        final JsonObject objectLeaf = Json.createObjectBuilder()
                .add("string", "abcdef")
                .add("byte",(byte) 1)
                .add("short",(short) 1)
                .add("int",1)
                .add("long",1L)
                .add("bigdecimal",new BigDecimal("1.23456"))
                .add("biginteger",new BigInteger("100000"))
                .add("float",1.234f)
                .add("double",1.234d)
                .add("bool",false).build();
                
        final JsonArray arrayLeaf = Json.createArrayBuilder()
                .add("xyzxyz")
                .add((byte) -3)
                .add((short) -3)
                .add(-3)
                .add(-3L)
                .add(new BigDecimal("-3.23456"))
                .add(new BigInteger("-300000"))
                .add(-3.234f)
                .add(-3.234d)
                .add(true).build();
    
        final JsonObject nested1 = Json.createObjectBuilder()
                .add("anobject", objectLeaf)
                .add("anarray", arrayLeaf)
                .build();
        
        final JsonArray nested2 = Json.createArrayBuilder()
                .add(nested1)
                .add(nested1)
                .build();
        
        final JsonObject nested3 = Json.createObjectBuilder()
                .add("anobject_2", nested2)
                .add("anarray_2", arrayLeaf)
                .add("bd", BigDecimal.ONE)
                .build();
        
        //roundtrip tests
        final MutableJsonStructure nested3Mutable = nested3.toMutableJsonStructure();
        final JsonStructure nested3Roundtrip = nested3Mutable.toJsonStructure();
        final MutableJsonStructure nested3MutableRoundtrip = nested3Roundtrip.toMutableJsonStructure();
        assertEquals(nested3Roundtrip, nested3);
        assertEquals(nested3Roundtrip.toString(), nested3.toString());
        assertEquals(nested3MutableRoundtrip, nested3Mutable);
        assertEquals(nested3MutableRoundtrip.toString(), nested3Mutable.toString());
        assertEquals(nested3MutableRoundtrip.toString(), nested3.toString());
    }
    
    protected static String pretty(MutableJsonStructure value) {
        return pretty(value.toJsonStructure());
    }
    
    protected static String pretty(JsonValue value) {
        Map config = new HashMap();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        StringWriter sw = new StringWriter();
        Json.createGeneratorFactory(config).createGenerator(sw).writeStartArray().write(value).writeEnd().close();;
        sw.flush();
        return (value.getValueType()+sw.toString().substring(2).substring(0, sw.toString().length()-3).replace("\n    ", "\n"));
    }

}
