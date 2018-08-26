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


package org.ballerinalang.test.securelistener;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for verifying no token propagation scenario.
 */
@Test(groups = "secure-listener-test")
public class NoTokenPropagationTest extends BaseTest {

    @BeforeGroups(value = "secure-listener-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{9090, 9091, 9092, 9093, 9094, 9095, 9096};
        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "secureListener").getAbsolutePath();
        String ballerinaConfPath = basePath + File.separator + "ballerina.conf";
        String[] args = new String[]{"--sourceroot", basePath, "--config", ballerinaConfPath};
        serverInstance.startBallerinaServer("secureservices", args, requiredPorts);
    }

    @Test(description = "No JWT Token propagation, authn failure test")
    public void testTokenPropagationSuccess() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9090, "passthrough"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }

    @AfterGroups(value = "secure-listener-test", alwaysRun = true)
    public void cleanup() throws Exception {
        serverInstance.stopServer();
    }
}
