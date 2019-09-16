/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.websocket;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This Class tests retry support of WebSocket client and server.
 */
@Test(groups = {"websocket-test"})
public class RetryClientTest extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer;
    private String url;
    private int port;
    private boolean sslEnabled;

    public RetryClientTest(int port, boolean sslEnabled, String url) {
        this.port = port;
        this.sslEnabled = sslEnabled;
        this.url = url;
    }

    @Test(description = "Tests sending and receiving of text frames in WebSockets with reconnect")
    public void testRetry() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String textSent = "hi all";
        remoteServer = new WebSocketRemoteServer(port, sslEnabled);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        remoteServer.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        remoteServer.stop();
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

    @Test(description = "Tests sending and receiving of binary frames in WebSocket with reconnect")
    public void testBinaryFrameForRetry() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        remoteServer = new WebSocketRemoteServer(port, sslEnabled);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        remoteServer.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer.stop();
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

    @Test(description = "Tests sending and receiving of binary frames in WebSocket with multiple reconnect " +
            "attempts")
    public void testMultipleRetryAttempts() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String text = "hi madam";
        ByteBuffer bufferData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        remoteServer = new WebSocketRemoteServer(port, sslEnabled);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        remoteServer.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(text);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        remoteServer.stop();
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferData);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferData);
        client.shutDown();
        remoteServer.stop();
        CountDownLatch countDownLatch3 = new CountDownLatch(1);
        countDownLatch3.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }
}
