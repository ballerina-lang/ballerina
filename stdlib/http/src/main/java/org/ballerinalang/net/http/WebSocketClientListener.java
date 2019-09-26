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
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.values.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;

import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT;
import static org.ballerinalang.net.http.WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE;
import static org.ballerinalang.net.http.WebSocketUtil.handleExceptionAndDispatchCloseMessage;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;

/**
 * Client connector listener for WebSocket.
 *
 * @since 1.1.0
 */
public class WebSocketClientListener extends ClientListenerImpl {

    private WebSocketOpenConnectionInfo connectionInfo;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientListener.class);

    public void setConnectionInfo(WebSocketOpenConnectionInfo connectionInfo) {
        super.setConnectionInfo(connectionInfo);
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (hasRetryConfig(webSocketClient)) {
            if (statusCode == STATUS_CODE_ABNORMAL_CLOSURE && reconnect(connectionInfo)) {
                return;
            } else {
                if (statusCode != STATUS_CODE_ABNORMAL_CLOSURE) {
                    logger.debug(STATEMENT +
                            webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
                }
            }
        }
        handleExceptionAndDispatchCloseMessage(connectionInfo, webSocketCloseMessage);
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        if (hasRetryConfig(webSocketClient)) {
            if (throwable instanceof IOException && reconnect(connectionInfo)) {
                return;
            }
        }
        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
    }
}
