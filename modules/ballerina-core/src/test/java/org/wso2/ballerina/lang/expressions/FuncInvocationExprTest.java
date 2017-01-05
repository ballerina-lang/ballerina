/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Local function invocation test
 *
 * @since 1.0.0
 */
public class FuncInvocationExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/funcInvocation-expr.bal");
    }

    @Test(description = "Test local function invocation expression")
    public void testFuncInvocationExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5), new BInteger(1)};
        BValue[] returns = Functions.invoke(bFile, "testFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 116;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test recursive function invocation")
    public void testFuncInvocationExprRecursive() {
        BValue[] args = {new BInteger(7)};
        BValue[] returns = Functions.invoke(bFile, "sum", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 28;
        Assert.assertEquals(actual, expected);

    }

    @Test(description = "Test local function invocation expression advanced")
    public void testFuncInvocationExprAdvanced() {
        BValue[] args = {new BInteger(100), new BInteger(5), new BInteger(1)};
        BValue[] returns = Functions.invoke(bFile, "funcInvocationWithinFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 322;
        Assert.assertEquals(actual, expected);
    }
}
