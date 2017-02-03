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

package org.wso2.ballerina.lang.service;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.message.StringDataSource;
import org.wso2.ballerina.core.nativeimpl.connectors.http.server.HTTPResourceDispatcher;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;
import org.wso2.ballerina.core.utils.MessageUtils;
import org.wso2.ballerina.lang.util.Services;
import org.wso2.carbon.messaging.CarbonMessage;

import java.nio.ByteBuffer;

/**
 * Service/Resource dispatching test class.
 */
public class ServiceTest {

    @BeforeClass
    public void setup() {
        EnvironmentInitializer.initialize("lang/service/echoService.bal");
    }

    @Test
    public void testServiceDispatching() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        // TODO: Improve with more assets
    }

    @Test(description = "Test for protocol availability check", expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = ".* protocol not defined .*")
    public void testProtocolAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        cMsg.removeProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        Services.invoke(cMsg);
    }

    @Test(description = "Test for service dispatcher availability check",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = ".* no service dispatcher available .*")
    public void testServiceDispatcherAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, "FOO");   // setting incorrect protocol
        Services.invoke(cMsg);
    }

    @Test(description = "Test for service availability check",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = ".* no service found .*")
    public void testServiceAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/foo/message", "GET");
        Services.invoke(cMsg);
    }

    @Test(description = "Test for resource dispatcher availability check",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = ".* no resource dispatcher available .*")
    public void testResourceDispatcherAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        DispatcherRegistry.getInstance().unregisterResourceDispatcher("http"); // Remove http resource dispatcher
        try {
            Services.invoke(cMsg);
        } finally {
            DispatcherRegistry.getInstance().registerResourceDispatcher(new HTTPResourceDispatcher()); // Add back
        }
    }

    @Test(description = "Test for resource availability check",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = ".* no resource found .*")
    public void testResourceAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/bar", "GET");
        Services.invoke(cMsg);
    }

    @Test
    public void testSetString() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/setString", "POST");
        cMsg.addMessageBody(ByteBuffer.wrap("hello".getBytes()));
        cMsg.setEndOfMsgAdded(true);
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
    }

    @Test
    public void testGetString() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "");
    }

    @Test
    public void testGetStringAfterSetString() {
        CarbonMessage setStringCMsg = MessageUtils.generateHTTPMessage("/echo/setString", "POST");
        String stringPayload = "hello";
        setStringCMsg.addMessageBody(ByteBuffer.wrap(stringPayload.getBytes()));
        setStringCMsg.setEndOfMsgAdded(true);
        Services.invoke(setStringCMsg);

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), stringPayload);
    }

    //TODO: add more test cases

}
