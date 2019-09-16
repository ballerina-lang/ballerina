/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.ballerinalang.net.http.websocketclientendpoint.FailoverContext;
import org.ballerinalang.net.http.websocketclientendpoint.RetryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.WebSocketConstants.BACK_OF_FACTOR;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTOR_FACTORY;
import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsInvalidHandshakeError;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_INTEVAL;
import static org.ballerinalang.net.http.WebSocketConstants.INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_COUNT;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECTING;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVDER_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVER;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_ERROR_DETAILS;
import static org.ballerinalang.net.http.WebSocketConstants.WSS_SCHEME;

/**
 * Utility class for WebSocket.
 */
public class WebSocketUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);

    static MapValue getServiceConfigAnnotation(ObjectValue service) {
        return (MapValue) service.getType().getAnnotation(HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION);
    }

    public static void handleHandshake(WebSocketService wsService, WebSocketConnectionManager connectionManager,
                                       HttpHeaders headers, WebSocketHandshaker webSocketHandshaker,
                                       NonBlockingCallback callback) {
        String[] subProtocols = wsService.getNegotiableSubProtocols();
        int idleTimeoutInSeconds = wsService.getIdleTimeoutInSeconds();
        int maxFrameSize = wsService.getMaxFrameSize();
        ServerHandshakeFuture future = webSocketHandshaker.handshake(subProtocols, idleTimeoutInSeconds * 1000, headers,
                maxFrameSize);
        future.setHandshakeListener(new ServerHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {
                ObjectValue webSocketEndpoint = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                        WebSocketConstants.WEBSOCKET_CALLER);
                ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                        WebSocketConstants.WEBSOCKET_CONNECTOR);

                webSocketEndpoint.set(WebSocketConstants.LISTENER_CONNECTOR_FIELD, webSocketConnector);
                populateEndpoint(webSocketConnection, webSocketEndpoint);
                WebSocketOpenConnectionInfo connectionInfo =
                        new WebSocketOpenConnectionInfo(wsService, webSocketConnection, webSocketEndpoint);
                connectionManager.addConnection(webSocketConnection.getChannelId(), connectionInfo);
                webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO,
                        connectionInfo);
                if (callback != null) {
                    callback.setReturnValues(webSocketEndpoint);
                    callback.notifySuccess();
                } else {
                    AttachedFunction onOpenResource = wsService.getResourceByName(
                            WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    if (onOpenResource != null) {
                        executeOnOpenResource(wsService, onOpenResource, webSocketEndpoint,
                                webSocketConnection);
                    } else {
                        readFirstFrame(webSocketConnection, webSocketConnector);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (callback != null) {
                    callback.notifyFailure(new WebSocketException(WsInvalidHandshakeError,
                                                                  "Unable to complete handshake:" +
                                                                          throwable.getMessage()));
                } else {
                    throw new WebSocketException(WsInvalidHandshakeError, "Unable to complete handshake");
                }
                logger.error("Unable to complete handshake", throwable);
            }
        });
    }

    static void executeOnOpenResource(WebSocketService wsService, AttachedFunction onOpenResource,
                                             ObjectValue webSocketEndpoint, WebSocketConnection webSocketConnection) {
        BType[] parameterTypes = onOpenResource.getParameterType();
        Object[] bValues = new Object[parameterTypes.length * 2];
        bValues[0] = webSocketEndpoint;
        bValues[1] = true;
        ObjectValue webSocketConnector =
                (ObjectValue) webSocketEndpoint.get(WebSocketConstants.LISTENER_CONNECTOR_FIELD);

        CallableUnitCallback onOpenCallableUnitCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                boolean isReady = (boolean) webSocketConnector.get(WebSocketConstants.CONNECTOR_IS_READY_FIELD);
                if (!isReady) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                boolean isReady = (boolean) webSocketConnector.get(WebSocketConstants.CONNECTOR_IS_READY_FIELD);
                if (!isReady) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
                ErrorHandlerUtils.printError("error: " + error.getPrintableStackTrace());
                closeDuringUnexpectedCondition(webSocketConnection);
            }
        };

        Executor.submit(wsService.getScheduler(), wsService.getBalService(), onOpenResource.getName(),
                onOpenCallableUnitCallback,
                null, bValues);
    }

    static void populateEndpoint(WebSocketConnection webSocketConnection, ObjectValue webSocketEndpoint) {
        webSocketEndpoint.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        String negotiatedSubProtocol = webSocketConnection.getNegotiatedSubProtocol();
        webSocketEndpoint.set(WebSocketConstants.LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD, negotiatedSubProtocol);
        webSocketEndpoint.set(WebSocketConstants.LISTENER_IS_SECURE_FIELD, webSocketConnection.isSecure());
        webSocketEndpoint.set(WebSocketConstants.LISTENER_IS_OPEN_FIELD, webSocketConnection.isOpen());
    }

    public static void handleWebSocketCallback(NonBlockingCallback callback,
                                               ChannelFuture webSocketChannelFuture, Logger log) {
        webSocketChannelFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                log.error("Error occurred ", cause);
                callback.setReturnValues(new WebSocketException(cause));

            } else {
                callback.setReturnValues(null);
            }
            callback.notifySuccess();
        });
    }

    public static void readFirstFrame(WebSocketConnection webSocketConnection, ObjectValue webSocketConnector) {
        webSocketConnection.readNextFrame();
        webSocketConnector.set(WebSocketConstants.CONNECTOR_IS_READY_FIELD, true);
    }

    /**
     * Closes the connection with the unexpected failure status code.
     *
     * @param webSocketConnection the websocket connection to be closed.
     */
    static void closeDuringUnexpectedCondition(WebSocketConnection webSocketConnection) {
        webSocketConnection.terminateConnection(1011, "Unexpected condition");

    }

    public static void setListenerOpenField(WebSocketOpenConnectionInfo connectionInfo) throws IllegalAccessException {
        connectionInfo.getWebSocketEndpoint().set(WebSocketConstants.LISTENER_IS_OPEN_FIELD,
                connectionInfo.getWebSocketConnection().isOpen());
    }

    public static int findMaxFrameSize(MapValue<String, Object> annotation) {
        long size = annotation.getIntValue(WebSocketConstants.ANNOTATION_ATTR_MAX_FRAME_SIZE);
        if (size <= 0) {
            return WebSocketConstants.DEFAULT_MAX_FRAME_SIZE;
        }
        try {
            return Math.toIntExact(size);
        } catch (ArithmeticException e) {
            logger.warn("The value set for maxFrameSize needs to be less than " + Integer.MAX_VALUE +
                    ". The maxFrameSize value is set to " + Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }

    }

    public static int findIdleTimeoutInSeconds(MapValue<String, Object> annAttrIdleTimeout) {
        long timeout = annAttrIdleTimeout.getIntValue(WebSocketConstants.ANNOTATION_ATTR_IDLE_TIMEOUT);
        if (timeout <= 0) {
            return 0;
        }
        try {
            return Math.toIntExact(timeout);
        } catch (ArithmeticException e) {
            logger.warn("The value set for idleTimeoutInSeconds needs to be less than" + Integer.MAX_VALUE +
                    ". The idleTimeoutInSeconds value is set to " + Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }

    static String[] findNegotiableSubProtocols(MapValue<String, Object> annAttrSubProtocols) {
        String[] subProtocolsInAnnotation = annAttrSubProtocols.getArrayValue(
                WebSocketConstants.ANNOTATION_ATTR_SUB_PROTOCOLS).getStringArray();
        if (subProtocolsInAnnotation == null) {
            return new String[0];
        }
        return Arrays.stream(subProtocolsInAnnotation).map(Object::toString)
                .toArray(String[]::new);
    }

    public static String getErrorMessage(Throwable err) {
        if (err.getMessage() == null) {
            return "Unexpected error occurred";
        }
        return err.getMessage();
    }

    public static MapValue<String, Object> createDetailRecord(String errMsg) {
        MapValue<String, Object> detail = BallerinaValues.createRecordValue(PROTOCOL_HTTP_PKG_ID,
                WEBSOCKET_ERROR_DETAILS);
        // The cause is null here. When there is a cause override the method to pass the proper cause
        return BallerinaValues.createRecord(detail, errMsg, null);
    }

    public static void populateClientConnectorConfig(MapValue<String, Object> clientEndpointConfig,
                                                     WebSocketClientConnectorConfig clientConnectorConfig,
                                                     String scheme) {
        clientConnectorConfig.setAutoRead(false); // Frames are read sequentially in ballerina.
        clientConnectorConfig.setSubProtocols(WebSocketUtil.findNegotiableSubProtocols(clientEndpointConfig));
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> headerValues = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                WebSocketConstants.CLIENT_CUSTOM_HEADERS_CONFIG);
        if (headerValues != null) {
            clientConnectorConfig.addHeaders(getCustomHeaders(headerValues));
        }

        long idleTimeoutInSeconds = WebSocketUtil.findIdleTimeoutInSeconds(clientEndpointConfig);
        if (idleTimeoutInSeconds > 0) {
            clientConnectorConfig.setIdleTimeoutInMillis((int) (idleTimeoutInSeconds * 1000));
        }

        clientConnectorConfig.setMaxFrameSize(WebSocketUtil.findMaxFrameSize(clientEndpointConfig));

        MapValue secureSocket = clientEndpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(clientConnectorConfig, secureSocket);
        } else if (scheme.equals(WSS_SCHEME)) {
            clientConnectorConfig.useJavaDefaults();
        }
        clientConnectorConfig.setWebSocketCompressionEnabled(
                clientEndpointConfig.getBooleanValue(WebSocketConstants.COMPRESSION_ENABLED_CONFIG));
    }

    private static Map<String, String> getCustomHeaders(MapValue<String, Object> headers) {
        Map<String, String> customHeaders = new HashMap<>();
        headers.keySet().forEach(
                key -> customHeaders.put(key, headers.get(key).toString())
        );
        return customHeaders;
    }

    /**
     * Populate the retry config.
     * @param retryConfig a retry config
     * @param retryConnectorConfig a doReconnect connector config
     */
    public static void populateRetryConnectorConfig(MapValue<String, Object> retryConfig,
                                                    RetryContext retryConnectorConfig) {
        int interval = Integer.parseInt(retryConfig.get(INTERVAL).toString());
        float backOfFactor = Float.parseFloat(retryConfig.get(BACK_OF_FACTOR).toString());
        int maxAttempts = Integer.parseInt(retryConfig.get(MAX_COUNT).toString());
        int maxInterval = Integer.parseInt(retryConfig.get(MAX_INTERVAL).toString());
        if (interval < 0) {
            logger.warn("The interval's value set for the configuration needs to be " +
                    "greater than -1. The interval[" + interval + "] value is set to 1000");
            interval = 1000;
        }
        if (backOfFactor < 0) {
            logger.warn("The decay's value set for the configuration needs to be " +
                    "greater than -1. The backOfFactor[" + backOfFactor + "] value is set to 1.0");
            backOfFactor = (float) 1.0;
        }
        if (maxInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The maxInterval[" + maxInterval + "] value is set to 30000");
            maxInterval =  30000;
        }
        if (maxAttempts < 0) {
            logger.warn("The maximum doReconnect attempt's value set for the configuration needs to be " +
                    "greater than -1. The maxAttempts[ " + maxAttempts + "] value is set to 0");
            maxAttempts = 0;
        }
        retryConnectorConfig.setInterval(interval);
        retryConnectorConfig.setMaxAttempts(maxAttempts);
        retryConnectorConfig.setBackOfFactor(backOfFactor);
        retryConnectorConfig.setMaxInterval(maxInterval);
    }

    /**
     * Populate the failover config.
     * @param clientEndpointConfig a client endpoint config
     * @param failoverClientConnectorConfig a failover client connector config
     * @param targetUrls target urls
     */
    public static void populateFailoverConnectorConfig(MapValue<String, Object> clientEndpointConfig,
                                                       FailoverContext failoverClientConnectorConfig,
                                                       ArrayList<String> targetUrls) {
        int failoverInterval = Integer.parseInt(clientEndpointConfig.get(FAILOVER_INTEVAL).toString());
        if (failoverInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The " + failoverInterval + "value is set to 1.0");
            failoverInterval = 1000;
        }
        failoverClientConnectorConfig.setFailoverInterval(failoverInterval);
        failoverClientConnectorConfig.setTargetUrls(targetUrls);
    }

    /**
     * Get the webSocket service.
     * @param clientEndpointConfig a client endpoint config
     * @param strand a strand
     * @return webSocketService
     */
    public static WebSocketService getWebSocketService(MapValue<String, Object> clientEndpointConfig, Strand strand) {
        Object clientService = clientEndpointConfig.get(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        WebSocketService wsService;
        if (clientService != null) {
            BType param = ((ObjectValue) clientService).getType().getAttachedFunctions()[0].getParameterType()[0];
            if (param == null || !(WebSocketConstants.WEBSOCKET_CLIENT_NAME.equals(
                    param.toString()) || WebSocketConstants.WEBSOCKET_FAILOVER_CLIENT_NAME.equals(
                            param.toString()))) {
                throw new WebSocketException("The callback service should be a WebSocket Client Service");
            }
            wsService = new WebSocketService((ObjectValue) clientService, strand.scheduler);
        } else {
            wsService = new WebSocketService(strand.scheduler);
        }
        return wsService;
    }

    public static void initialiseWebSocketConnection(String remoteUrl, ObjectValue webSocketClient,
                                                     WebSocketService wsService) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        String scheme = URI.create(remoteUrl).getScheme();
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, scheme);
        // Get the connector factory from the native data
        HttpWsConnectorFactory connectorFactory = ((HttpWsConnectorFactory) webSocketClient.
                getNativeData(CONNECTOR_FACTORY));
        // Create the client connector
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(clientConnectorConfig);
        // Add the client connector as the native data, when client is not as a failover client
        // Because Here, using one url So no need to create the client connector again
        if (hasRetryConfig(webSocketClient)) {
            webSocketClient.addNativeData(CLIENT_CONNECTOR, clientConnector);
        }
        establishWebSocketConnection(clientConnector, webSocketClient, wsService);
    }

    public static void initialiseWebSocketFailoverConnection(String remoteUrl, ObjectValue webSocketClient,
                                                             WebSocketService wsService) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, remoteUrl);
        // Get the connector factory from the native data
        HttpWsConnectorFactory connectorFactory = ((HttpWsConnectorFactory) webSocketClient.
                getNativeData(CONNECTOR_FACTORY));
        // Create the client connector
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(clientConnectorConfig);
        establishWebSocketFailoverConnection(clientConnector, webSocketClient, wsService);
    }

    private static void establishWebSocketFailoverConnection(WebSocketClientConnector clientConnector,
                                                    ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketFailoverClientListener clientConnectorListener = new WebSocketFailoverClientListener();
        boolean readyOnConnect = webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG).getBooleanValue(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        handshakeFuture.setClientHandshakeListener(
                new WebSocketFailoverClientHandshakeListener(webSocketClient, wsService, clientConnectorListener,
                        readyOnConnect, countDownLatch));
        waitingForHandShake(countDownLatch);
    }

    private static void establishWebSocketConnection(WebSocketClientConnector clientConnector,
                                                     ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketClientListener clientConnectorListener = new WebSocketClientListener();
        boolean readyOnConnect = webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG).getBooleanValue(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        handshakeFuture.setClientHandshakeListener(
                new WebSocketClientHandshakeListener(webSocketClient, wsService, clientConnectorListener,
                        readyOnConnect, countDownLatch));
        waitingForHandShake(countDownLatch);
    }

    private static void waitingForHandShake(CountDownLatch countDownLatch) {
        try {
            // Wait for 5 minutes before timeout
            if (!countDownLatch.await(60 * 5L, TimeUnit.SECONDS)) {
                throw new WebSocketException("Waiting for WebSocket handshake has not been successful");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException("Error occurred: " + e.getMessage());

        }
    }
    /**
     * Do the doReconnect when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     * @return if do the reconnection, return true
     */
    static boolean reconnect(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        RetryContext retryConnectorConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
        int interval = retryConnectorConfig.getInterval();
        int maxInterval = retryConnectorConfig.getMaxInterval();
        int maxAttempts = retryConnectorConfig.getMaxAttempts();
        int noOfReconnectAttempts = retryConnectorConfig.getReconnectAttempts();
        float backOfFactor = retryConnectorConfig.getBackOfFactor();
        WebSocketService wsService = connectionInfo.getService();
        WebSocketClientConnector clientConnector = (WebSocketClientConnector) webSocketClient.
                getNativeData(CLIENT_CONNECTOR);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        // Check the no of reconnection attempt equals with maximum reconnection attempt or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (((noOfReconnectAttempts < maxAttempts) && maxAttempts > 0) || maxAttempts == 0) {
            retryConnectorConfig.setReconnectAttempts(noOfReconnectAttempts + 1);
            logger.info(formatter.format(date.getTime()) + " " + RECONNECTING);
            setCountDownLatch(getWaitTime(interval, maxInterval, backOfFactor, noOfReconnectAttempts));
            establishWebSocketConnection(clientConnector, webSocketClient, wsService);
            return true;
        }
        logger.info(STATEMENT_FOR_RECONNECT +
                webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
        return false;
    }

    /**
     * Do the failover when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     * @return return true, if attempts the failover function
     */
    private static boolean failoverAndRetry(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.getNativeData(FAILOVER_CONFIG);
        int currentIndex = failoverConfig.getCurrentIndex();
        ArrayList targets = failoverConfig.getTargetUrls();
        WebSocketService wsService = connectionInfo.getService();
        int failoverInterval = failoverConfig.getFailoverInterval();
        // Set next url index
        currentIndex++;
        // Check current url index equals to target size or not. if equal, set the currentIndex = 0
        if (currentIndex == targets.size()) {
            currentIndex = 0;
        }
        // Check whether failover attempt finished or not.
        // If it isn't finished, call the initialiseWebSocketConnection()
        // if it is finished and has retry config, call the doReconnectForFailover()
        if (!failoverConfig.isFinishedFailover()) {
            // Check the current url index equals with previous connected url index or not.
            // If it isn't equal, call the initialiseWebSocketConnection()
            // if it equals, call the doReconnectForFailover()
            if (currentIndex != failoverConfig.getInitialIndex()) {
                failoverConfig.setCurrentIndex(currentIndex);
                setCountDownLatch(failoverInterval);
                initialiseWebSocketFailoverConnection(targets.get(currentIndex).toString(), webSocketClient,
                        wsService);
                return true;
            } else {
                failoverConfig.setCurrentIndex(currentIndex);
                failoverConfig.setFinishedFailover(true);
                return reconnectForFailover(connectionInfo, currentIndex);
            }
        } else if (hasRetryConfig(webSocketClient)) {
            failoverConfig.setCurrentIndex(currentIndex);
            return reconnectForFailover(connectionInfo, currentIndex);
        }
        logger.info(STATEMENT_FOR_FAILOVDER_RECONNECT + targets);
        return false;
    }

    /**
     * Do the failover when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     * @return return true, if attempts the failover function
     */
    private static boolean doFailover(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        FailoverContext failoverConfig = (FailoverContext)
                webSocketClient.getNativeData(FAILOVER_CONFIG);
        int currentIndex = failoverConfig.getCurrentIndex();
        ArrayList targets = failoverConfig.getTargetUrls();
        WebSocketService wsService = connectionInfo.getService();
        int failoverInterval = failoverConfig.getFailoverInterval();
        // Set next url index
        currentIndex++;
        // Check current url index equals to target size or not. if equal, set the currentIndex = 0
        if (currentIndex == targets.size()) {
            currentIndex = 0;
        }
        // Check the current url index equals with previous connected url index or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (currentIndex != failoverConfig.getInitialIndex()) {
            failoverConfig.setCurrentIndex(currentIndex);
            setCountDownLatch(failoverInterval);
            initialiseWebSocketFailoverConnection(targets.get(currentIndex).toString(), webSocketClient,
                    wsService);
            return true;
        }
        logger.info(STATEMENT_FOR_FAILOVER + targets);
        return false;
    }

    /**
     * Do the reconnection when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     */
    private static boolean reconnectForFailover(WebSocketOpenConnectionInfo connectionInfo, int currentIndex) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        RetryContext retryConnectorConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.getNativeData(FAILOVER_CONFIG);
        int interval = retryConnectorConfig.getInterval();
        int maxInterval = retryConnectorConfig.getMaxInterval();
        int maxAttempts = retryConnectorConfig.getMaxAttempts();
        int noOfReconnectAttempts = retryConnectorConfig.getReconnectAttempts();
        float backOfFactor = retryConnectorConfig.getBackOfFactor();
        WebSocketService wsService = connectionInfo.getService();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        ArrayList targets = failoverConfig.getTargetUrls();
        // Check the current url index equals with previous connected url index or not.
        // If it is equal, update the no of reconnection attempt by one
        if (currentIndex == failoverConfig.getInitialIndex()) {
            logger.info(formatter.format(date.getTime()) + " " + RECONNECTING);
            noOfReconnectAttempts++;
            retryConnectorConfig.setReconnectAttempts(noOfReconnectAttempts);
        }
        // Check the no of reconnection attempt equals with maximum reconnection attempt or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (((noOfReconnectAttempts < maxAttempts) && maxAttempts > 0) || maxAttempts == 0) {
            setCountDownLatch(getWaitTime(interval, maxInterval, backOfFactor, noOfReconnectAttempts));
            initialiseWebSocketFailoverConnection(targets.get(currentIndex).toString(), webSocketClient,
                    wsService);
            return true;
        }
        logger.info(STATEMENT_FOR_FAILOVDER_RECONNECT + targets);
        return false;
    }

    /**
     * Set waiting time before attempting to next doReconnect/failover.
     * @param interval interval
     */
    private static void setCountDownLatch(int interval) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        wait(countDownLatch, interval);
        countDownLatch.countDown();
    }

    public static boolean hasRetryConfig(ObjectValue webSocketClient) {
        return webSocketClient.getMapValue(WebSocketConstants.CLIENT_ENDPOINT_CONFIG).
                getMapValue(RETRY_CONFIG) != null;
    }

    private static void wait(CountDownLatch countDownLatch, int interval) {
        try {
            countDownLatch.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Calculate the waiting time.
     * @param interval interval
     * @param maxInterval maximum Interval
     * @param backOfFactor back of factor
     * @param reconnectAttempts no of doReconnect attempts
     * @return waiting time
     */
    public static int getWaitTime(int interval, int maxInterval, float backOfFactor, int reconnectAttempts) {
        interval = (int) (interval * Math.pow(backOfFactor, reconnectAttempts));
        if (interval > maxInterval) {
            interval = maxInterval;
        }
        return interval;
    }

    static void determineAction(WebSocketOpenConnectionInfo connectionInfo, Throwable throwable,
                                WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        if (hasRetryConfig(webSocketClient)) {
            // When connection lost, do the failover for remaining server urls. If failover fails,
            // do the reconnection
            if (failoverAndRetry(connectionInfo)) {
                return;
            }
        } else {
            // When connection lost, do the failover for remaining server urls.
            if (doFailover(connectionInfo)) {
                return;
            }
        }
        closeConnection(connectionInfo, throwable, webSocketCloseMessage);
    }

    static void handleExceptionAndDispatchCloseMessage(WebSocketOpenConnectionInfo connectionInfo,
                                                       WebSocketCloseMessage webSocketCloseMessage) {
        try {
            WebSocketDispatcher.dispatchCloseMessage(connectionInfo, webSocketCloseMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    public static void closeConnection(WebSocketOpenConnectionInfo connectionInfo, Throwable throwable,
                                       WebSocketCloseMessage webSocketCloseMessage) {
        if (throwable != null) {
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        } else {
            handleExceptionAndDispatchCloseMessage(connectionInfo, webSocketCloseMessage);
        }
    }

    private WebSocketUtil() {
    }
}
