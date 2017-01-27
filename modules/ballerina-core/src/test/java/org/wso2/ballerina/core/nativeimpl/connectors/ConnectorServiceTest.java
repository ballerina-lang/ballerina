/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.ballerina.core.nativeimpl.connectors;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.message.StringDataSource;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.MessageUtils;
import org.wso2.ballerina.lang.util.Services;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Test class for Connector service.
 */
public class ConnectorServiceTest {

    @BeforeClass()
    public void setup() {
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.message:setStringPayload_message_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        EnvironmentInitializer.initialize("lang/connectors/connector-in-service.bal");
    }

    @Test(description = "Test action3Resource")
    public void testAction3Resource() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action3", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "MyParam1");
    }

    @Test(description = "Test action1Resource")
    public void testAction1Resource() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "false");
    }

    @Test(description = "Test action1Resource after calling action2Resource")
    public void testAction2Resource() {
        CarbonMessage action2Req = MessageUtils.generateHTTPMessage("/invoke/action2", "GET");
        Services.invoke(action2Req);

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "true");
    }

    @Test(description = "Test action5Resource")
    public void testAction5Resource() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action5", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "MyParam1, MyParam1");
    }

    @Test(description = "Test action6Resource")
    public void testAction6Resource() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action6", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        //action level connector declaration not supported yet
        //Assert.assertEquals(stringDataSource.getValue(), "Hello, World");
    }

}
