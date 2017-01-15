/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Test Unary expression
 *
 * @since 1.0.0
 */
public class UnaryExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/unary-expr.bal");
    }

    @Test(description = "Test unary negative expression")
    public void integerUnaryExprTest() {
        BValue[] returns = Functions.invoke(bFile, "negativeIntTest");

        Assert.assertEquals(returns.length, 2);

        BInteger x = (BInteger) returns[0];
        Assert.assertSame(x.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(x.intValue(), (-5), "Invalid value returned.");

        BInteger y = (BInteger) returns[1];
        Assert.assertSame(y.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(y.intValue(), 5, "Invalid value returned.");
    }

    @Test(description = "Test int positive unary expression")
    public void positiveIntegerUnaryExprTest() {
        BValue[] returns = Functions.invoke(bFile, "positiveIntTest");

        Assert.assertEquals(returns.length, 2);

        BInteger x = (BInteger) returns[0];
        Assert.assertSame(x.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(x.intValue(), (+5), "Invalid value returned.");

        BInteger y = (BInteger) returns[1];
        Assert.assertSame(y.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(y.intValue(), +5, "Invalid value returned.");
    }

    @Test(description = "Test long unary negative expression")
    public void longUnaryExprTest() {
        BValue[] args = {new BLong(5)};
        BValue[] returns = Functions.invoke(bFile, "negativeLongTest", args);

        Assert.assertEquals(returns.length, 2);

        BLong x = (BLong) returns[0];
        Assert.assertSame(x.getClass(), BLong.class, "Invalid class type returned.");
        Assert.assertEquals(x.longValue(), (-5), "Invalid value returned.");

        BLong y = (BLong) returns[1];
        Assert.assertSame(y.getClass(), BLong.class, "Invalid class type returned.");
        Assert.assertEquals(y.longValue(), 5, "Invalid value returned.");
    }

    @Test(description = "Test long positive unary expression")
    public void positiveLongUnaryExprTest() {
        BValue[] args = {new BLong(5)};
        BValue[] returns = Functions.invoke(bFile, "positiveLongTest", args);

        Assert.assertEquals(returns.length, 2);

        BLong x = (BLong) returns[0];
        Assert.assertSame(x.getClass(), BLong.class, "Invalid class type returned.");
        Assert.assertEquals(x.longValue(), (+5), "Invalid value returned.");

        BLong y = (BLong) returns[1];
        Assert.assertSame(y.getClass(), BLong.class, "Invalid class type returned.");
        Assert.assertEquals(y.longValue(), +5, "Invalid value returned.");
    }

    @Test(description = "Test float unary negative expression")
    public void floatUnaryExprTest() {
        BValue[] returns = Functions.invoke(bFile, "negativeFloatTest");

        Assert.assertEquals(returns.length, 2);

        BFloat x = (BFloat) returns[0];
        Assert.assertSame(x.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(x.floatValue(), -5.0f, "Invalid value returned.");

        BFloat y = (BFloat) returns[1];
        Assert.assertSame(y.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(y.floatValue(), 5.0f, "Invalid value returned.");
    }

    @Test(description = "Test float positive unary expression")
    public void positiveFloatUnaryExprTest() {
        BValue[] returns = Functions.invoke(bFile, "positiveFloatTest");

        Assert.assertEquals(returns.length, 2);

        BFloat x = (BFloat) returns[0];
        Assert.assertSame(x.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(x.floatValue(), +5f, "Invalid value returned.");

        BFloat y = (BFloat) returns[1];
        Assert.assertSame(y.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(y.floatValue(), +5f, "Invalid value returned.");
    }

    @Test(description = "Test long unary negative expression")
    public void doubleUnaryExprTest() {
        BValue[] args = {new BDouble(5.0)};
        BValue[] returns = Functions.invoke(bFile, "negativeDoubleTest", args);

        Assert.assertEquals(returns.length, 2);

        BDouble x = (BDouble) returns[0];
        Assert.assertSame(x.getClass(), BDouble.class, "Invalid class type returned.");
        Assert.assertEquals(x.doubleValue(), -5.0, "Invalid value returned.");

        BDouble y = (BDouble) returns[1];
        Assert.assertSame(y.getClass(), BDouble.class, "Invalid class type returned.");
        Assert.assertEquals(y.doubleValue(), 5.0, "Invalid value returned.");
    }

    @Test(description = "Test long positive unary expression")
    public void positiveDoubleUnaryExprTest() {
        BValue[] args = {new BDouble(5.0)};
        BValue[] returns = Functions.invoke(bFile, "positiveDoubleTest", args);

        Assert.assertEquals(returns.length, 2);

        BDouble x = (BDouble) returns[0];
        Assert.assertSame(x.getClass(), BDouble.class, "Invalid class type returned.");
        Assert.assertEquals(x.doubleValue(), +5.0, "Invalid value returned.");

        BDouble y = (BDouble) returns[1];
        Assert.assertSame(y.getClass(), BDouble.class, "Invalid class type returned.");
        Assert.assertEquals(y.doubleValue(), +5.0, "Invalid value returned.");
    }

    @Test(description = "Test unary boolean not expression")
    public void booleanUnaryExprTest() {
        BValue[] returns = Functions.invoke(bFile, "booleanNotTest");

        Assert.assertEquals(returns.length, 3);

        BBoolean x = (BBoolean) returns[0];
        Assert.assertSame(x.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(x.booleanValue(), false, "Invalid value returned.");

        BBoolean y = (BBoolean) returns[1];
        Assert.assertSame(y.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(y.booleanValue(), true, "Invalid value returned.");

        BBoolean z = (BBoolean) returns[2];
        Assert.assertSame(z.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(z.booleanValue(), true, "Invalid value returned.");
    }

    @Test(description = "Test unary boolean not expression in if else")
    public void unaryExprInIfConditionTest() {
        BValue[] returns = Functions.invoke(bFile, "unaryExprInIfConditionTest");

        Assert.assertEquals(returns.length, 1);

        BBoolean x = (BBoolean) returns[0];
        Assert.assertSame(x.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(x.booleanValue(), true, "Invalid value returned.");

    }
}
