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
package org.wso2.ballerina.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.message.StringDataSource;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.MessageDataSource;

import java.util.List;

/**
 * Test Native function in ballerina.lang.message.
 */
public class MessageTest {

    private BallerinaFile bFile;
    //private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/messageTest.bal");
    }

    @Test
    public void testGetJSONPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "{\"name\":\"Jack\",\"address\":\"WSO2\"}";
        carbonMsg.setStringMessageBody(payload);
        BValue[] args = { new BMessage(carbonMsg) };
        BValue[] returns = Functions.invoke(bFile, "testGetJSONPayload", args);
        Assert.assertEquals(returns[0].stringValue(), payload);
    }

    @Test
    public void testSetJSONPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "{\"name\":\"Jack\",\"address\":\"WSO2\"}";
        BValue[] args = { new BMessage(carbonMsg), new BJSON(payload) };
        BValue[] returns = Functions.invoke(bFile, "testSetJSONPayload", args);
        MessageDataSource newPayload = ((BMessage) returns[0]).getMessageDataSource();
        Assert.assertTrue(newPayload instanceof BJSON);
        String value = newPayload.getMessageAsString();
        Assert.assertEquals(value, payload);
    }

    @Test
    public void testSetHeader() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String name = "Content-Type";
        String value = "text/plain";
        BValue[] args = { new BMessage(carbonMsg), new BString(name), new BString(value) };
        BValue[] returns = Functions.invoke(bFile, "testSetHeader", args);
        List<Header> headers = ((BMessage) returns[0]).getHeaders();
        Assert.assertEquals(headers.size(), 1, "Headers list can have only 1 header.");
        Assert.assertNotNull(headers.get(0));
        Assert.assertEquals(headers.get(0).getName(), name);
        Assert.assertEquals(headers.get(0).getValue(), value);
    }

    @Test
    public void testSetXmlPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String xmlPayload = "<root><item>Text</item></root>";
        BValue[] args = { new BMessage(carbonMsg), new BXML(xmlPayload) };
        BValue[] returns = Functions.invoke(bFile, "testSetXmlPayload", args);
        BMessage bMessage = ((BMessage) returns[0]);
        MessageDataSource newPayload = ((BMessage) returns[0]).getMessageDataSource();
        Assert.assertEquals(newPayload.getMessageAsString(), xmlPayload, "XML payload not set properly");
        List<Header> headers = bMessage.getHeaders();
        Assert.assertEquals(headers.size(), 1, "Headers list can have only 1 header.");
        Assert.assertNotNull(headers.get(0));
        Assert.assertEquals(headers.get(0).getName(), "Content-Type", "Content-Type header not found");
        Assert.assertEquals(headers.get(0).getValue(), "application/xml", "Invalid Content-Type");
    }

    @Test
    public void testAddHeader() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String name = "MyNewHeader";
        String value = "NewValue";
        BValue[] args = { new BMessage(carbonMsg), new BString(name), new BString(value) };
        BValue[] returns = Functions.invoke(bFile, "testAddHeader", args);
        List<Header> headers = ((BMessage) returns[0]).getHeaders();
        Assert.assertEquals(headers.size(), 1, "Headers list can have only 1 header.");
        Assert.assertNotNull(headers.get(0));
        Assert.assertEquals(headers.get(0).getName(), name);
        Assert.assertEquals(headers.get(0).getValue(), value);
    }

    @Test
    public void testRemoveHeader() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String name = "NewHeader1";
        String value = "Value";
        carbonMsg.setHeader(name, value);
        carbonMsg.setHeader("Content-Type", "application/xml");
        Assert.assertEquals(carbonMsg.getHeaders().size(), 2, "Header count mismatched.");
        Assert.assertEquals(carbonMsg.getHeader(name), value, "Header not found.");
        BValue[] args = { new BMessage(carbonMsg), new BString(name) };
        BValue[] returns = Functions.invoke(bFile, "testRemoveHeader", args);
        BMessage bMessage = ((BMessage) returns[0]);
        List<Header> headers = bMessage.getHeaders();
        Assert.assertEquals(headers.size(), 1, "Headers list can have only one element");
        Assert.assertNull(bMessage.getHeader(name), "Removed header found");
    }

    //    @Test
    public void testGetHeader() {
        // TODO : This is not working, because CMessage Headers won't get sync with BMessage value.
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String name = "Content-Type";
        String value = "text/plain";
        carbonMsg.setHeader(name, value);
        carbonMsg.setHeader("Accept", "Application/json");
        // These headers are not getting set.
        BValue[] args = { new BMessage(carbonMsg), new BString(name) };
        BValue[] returns = Functions.invoke(bFile, "testGetHeader", args);
        Assert.assertEquals(returns[0].stringValue(), value);
    }

    @Test
    public void testGetStringPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "Hello World...!!!";
        carbonMsg.setStringMessageBody(payload);
        BValue[] args = { new BMessage(carbonMsg) };
        BValue[] returns = Functions.invoke(bFile, "testGetStringPayload", args);
        MessageDataSource newPayload = ((BMessage) returns[0]).getMessageDataSource();
        Assert.assertTrue(newPayload instanceof StringDataSource);
        String value = newPayload.getMessageAsString();
        Assert.assertEquals(value, payload);
    }

    @Test
    public void testSetStringPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "Hello World...!!!";
        BValue[] args = { new BMessage(carbonMsg), new BString(payload) };
        BValue[] returns = Functions.invoke(bFile, "testSetStringPayload", args);
        MessageDataSource newPayload = ((BMessage) returns[0]).getMessageDataSource();
        Assert.assertTrue(newPayload instanceof StringDataSource);
        String value = newPayload.getMessageAsString();
        Assert.assertEquals(value, payload);
    }

    @Test
    public void testEmptyString() {
        BValue[] returns = Functions.invoke(bFile, "testEmptyString");
        Assert.assertEquals(returns.length, 1);
        String returnString = returns[0].stringValue();
        Assert.assertEquals(returnString, "");
    }

    @Test
    public void testClone() {
        /*final String payload1 = "Hello World...!!! I am the Original Copy.";
        final String payload2 = "Hello World...!!! I am the Cloned Copy.";
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        carbonMsg.setStringMessageBody(payload1);
        BValue[] args = {new BMessage(carbonMsg), new BString(payload2)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testClone", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getReturnBValue(bContext).intValue(), 1);*/
    }

    // TODO : Add test cases fot other native functions.
}
