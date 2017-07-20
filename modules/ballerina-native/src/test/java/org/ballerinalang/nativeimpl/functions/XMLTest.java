/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.functions;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLAttributes;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.nativeimpl.util.XMLUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * Test Native function in ballerina.model.xml.
 */
public class XMLTest {

    private ProgramFile programFile;
    private ProgramFile xmlAttrProgFile;
    ProgramFile namespaceProgFile;
    private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
    private static final String s2 = "<person><name>Jack</name></person>";
    private static String l1;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/xml/xmlTest.bal");
        xmlAttrProgFile = BTestUtils.getProgramFile("samples/xml/xmlAttributeTest.bal");
        namespaceProgFile = BTestUtils.getProgramFile("samples/xml/xmlNamespaceTest.bal");
    }

    @Test
    public void testGetString() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    
    @Test
    public void testGetStringFromSeq() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetNonExistingString() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx/text()")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getString", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testGetStringFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BLangFunctions.invokeNew(programFile, "getString", args);
    }

    @Test
    public void testGetXML() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test
    public void testGetXMLFromSingletonSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to get element " +
                  "from xml: cannot execute xpath on a xml sequence.*")
    public void testGetXMLFromSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        seq.add(1, new BXMLItem(s2));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }
    
    @Test
    public void testGetXMLLarge() {
        // Load large xml
        l1 = XMLUtils.readFileToString("datafiles/message13k.xml");
        BValue[] args = {new BXMLItem(l1),
                new BString("/persons/person[160]")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person>" +
                "<name>Jill</name>" +
                "<address>wso2</address>" +
                "</person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetNonExistingXML() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testGetXMLFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromText() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test
    public void testSetString() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }

    @Test
    public void testSetStringToSingletonSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to set " +
                    "string in xml: cannot execute xpath on a xml sequence.*")
    public void testSetStringToSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        seq.add(1, new BXMLItem(s2));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }
    
    @Test
    public void testSetStringToNonExistingElement() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetStringToMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path"), new BString("Peter")};
        BLangFunctions.invokeNew(programFile, "setString", args);
    }

    @Test
    public void testSetStringToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageSimple.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employee/@id"), new BString("0")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<employee id=\"0\">\n" +
                "    <name>Parakum</name>\n" +
                "    <age>32</age>\n" +
                "</employee>");
    }

    @Test
    public void testSetXML() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name><fname>Jack" +
                "</fname><lname>Peter</lname></name></person>");
    }

    @Test
    public void testSetXMLToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), s2);
    }


    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetXMLToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BLangFunctions.invokeNew(programFile, "setXML", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetXMLNullValue() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"),
                null};
        BLangFunctions.invokeNew(programFile, "setXML", args);
    }

    @Test
    public void testAddElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/person"), new BXMLItem("<address>wso2</address>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"), new BXMLItem("<address>wso2</address>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), s2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testAddElementToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"), new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToText() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()"), 
            new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/"), new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id"), 
                new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test
    public void testAddAttributeWithXPath() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"), new BString("id"), new BString("person123")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<person><name id=\"person123\">Jack</name></person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"), new BString("id"), new BString("person123")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), s2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testAddAttributeToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"), new BString("id"), new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToText() {
        BValue[] args = {new BXMLItem(s2), new BString("/persons/person/name/text()"), new BString("id"),
                new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/"), new BString("id"), new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id"), new BString("id"),
                new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test
    public void testRemove() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/address")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "remove", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<persons><person><name>Jack" +
                "</name></person></persons>");
    }

    @Test
    public void testRemoveNonExistingElement() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "remove", args);
        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testRemoveFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BLangFunctions.invokeNew(programFile, "remove", args);
    }

    @Test(description = "Test xml element string value replacement")
    public void testSetXmlElementText() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "xmlSetString1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        OMElement xmlMessage = (OMElement) ((BXMLItem) returns[0]).value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName1", "XML Element text not set properly");
    }

    @Test(description = "Test xml text value replacement")
    public void testSetXmlText() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "xmlSetString2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        OMElement xmlMessage = (OMElement) ((BXMLItem) returns[0]).value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName2", "XML Element text not set properly");
    }

    @Test
    public void testIsSingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIsSingleton");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
    }
    
    @Test
    public void testIsEmpty() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIsEmpty");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }
    
    @Test
    public void testGetItemType() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetItemType");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
        
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "");
    }
    
    @Test
    public void testGetElementName() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementName");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test}name");
    }
    
    @Test
    public void testGetTextValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "supun");
    }
    
    @Test
    public void testGetElements() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElements");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }
    
    @Test
    public void testGetElementsByName() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByName");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetNonExistingChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetNonExistingChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testSelectChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testConcat() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testConcat");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun" +
                "</fname><lname>setunga</lname></ns0:name><ns1:address xmlns:ns1=\"http://sample.com/test\">" +
                "<country>SL</country><city>Colombo</city></ns1:address>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testSetChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildren");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
    }
    
    @Test
    public void testCopy() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCopy");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // Check children of the copied xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) ((BXML) returns[0]).children()).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children of the original xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray originalChildren = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(originalChildren.size(), 2);
        Assert.assertEquals(originalChildren.get(0).stringValue(), "<fname>supun</fname>");
        Assert.assertEquals(originalChildren.get(1).stringValue(), "<lname>setunga</lname>");
    }
    
    @Test
    public void testToString() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test
    public void testStrip() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStrip");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book-->     <bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
        
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 3);
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
    }
    
    @Test
    public void testStripSingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStripSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<bookId>001</bookId>");
        Assert.assertEquals(returns[1].stringValue(), "<bookId>001</bookId>");
    }
    
    @Test
    public void testStripEmptySingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStripEmptySingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "");
        Assert.assertEquals(returns[1].stringValue(), "");
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }
    
    @Test
    public void testSlice() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSlice");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 3);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName><bookId>001</bookId><bookAuthor>" +
                "Author01</bookAuthor>");
    }
    
    @Test
    public void testSliceAll() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSliceAll");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to slice xml: " +
                    "invalid indices: 4 < 1.*")
    public void testSliceInvalidIndex() {
        BLangFunctions.invokeNew(programFile, "testSliceInvalidIndex");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to slice xml: " +
                    "index out of range: \\[4,10\\].*")
    public void testSliceOutOfRangeIndex() {
        BLangFunctions.invokeNew(programFile, "testSliceOutOfRangeIndex");
    }
    
    @Test
    public void testSliceSingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSliceSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName>");
    }
    
    @Test
    public void testXPathOnCopiedXML() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testXPathOnCopiedXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root><bookId>001</bookId><bookAuthor>Author01</bookAuthor>" +
                "</root>");
        Assert.assertEquals(returns[1].stringValue(), "<root><bookName>Book1</bookName><bookId>001</bookId></root>");
    }
    
    @Test
    public void testSeqCopy() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSeqCopy");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>Updated Book ID</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }

    @Test
    public void testAddAttributeWithString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns4=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns0Kf5j:foo2=\"bar2\" ns4:foo3=\"bar3\"/>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class}, 
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: localname of the " +
            "attribute cannot be empty.*")
    public void testAddAttributeWithoutLocalname() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithoutLocalname");
    }
    
    @Test
    public void testAddAttributeWithEmptyNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" foo1=\"bar\"/>");
    }
    
    @Test
    public void testAddNamespaceAsAttribute1() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
            "xmlns:ns4=\"http://wso2.com\"/>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<root xmlns=\"http://ballerinalang.org/\" " +
            "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns4=\"http://wso2.com\"/>");
    }
    
    @Test
    public void testAddAttributeWithQName() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\" ns3:foo2=\"bar2\"/>");
    }
    
    @Test
    public void testUpdateAttributeWithString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns4=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" foo1=\"newbar1\" ns0Kf5j:foo2=\"newbar2\" " +
                "ns4:foo3=\"bar3\"/>");
    }
    
    @Test
    public void testUpdateNamespaceAsAttribute() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://wso2.com\"/>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<root xmlns=\"http://ballerinalang.org/\" " +
            "xmlns:ns3=\"http://wso2.com\"/>");
    }
    
    @Test
    public void testUpdateAttributeWithQName() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"newbar1\" ns3:foo2=\"newbar2\"/>");
    }
    
    @Test
    public void testGetAttributeWithString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");
        
        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "");
    }
    
    @Test
    public void testGetAttributeWithoutLocalname() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithoutLocalname");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "");
    }
    
    @Test
    public void testGetAttributeWithEmptyNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
    }

    @Test
    public void testGetNamespaceAsAttribute() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "http://sample.com/wso2/f");
    }

    @Test
    public void testGetAttributeWithQName() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "");
    }

    @Test
    public void testUsingQNameAsString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUsingQNameAsString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a1}wso2");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/a1}ballerina");
    }

    @Test
    public void testGetAttributesAsMap() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributesAsMap");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"{http://www.w3.org/2000/xmlns/}ns0\":"
                + "\"http://sample.com/wso2/a1\",\"{http://sample.com/wso2/a1}foo1\":\"bar1\",\"foo2\":\"bar2\"}");

        Assert.assertTrue(returns[1] instanceof BMap);
        Assert.assertEquals(returns[1].stringValue(), "{\"{http://sample.com/default/namepsace}ns0\":"
                + "\"http://sample.com/wso2/a1\",\"{http://sample.com/wso2/a1}foo1\":\"bar1\",\"foo2\":\"bar2\"}");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "bar1");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "bar1");
    }

    @Test
    public void testNamespaceDclr() {
        BValue[] returns = BLangFunctions.invokeNew(namespaceProgFile, "testNamespaceDclr");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a2}foo");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/b1}foo");
        
        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "{http://sample.com/wso2/d2}foo");
    }

    @Test
    public void testInnerScopeNamespaceDclr() {
        BValue[] returns = BLangFunctions.invokeNew(namespaceProgFile, "testInnerScopeNamespaceDclr");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a1}foo");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/a3}foo");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "{http://sample.com/wso2/a1}foo");
    }

    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "redeclareNamespaces.bal:11: redeclared symbol 'ns0'")
    public void testRedeclaringNamespace() {
        BTestUtils.getProgramFile("samples/xml/redeclareNamespaces.bal");
    }

    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "attributeMapInvalidUse.bal:6: incompatible types: 'xml-attributes' " +
            "cannot be assigned to 'map'")
    public void testXMlAttributesMapInvalidUsage() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils.getProgramFile("samples/xml/attributeMapInvalidUse.bal");
        BLangFunctions.invokeNew(xmlAttributeMapInvalidUsage, "testXMlAttributesMapInvalidUsage");
    }
    
    @Test
    public void testXMLAttributesToAny() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testXMLAttributesToAny");
        Assert.assertTrue(returns[0] instanceof BXMLAttributes);
        Assert.assertEquals(returns[0].stringValue(), "{\"{http://www.w3.org/2000/xmlns/}ns0\":" +
            "\"http://sample.com/wso2/a1\",\"{http://sample.com/wso2/a1}foo1\":\"bar1\",\"foo2\":\"bar2\"}");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "namespaceConflictWithPkgImport.bal:6: redeclared symbol 'x'")
    public void testNamespaceConflictWithPkgImport() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils
                .getProgramFile("samples/xml/namespaceConflictWithPkgImport.bal");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "pkgImportConflictWithNamespace.bal:4: redeclared symbol 'x'")
    public void testPkgImportConflictWithNamespace() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils
                .getProgramFile("samples/xml/pkgImportConflictWithNamespace.bal");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "getAttributesFromNonXml.bal:4: incompatible types: expected 'xml', "
                    + "found 'map'")
    public void testGetAttributesFromNonXml() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils.getProgramFile("samples/xml/getAttributesFromNonXml.bal");
    }

    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "updateAttributeMap.bal:3: xml attributes cannot be updated as a " +
            "collection. update attributes one at a time")
    public void testUpdateAttributeMap() {
        ProgramFile xmlAttributeMapInvalidUsage =
                BTestUtils.getProgramFile("samples/xml/updateAttributeMap.bal");
    }
    
    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "updateQname.bal:4: cannot assign values to an xml qualified name")
    public void testUpdateQname() {
        ProgramFile xmlAttributeMapInvalidUsage =
                BTestUtils.getProgramFile("samples/xml/updateQname.bal");
    }
    
    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "undefinedNamespace.bal:8: undefined namespace 'ns0'")
    public void testUndefinedNamespace() {
        ProgramFile xmlAttributeMapInvalidUsage =
                BTestUtils.getProgramFile("samples/xml/undefinedNamespace.bal");
    }
    
    @Test
    public void testRuntimeNamespaceLookup() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testRuntimeNamespaceLookup");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns401=\"http://sample.com/wso2/a1\" xmlns:ns1=" +
            "\"http://sample.com/wso2/b1\" xmlns:ns403=\"http://sample.com/wso2/e3\" xmlns:nsn7xFP=" +
            "\"http://sample.com/wso2/f3\" ns401:foo1=\"bar1\" ns1:foo2=\"bar2\" ns403:foo3=\"bar3\" " +
            "nsn7xFP:foo4=\"bar4\"/>");
    }
    
    @Test
    public void testRuntimeNamespaceLookupPriority() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testRuntimeNamespaceLookupPriority");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:p1=\"http://wso2.com\" " +
            "xmlns:p2=\"http://sample.com/wso2/a1\" xmlns:ns401=\"http://sample.com/wso2/a1\" ns401:foo1=\"bar1\" " +
            "p1:foo2=\"bar2\"/>");
    }

    @Test
    public void testSetAttributes() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testSetAttributes");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:nsRJUck=\"http://wso2.com\" xmlns:nsn7xDi="
                + "\"http://sample.com/wso2/a1\" nsRJUck:foo2=\"bar2\" nsn7xDi:foo3=\"bar3\" foo1=\"bar1\"/>");
    }

    @Test
    public void testGetAttributeFromSingletonSeq() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeFromSingletonSeq");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar");
    }
}
