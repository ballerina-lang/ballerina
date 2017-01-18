/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

/**
 * Test cases for ballerina.lang.array
 */
public class ArrayTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.system:print_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/arrayTest.bal", symScope);
    }

    @Test
    public void testDoubleArrayLength() {
        BArray<BDouble> bDoubleBArray = new BArray<>(BDouble.class);
        bDoubleBArray.add(0, new BDouble(10));
        bDoubleBArray.add(1, new BDouble(11.1));
        bDoubleBArray.add(2, new BDouble(12.2));
        BValue[] args = {bDoubleBArray};
        BValue[] returnVals = Functions.invoke(bFile, "testDoubleArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testFloatArrayLength() {
        BArray<BFloat> barray = new BArray<>(BFloat.class);
        barray.add(0, new BFloat(10));
        barray.add(1, new BFloat(11.1f));
        barray.add(2, new BFloat(12.2f));
        BValue[] args = {barray};
        BValue[] returnVals = Functions.invoke(bFile, "testFloatArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testIntArrayLength() {
        BArray<BInteger> bArray = new BArray<>(BInteger.class);
        bArray.add(0, new BInteger(10));
        bArray.add(1, new BInteger(11));
        bArray.add(2, new BInteger(12));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testIntArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 4, "Length didn't match");
    }

    @Test
    public void testLongArrayLength() {
        BArray<BLong> bArray = new BArray<>(BLong.class);
        bArray.add(0, new BLong(10));
        bArray.add(1, new BLong(11));
        bArray.add(2, new BLong(12));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testLongArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 4, "Length didn't match");
    }

    @Test
    public void testStringArrayLength() {
        BArray<BString> bArray = new BArray<>(BString.class);
        bArray.add(0, new BString("Hello"));
        bArray.add(1, new BString("World"));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testStringArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testXMLArrayLength() {
        BArray<BXML> bArray = new BArray<>(BXML.class);
        bArray.add(0, new BXML("<t>a</t>"));
        bArray.add(1, new BXML("<t>b</t>"));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testXMLArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testJSONArrayLength() {
        BArray<BJSON> bArray = new BArray<>(BJSON.class);
        bArray.add(0, new BJSON("{ \"test\" : \"1\"}"));
        bArray.add(1, new BJSON("{ \"test\" : \"1\"}"));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testJSONArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testMessageArrayLength() {
        BArray<BMessage> bArray = new BArray<>(BMessage.class);
        bArray.add(0, new BMessage(new DefaultCarbonMessage()));
        bArray.add(1, new BMessage(new DefaultCarbonMessage()));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testMessageArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testDoubleArrayCopyOf() {
        final double v1 = 10;
        final double v2 = 11.1;
        BArray<BDouble> bArray = new BArray<>(BDouble.class);
        bArray.add(0, new BDouble(v1));
        bArray.add(1, new BDouble(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testDoubleArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertNotEquals(((BDouble) ((BArray) returnVals[0]).get(0)).doubleValue(), v1, "Found same value");
        Assert.assertNotEquals(((BDouble) ((BArray) returnVals[0]).get(1)).doubleValue(), v2, "Found same value");
    }

    @Test
    public void testFloatArrayCopyOf() {
        final float v1 = 10f;
        final float v2 = 11.1f;
        BArray<BFloat> bArray = new BArray<>(BFloat.class);
        bArray.add(0, new BFloat(v1));
        bArray.add(1, new BFloat(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testFloatArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertNotEquals(((BFloat) ((BArray) returnVals[0]).get(0)).floatValue(), v1, "Found same value");
        Assert.assertNotEquals(((BFloat) ((BArray) returnVals[0]).get(1)).floatValue(), v2, "Found same value");
    }

    @Test
    public void testIntArrayCopyOf() {
        final int v1 = 10;
        final int v2 = 11;
        BArray<BInteger> bArray = new BArray<>(BInteger.class);
        bArray.add(0, new BInteger(v1));
        bArray.add(1, new BInteger(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testIntArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertNotEquals(((BInteger) ((BArray) returnVals[0]).get(0)).intValue(), v1, "Found same value");
        Assert.assertNotEquals(((BInteger) ((BArray) returnVals[0]).get(1)).intValue(), v2, "Found same value");
    }

    @Test
    public void testLongArrayCopyOf() {
        final long v1 = 10;
        final long v2 = 11;
        BArray<BLong> bArray = new BArray<>(BLong.class);
        bArray.add(0, new BLong(v1));
        bArray.add(1, new BLong(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testLongArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertNotEquals(((BLong) ((BArray) returnVals[0]).get(0)).longValue(), v1, "Found same value");
        Assert.assertNotEquals(((BLong) ((BArray) returnVals[0]).get(1)).longValue(), v2, "Found same value");
    }

    @Test
    public void testStringArrayCopyOf() {
        final String v1 = "a";
        final String v2 = "b";
        BArray<BString> bArray = new BArray<>(BString.class);
        bArray.add(0, new BString(v1));
        bArray.add(1, new BString(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testStringArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(0).stringValue(), v1, "Found same value");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(1).stringValue(), v2, "Found same value");
    }

    @Test
    public void testXMLArrayCopyOf() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        BArray<BXML> bArray = new BArray<>(BXML.class);
        bArray.add(0, new BXML(v1));
        bArray.add(1, new BXML(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testXMLArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(0).stringValue(), v1, "Found same value");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(1).stringValue(), v2, "Found same value");
    }

    @Test
    public void testJSONArrayCopyOf() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        BArray<BJSON> bArray = new BArray<>(BJSON.class);
        bArray.add(0, new BJSON(v1));
        bArray.add(1, new BJSON(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testJSONArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(0).stringValue(), v1, "Found same value");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(1).stringValue(), v2, "Found same value");
    }

    @Test
    public void testMessageArrayCopyOf() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        BArray<BMessage> bArray = new BArray<>(BMessage.class);
        bArray.add(0, new BMessage(v1));
        bArray.add(1, new BMessage(v2));
        BValue[] args = {bArray};
        BValue[] returnVals = Functions.invoke(bFile, "testMessageArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
    }


    @Test
    public void testDoubleArrayCopyOfRange() {
        final double v1 = 10;
        final double v2 = 11.1;
        final double v3 = 11.2;
        final double v4 = 11.3;
        BArray<BDouble> bArray = new BArray<>(BDouble.class);
        bArray.add(0, new BDouble(v1));
        bArray.add(1, new BDouble(v2));
        bArray.add(2, new BDouble(v3));
        bArray.add(3, new BDouble(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testDoubleArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BDouble) ((BArray) returnVals[0]).get(0)).doubleValue(), v2, "Value didn't match");
        Assert.assertEquals(((BDouble) ((BArray) returnVals[0]).get(1)).doubleValue(), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testDoubleArrayCopyOfRangeNegative() {
        final double v1 = 10;
        final double v2 = 11.1;
        BArray<BDouble> bArray = new BArray<>(BDouble.class);
        bArray.add(0, new BDouble(v1));
        bArray.add(1, new BDouble(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testDoubleArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testDoubleArrayCopyOfRangNegativeMinusArgs() {
        final double v1 = 10;
        final double v2 = 11.1;
        BArray<BDouble> bArray = new BArray<>(BDouble.class);
        bArray.add(0, new BDouble(v1));
        bArray.add(1, new BDouble(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testDoubleArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }



    @Test
    public void testFloatArrayCopyOfRanged() {
        final float v1 = 10f;
        final float v2 = 11.1f;
        final float v3 = 11.2f;
        final float v4 = 11.3f;
        BArray<BFloat> bArray = new BArray<>(BFloat.class);
        bArray.add(0, new BFloat(v1));
        bArray.add(1, new BFloat(v2));
        bArray.add(2, new BFloat(v3));
        bArray.add(3, new BFloat(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testFloatArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BFloat) ((BArray) returnVals[0]).get(0)).floatValue(), v2, "Value didn't match");
        Assert.assertEquals(((BFloat) ((BArray) returnVals[0]).get(1)).floatValue(), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testFloatArrayCopyOfRangeNegative() {
        final float v1 = 10f;
        final float v2 = 11.1f;
        BArray<BFloat> bArray = new BArray<>(BFloat.class);
        bArray.add(0, new BFloat(v1));
        bArray.add(1, new BFloat(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testFloatArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testFloatArrayCopyOfRangNegativeMinusArgs() {
        final float v1 = 10f;
        final float v2 = 11.1f;
        BArray<BFloat> bArray = new BArray<>(BFloat.class);
        bArray.add(0, new BFloat(v1));
        bArray.add(1, new BFloat(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testFloatArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testIntArrayCopyOfRanged() {
        final int v1 = 10;
        final int v2 = 11;
        final int v3 = 12;
        final int v4 = 13;
        BArray<BInteger> bArray = new BArray<>(BInteger.class);
        bArray.add(0, new BInteger(v1));
        bArray.add(1, new BInteger(v2));
        bArray.add(2, new BInteger(v3));
        bArray.add(3, new BInteger(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testIntArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BInteger) ((BArray) returnVals[0]).get(0)).intValue(), v2, "Value didn't match");
        Assert.assertEquals(((BInteger) ((BArray) returnVals[0]).get(1)).intValue(), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testIntArrayCopyOfRangeNegative() {
        final int v1 = 10;
        final int v2 = 11;
        BArray<BInteger> bArray = new BArray<>(BInteger.class);
        bArray.add(0, new BInteger(v1));
        bArray.add(1, new BInteger(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testIntArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testIntArrayCopyOfRangNegativeMinusArgs() {
        final int v1 = 10;
        final int v2 = 11;
        BArray<BInteger> bArray = new BArray<>(BInteger.class);
        bArray.add(0, new BInteger(v1));
        bArray.add(1, new BInteger(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testIntArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testLongArrayCopyOfRange() {
        final long v1 = 10;
        final long v2 = 11;
        final long v3 = 12;
        final long v4 = 13;
        BArray<BLong> bArray = new BArray<>(BLong.class);
        bArray.add(0, new BLong(v1));
        bArray.add(1, new BLong(v2));
        bArray.add(2, new BLong(v3));
        bArray.add(3, new BLong(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testLongArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BLong) ((BArray) returnVals[0]).get(0)).longValue(), v2, "Value didn't match");
        Assert.assertEquals(((BLong) ((BArray) returnVals[0]).get(1)).longValue(), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testLongArrayCopyOfRangeNegative() {
        final long v1 = 10;
        final long v2 = 11;
        BArray<BLong> bArray = new BArray<>(BLong.class);
        bArray.add(0, new BLong(v1));
        bArray.add(1, new BLong(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testLongArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testLongArrayCopyOfRangNegativeMinusArgs() {
        final long v1 = 10;
        final long v2 = 11;
        BArray<BLong> bArray = new BArray<>(BLong.class);
        bArray.add(0, new BLong(v1));
        bArray.add(1, new BLong(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testLongArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testStringArrayCopyOfRange() {
        final String v1 = "a";
        final String v2 = "b";
        final String v3 = "c";
        final String v4 = "d";
        BArray<BString> bArray = new BArray<>(BString.class);
        bArray.add(0, new BString(v1));
        bArray.add(1, new BString(v2));
        bArray.add(2, new BString(v3));
        bArray.add(3, new BString(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testStringArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BArray) returnVals[0]).get(0).stringValue(), v2, "Value didn't match");
        Assert.assertEquals(((BArray) returnVals[0]).get(1).stringValue(), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testStringArrayCopyOfRangeNegative() {
        final String v1 = "a";
        final String v2 = "b";
        BArray<BString> bArray = new BArray<>(BString.class);
        bArray.add(0, new BString(v1));
        bArray.add(1, new BString(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testStringArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testStringArrayCopyOfRangNegativeMinusArgs() {
        final String v1 = "a";
        final String v2 = "b";
        BArray<BString> bArray = new BArray<>(BString.class);
        bArray.add(0, new BString(v1));
        bArray.add(1, new BString(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testStringArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testXMLArrayCopyOfRange() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        final String v3 = "<xml>c</xml>";
        final String v4 = "<xml>d</xml>";
        BArray<BXML> bArray = new BArray<>(BXML.class);
        bArray.add(0, new BXML(v1));
        bArray.add(1, new BXML(v2));
        bArray.add(2, new BXML(v3));
        bArray.add(3, new BXML(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testXMLArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(0).stringValue(), v1, "Found same value");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(1).stringValue(), v2, "Found same value");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testXMLArrayCopyOfRangeNegative() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        BArray<BXML> bArray = new BArray<>(BXML.class);
        bArray.add(0, new BXML(v1));
        bArray.add(1, new BXML(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testXMLArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testXMLArrayCopyOfRangNegativeMinusArgs() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        BArray<BXML> bArray = new BArray<>(BXML.class);
        bArray.add(0, new BXML(v1));
        bArray.add(1, new BXML(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testXMLArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testJSONArrayCopyOfRange() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        final String v3 = "{ \"json\" : \"3\"}";
        final String v4 = "{ \"json\" : \"4\"}";
        BArray<BJSON> bArray = new BArray<>(BJSON.class);
        bArray.add(0, new BJSON(v1));
        bArray.add(1, new BJSON(v2));
        bArray.add(2, new BJSON(v3));
        bArray.add(3, new BJSON(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testJSONArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(0).stringValue(), v1, "Found same value");
        Assert.assertNotEquals(((BArray) returnVals[0]).get(1).stringValue(), v2, "Found same value");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testJSONArrayCopyOfRangeNegative() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        BArray<BJSON> bArray = new BArray<>(BJSON.class);
        bArray.add(0, new BJSON(v1));
        bArray.add(1, new BJSON(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testJSONArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testJSONArrayCopyOfRangNegativeMinusArgs() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        BArray<BJSON> bArray = new BArray<>(BJSON.class);
        bArray.add(0, new BJSON(v1));
        bArray.add(1, new BJSON(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testJSONArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testMessageArrayCopyOfRange() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        final CarbonMessage v3 = new DefaultCarbonMessage();
        final CarbonMessage v4 = new DefaultCarbonMessage();
        BArray<BMessage> bArray = new BArray<>(BMessage.class);
        bArray.add(0, new BMessage(v1));
        bArray.add(1, new BMessage(v2));
        bArray.add(2, new BMessage(v3));
        bArray.add(3, new BMessage(v4));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = Functions.invoke(bFile, "testMessageArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BArray) returnVals[0]).size(), bArray.size(), "Found Same size array.");
        Assert.assertEquals(((BArray) returnVals[0]).size(), 2, "Incorrect Array size.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testMessageArrayCopyOfRangNegative() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        BArray<BMessage> bArray = new BArray<>(BMessage.class);
        bArray.add(0, new BMessage(v1));
        bArray.add(1, new BMessage(v2));
        BValue[] args = {bArray , new BInteger(1), new BInteger(3)};
        Functions.invoke(bFile, "testMessageArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "Array index out of range.*")
    public void testMessageArrayCopyOfRangNegativeMinusArgs() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        BArray<BMessage> bArray = new BArray<>(BMessage.class);
        bArray.add(0, new BMessage(v1));
        bArray.add(1, new BMessage(v2));
        BValue[] args = {bArray , new BInteger(-1), new BInteger(3)};
        Functions.invoke(bFile, "testMessageArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }


}
