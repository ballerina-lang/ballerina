/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.bytetype;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour of byte values.
 * <p>
 * Defining a byte value
 * byte b;
 * b = 23;
 *
 * @since 0.964
 */

public class BByteValueTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/byte/byte-value.bal");
    }

    @Test(description = "Test byte value assignment")
    public void testByteValue() {
        BValue[] returns = BRunUtil.invoke(result, "testByteValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 34, "Invalid byte value returned.");
    }

    @Test(description = "Test byte function parameter")
    public void testByteParameter() {
        invokeByteInputFunction("testByteParam");
    }

    @Test(description = "Test global byte value assignment")
    public void testGlobalByte() {
        invokeByteInputFunction("testGlobalByte");
    }

    private void invokeByteInputFunction(String functionName) {
        byte input = 34;
        BValue[] args = {new BByte(input)};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to integer cast")
    public void testByteToIntCast() {
        byte input = 12;
        BValue[] args = {new BByte(input)};
        BValue[] returns = BRunUtil.invoke(result, "testByteToIntCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), (int) input, "Invalid integer value returned.");
    }

    @Test(description = "Test integer to byte cast")
    public void testIntToByteCast() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToByteExplicitCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), (byte) input, "Invalid byte value returned.");
    }

    @Test(description = "Test integer to byte explicit cast")
    public void testIntToByteExplicitCast() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToByteCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), (byte) input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to any cast")
    public void testByteToAnyCast() {
        byte input = 45;
        BValue[] args = {new BByte(input)};
        BValue[] returns = BRunUtil.invoke(result, "testByteToAnyCast", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);
        Assert.assertSame(returns[1].getClass(), BByte.class);
        BByte bByte = (BByte) returns[1];
        Assert.assertEquals(bByte.byteValue(), input, "Invalid byte value returned.");
    }


    @Test(description = "Test byte array to any cast")
    public void testByteArrayToAnyCast() {
        byte input1 = 34;
        byte input2 = 56;

        BByteArray bByteArrayIn = new BByteArray();
        bByteArrayIn.add(0, input1);
        bByteArrayIn.add(1, input2);
        BValue[] args = {bByteArrayIn};

        BValue[] returns = BRunUtil.invoke(result, "testByteArrayToAny", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);
        Assert.assertSame(returns[1].getClass(), BByteArray.class);

        BByteArray bByteArrayOut = (BByteArray) returns[1];
        Assert.assertEquals(bByteArrayOut.get(0), input1);
        Assert.assertEquals(bByteArrayOut.get(1), input2);
    }

    @Test(description = "Test integer to byte conversion")
    public void testIntToByteConversion() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToByteConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), (byte) input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to integer conversion")
    public void testByteToIntConversion() {
        byte input = 45;
        BValue[] args = {new BByte(input)};
        BValue[] returns = BRunUtil.invoke(result, "testByteToIntConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), (long) input, "Invalid integer value returned.");
    }


    @Test(description = "Test byte array value")
    public void testByteArrayValue() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByteArray.class);
    }

    @Test(description = "Test byte array assignment")
    public void testByteArrayAssignment() {
        byte input1 = 2;
        byte input2 = 56;
        byte input3 = 89;
        byte input4 = 23;

        BByteArray bByteArrayIn = new BByteArray();
        bByteArrayIn.add(0, input1);
        bByteArrayIn.add(1, input2);
        bByteArrayIn.add(2, input3);
        bByteArrayIn.add(3, input4);
        BValue[] args = {bByteArrayIn};

        BValue[] returns = BRunUtil.invoke(result, "testByteArrayAssignment", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByteArray.class);
        BByteArray bByteArrayOut = (BByteArray) returns[0];

        Assert.assertEquals(bByteArrayOut.get(0), input1);
        Assert.assertEquals(bByteArrayOut.get(1), input2);
        Assert.assertEquals(bByteArrayOut.get(2), input3);
        Assert.assertEquals(bByteArrayOut.get(3), input4);
    }


    @Test(description = "Test byte array length")
    public void testByteArrayLength() {
        invokeArrayLengthFunction("testByteArrayLength", 4);
    }

    @Test(description = "Test byte array zero length")
    public void testByteArrayZeroLength() {
        invokeArrayLengthFunction("testByteArrayZeroLength", 0);
    }

    private void invokeArrayLengthFunction(String functionName, int length) {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(length, bInteger.intValue(), "Invalid array size");
    }

    @Test(description = "Test byte array size increase")
    public void testByteArrayIncreaseSize() {
        invokeArrayLengthFunction("testByteArrayIncreaseSize", 10);
    }

    @Test(description = "Test byte array of array")
    public void testByteArrayOfArray() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayOfArray", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        BInteger bInteger1 = (BInteger) returns[1];
        Assert.assertEquals(3, bInteger.intValue(), "Invalid array size");
        Assert.assertEquals(4, bInteger1.intValue(), "Invalid array size");
    }

    @Test(description = "Test byte equal operation")
    public void testByteEqual() {
        BValue[] args = {new BByte(2), new BByte(3), new BByte(2)};
        BValue[] returns = BRunUtil.invoke(result, "testByteBinaryOperation", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        BBoolean boolean1 = (BBoolean) returns[0];
        BBoolean boolean2 = (BBoolean) returns[1];
        Assert.assertFalse(boolean1.booleanValue(), "Invalid result");
        Assert.assertTrue(boolean2.booleanValue(), "Invalid result");
    }

    @Test(description = "Test byte not equal operation")
    public void testByteNotEqual() {
        BValue[] args = {new BByte(12), new BByte(34), new BByte(12)};
        BValue[] returns = BRunUtil.invoke(result, "testByteBinaryNotEqualOperation", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        BBoolean boolean1 = (BBoolean) returns[0];
        BBoolean boolean2 = (BBoolean) returns[1];
        Assert.assertTrue(boolean1.booleanValue(), "Invalid result");
        Assert.assertFalse(boolean2.booleanValue(), "Invalid result");
    }


    @Test
    public void simpleWorkerMessagePassingTest() {
        BRunUtil.invoke(result, "testWorkerWithByteVariable", new BValue[0]);
    }

}
