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

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocketclientendpoint.RetryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;
import static org.ballerinalang.net.http.WebSocketUtil.setReconnectContexValue;
import static org.ballerinalang.net.http.WebSocketUtil.setWebSocketEndpoint;

/**
 * The handshake listener for the client.
 *
 * @since 1.0.0
 */
public class ClientHandshakeListener extends WebSocketClientHandshakeListener {
    private final WebSocketService wsService;
    private final ClientListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private CountDownLatch countDownLatch;
    private static final Logger logger = LoggerFactory.getLogger(ClientHandshakeListener.class);

    public ClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
                                   ClientListener clientConnectorListener, boolean readyOnConnect,
                                   CountDownLatch countDownLatch) {
        super(webSocketClient, wsService, clientConnectorListener, readyOnConnect, countDownLatch);
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        //Using only one service endpoint in the client as there can be only one connection.
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(carbonResponse));
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, webSocketConnection, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        webSocketClient.set(WebSocketConstants.CLIENT_CONNECTOR_FIELD, webSocketConnector);
        if (hasRetryConfig(webSocketClient)) {
            if (readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
            logger.info(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                    CLIENT_URL_CONFIG));
        } else {
            RetryContext retryConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
            logger.info(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                    CLIENT_URL_CONFIG));
            setWebSocketEndpoint(retryConfig, webSocketClient, webSocketConnection);
            if (retryConfig.isConnectionMade() || readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            setReconnectContexValue(retryConfig);
        }
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, null, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        countDownLatch.countDown();
        if (throwable instanceof IOException) {
            if (hasRetryConfig(webSocketClient) && reconnect(connectionInfo)) {
                return;
            }
        } else {
            logger.info("A connection has some issue that needs to fix.");
        }
        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
    }
}
