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
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetStringPayload;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Instance creation expression test.
 *
 * @since 1.0.0
 */
public class InstanceCreationExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new SetStringPayload());
        bFile = ParserUtils.parseBalFile("lang/expressions/instance-creation-expr.bal", symScope);
    }

    @Test(description = "Test instance creation expression")
    public void testArrayAccessExpr() {
        BValue[] args = { new BString("Test Payload") };
        BValue[] returns = Functions.invoke(bFile, "testMessagePayload", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMessage.class);

        String actual = ((BMessage) returns[0]).getBuiltPayload().stringValue();
        String expected = "Test Payload";
        Assert.assertEquals(actual, expected);
    }

}
