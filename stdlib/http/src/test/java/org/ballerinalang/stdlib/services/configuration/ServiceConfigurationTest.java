/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.services.configuration;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test case for services with multiple http:ServiceConfig annotations.
 *
 * @since 0.95.4
 */
public class ServiceConfigurationTest {

    @Test(description = "Tests for multiple service configs in a resource")
    public void testDuplicateServiceConfigAnnotations() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/services/configuration/service-config-annotation.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag.length, 1);
        Assert.assertEquals(diag[0].getMessage(),
                            "multiple service configuration annotations found in service : helloWorldServiceConfig");
    }

    @Test(description = "Test for configuring a service")
    public void testConfiguringAService() throws IOException {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, Paths.get(resourceRoot, "datafiles", "service-config.conf").toString(), null);

        Path sourceRoot = Paths.get(resourceRoot, "test-src", "services", "configuration");
        CompileResult configuredService = BServiceUtil
                .setupProgramFile(this, sourceRoot.resolve("service_configuration.bal").toString());

        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/hello", "GET");
        HttpCarbonMessage responseMsg = Services.invokeNew(configuredService, "backendEP", requestMsg);

        Assert.assertNotNull(responseMsg);
    }
}
