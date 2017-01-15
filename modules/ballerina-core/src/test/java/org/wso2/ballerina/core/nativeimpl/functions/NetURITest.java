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
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.http.Constants;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * TestCases for package ballerina.net.uri.
 */
public class NetURITest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.system:print_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/netUri.bal", symScope);
    }

    @Test
    public void testEncode() {
        BValue[] args = {
                new BString("http://localhost:9090"),
                new BString("http://localhost:9090/echoService/hello world/"),
                new BString("http://localhost:9090/echoService?type=string&value=hello world"),
                new BString("http://localhost:9090/echoService#abc"),
                new BString("http://localhost:9090/echoService:abc"),
                new BString("http://localhost:9090/echoService+abc"),
                new BString("http://localhost:9090/echoService*abc"),
                new BString("http://localhost:9090/echoService%abc"),
                new BString("http://localhost:9090/echoService~abc"),
        };

        for (BValue arg : args) {
            BValue[] inputArg = {arg};
            BValue[] returnVals = Functions.invoke(bFile, "testEncode", inputArg);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for " + arg.stringValue());
            Assert.assertFalse(returnVals[0].stringValue().contains(" "), "Encoded valued can't contain space.");
            Assert.assertFalse(returnVals[0].stringValue().contains("*"), "Encoded valued can't contain *.");
            Assert.assertFalse(returnVals[0].stringValue().contains("+"), "Encoded valued can't contain +.");
            Assert.assertFalse(returnVals[0].stringValue().contains("%7E"), "Encoded valued can't contain %7E.");
        }
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testEncodeNegative() {
        BValue[] inputArg = {new BString(null)};
        Functions.invoke(bFile, "testEncode", inputArg);
    }

    @Test
    public void testGetQueryParam() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        cMsg.setProperty(Constants.TO,
                "https://localhost:9090/EchoService?qparam1=value1&qparam2=value 2&qparam3=value3");
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Map<String, String> paramsExpected = new HashMap<>();
        paramsExpected.put("qparam1", "value1");
        paramsExpected.put("qparam2", "value 2");
        paramsExpected.put("qparam3", "value3");
        paramsExpected.put("qparam4", "");

        for (String qparam : paramsExpected.keySet()) {
            BValue[] inputArg = {msg, new BString(qparam)};
            BValue[] returnVals = Functions.invoke(bFile, "testGetQueryParam", inputArg);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for " + qparam);
            Assert.assertEquals(returnVals[0].stringValue(), paramsExpected.get(qparam));
        }

        cMsg.setProperty(Constants.TO, "https://localhost:9090/EchoService");
        msg = new BMessage();
        msg.setValue(cMsg);
        BValue[] inputArg = {msg, new BString("param1")};
        BValue[] returnVals = Functions.invoke(bFile, "testGetQueryParam", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");

    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testGetQueryParamNegative() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        cMsg.setProperty(Constants.TO, "https://localhost:9090/EchoService?a&b=test");
        msg.setValue(cMsg);
        BValue[] inputArg = {msg, new BString("a")};
        BValue[] returnVals = Functions.invoke(bFile, "testGetQueryParam", inputArg);
    }
}
