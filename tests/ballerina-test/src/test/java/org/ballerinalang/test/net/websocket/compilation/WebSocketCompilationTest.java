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

package org.ballerinalang.test.net.websocket.compilation;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test WebSocket Service Compilation.
 */
public class WebSocketCompilationTest {
    @Test(description = "Successfully compiling WebSocket service")
    public void testSuccess() {
        CompileResult compileResult = BCompileUtil.compileAndSetup(
                "test-src/net/websocket/compilation/success.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Successfully compiling WebSocketClientService")
    public void testSuccessClient() {
        CompileResult compileResult = BCompileUtil.compileAndSetup(
                "test-src/net/websocket/compilation/success_client.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Successfully compiling WebSocket upgrade resource")
    public void testSuccessWebSocketUpgrade() {
        CompileResult compileResult = BCompileUtil.compileAndSetup(
                "test-src/net/websocket/compilation/success_websocket_upgrade.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Invalid signature for onOpen and onIdle resources")
    public void testFailOnOpenOnIdle() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/fail_onOpen_onIdle.bal");

        assertExpectedDiagnosticsLength(compileResult, 3);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onOpen resource in service " +
                "echo: The first parameter should be an endpoint", 29, 5);
        BAssertUtil.validateError(compileResult, 1, "Invalid resource signature for onIdleTimeout resource in " +
                "service echo: Expected parameter count = 1", 32, 5);
        BAssertUtil.validateError(compileResult, 2, "Invalid resource signature for onIdleTimeout resource in " +
                "service echo: The first parameter should be an endpoint", 32 , 5);
    }

    @Test(description = "Invalid parameter count for onText resource")
    public void testFailOnTextParamCount() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_onText_param_count.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onText resource in service " +
                "echo: Unexpected parameter count", 30, 5);
    }

    @Test(description = "Invalid signature for onText resource")
    public void testFailOnText() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/fail_onText.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onText resource in service " +
                "echo: The second parameter should be a string", 30, 5);
    }

    @Test(description = "Invalid signature for onBinary resource")
    public void testFailOnBinary() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/fail_onBinary.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onBinary resource in service " +
                "echo: The second parameter should be a blob", 30, 5);
    }

    @Test(description = "Invalid signature for onPing and onPong resources")
    public void testFailOnPingOnPong() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/fail_onPing_onPong.bal");

        assertExpectedDiagnosticsLength(compileResult, 3);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onPing resource in service " +
                "echo: The second parameter should be a blob", 30, 5);
        BAssertUtil.validateError(compileResult, 1, "Invalid resource signature for onPong resource in service " +
                "echo: Expected parameter count = 2", 33, 5);
        BAssertUtil.validateError(compileResult, 2, "Invalid resource signature for onPong resource in service " +
                "echo: The second parameter should be a blob", 33, 5);
    }

    @Test(description = "Invalid signature for onClose resource")
    public void testFailOnClose() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/fail_onClose.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onClose resource in service " +
                "echo: The third parameter should be a string", 30, 5);
    }

    @Test(description = "Invalid signature for onError resources")
    public void testFailOnError() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/fail_onError.bal");

        assertExpectedDiagnosticsLength(compileResult, 2);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onError resource in service " +
                "echo: Expected parameter count = 2", 30, 5);
        BAssertUtil.validateError(compileResult, 1, "Invalid resource signature for onError resource in service " +
                "echo: The second parameter should be an error", 30, 5);
    }

    @Test(description = "Invalid resource in WebSocketService")
    public void testInValidResource() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/invalid_resource.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource name onFind in service echo", 30, 5);
    }

    @Test(description = "Invalid resource onOpen in WebSocketClientService")
    public void testFailOnOpenClient() {
        CompileResult compileResult = BCompileUtil.compile("test-src/net/websocket/compilation/fail_onOpen_client.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "onOpen resource is not supported for WebSocketClientService echo",
                26, 5);
    }

    @Test(description = "WebSocketClientService is bound to an endpoint")
    public void testFailClientBoundToEndpoint() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_client_bound_to_endpoint.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "WebSocketClientService cannot be bound to an endpoint", 25, 1);
    }

    @Test(description = "WebSocket upgrade resource config has a no upgradeService")
    public void testFailWebSocketUpgradeNoService() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_websocket_upgrade_no_service.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "An upgradeService need to be specified for the WebSocket upgrade resource", 28, 5);
    }

    private void assertExpectedDiagnosticsLength(CompileResult compileResult, int expectedLength) {
        Assert.assertEquals(compileResult.getDiagnostics().length, expectedLength);
    }
}
