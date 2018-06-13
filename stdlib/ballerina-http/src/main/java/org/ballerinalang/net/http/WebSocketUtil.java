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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;


/**
 * Utility class for websockets.
 */
public abstract class WebSocketUtil {

    public static ProgramFile getProgramFile(Resource resource) {
        return resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
    }

    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service
                .getAnnotationList(pkgPath, WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION);

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "Multiple service configuration annotations found in service: " + service.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }

    public static void handleHandshake(WebSocketService wsService, WebSocketConnectionManager connectionManager,
                                       HttpHeaders headers, WebSocketInitMessage initMessage, Context context,
                                       CallableUnitCallback callback) {
        String[] subProtocols = wsService.getNegotiableSubProtocols();
        int idleTimeoutInSeconds = wsService.getIdleTimeoutInSeconds();
        int maxFrameSize = wsService.getMaxFrameSize();
        ServerHandshakeFuture future = initMessage.handshake(subProtocols, true, idleTimeoutInSeconds * 1000, headers,
                                                             maxFrameSize);
        future.setHandshakeListener(new ServerHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {
                // TODO: Need to create new struct
                BStruct webSocketEndpoint = BLangConnectorSPIUtil.createObject(
                        wsService.getResources()[0].getResourceInfo().getServiceInfo().getPackageInfo()
                                .getProgramFile(), PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_ENDPOINT);
                BStruct webSocketConnector = BLangConnectorSPIUtil.createObject(
                        wsService.getResources()[0].getResourceInfo().getServiceInfo().getPackageInfo()
                                .getProgramFile(), PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_CONNECTOR);

                webSocketEndpoint.setRefField(1, webSocketConnector);
                populateEndpoint(webSocketConnection, webSocketEndpoint);
                WebSocketOpenConnectionInfo connectionInfo =
                        new WebSocketOpenConnectionInfo(wsService, webSocketConnection, webSocketEndpoint);
                connectionManager.addConnection(webSocketConnection.getId(), connectionInfo);
                webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO,
                                                 connectionInfo);
                if (context != null && callback != null) {
                    context.setReturnValues(webSocketEndpoint);
                    callback.notifySuccess();
                } else {
                    Resource onOpenResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    if (onOpenResource != null) {
                        executeOnOpenResource(onOpenResource, webSocketEndpoint, webSocketConnection);
                    } else {
                        readFirstFrame(webSocketConnection, webSocketConnector);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (context != null) {
                    context.setReturnValues();
                }
                if (callback != null) {
                    callback.notifyFailure(
                            HttpUtil.getError(context, "Unable to complete handshake:" + throwable.getMessage()));
                }
                throw new BallerinaConnectorException("Unable to complete handshake", throwable);
            }
        });
    }

    public static void executeOnOpenResource(Resource onOpenResource, BStruct webSocketEndpoint,
                                             WebSocketConnection webSocketConnection) {
        List<ParamDetail> paramDetails =
                onOpenResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = webSocketEndpoint;
        BStruct webSocketConnector = (BStruct) webSocketEndpoint.getRefField(1);

        CallableUnitCallback onOpenCallableUnitCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                if (webSocketConnector.getBooleanField(WebSocketConstants.CONNECTOR_IS_READY_INDEX) == 0) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
            }

            @Override
            public void notifyFailure(BStruct error) {
                if (webSocketConnector.getBooleanField(WebSocketConstants.CONNECTOR_IS_READY_INDEX) == 0) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
                ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
            }
        };
        //TODO handle BallerinaConnectorException
        Executor.submit(onOpenResource, onOpenCallableUnitCallback, null, null, bValues);
    }

    public static void populateEndpoint(WebSocketConnection webSocketConnection, BStruct webSocketEndpoint) {
        webSocketEndpoint.setStringField(WebSocketConstants.LISTENER_ID_INDEX, webSocketConnection.getId());
        webSocketEndpoint.setStringField(WebSocketConstants.LISTENER_NEGOTIATED_SUBPROTOCOLS_INDEX,
                                         webSocketConnection.getSession().getNegotiatedSubprotocol());
        webSocketEndpoint.setBooleanField(WebSocketConstants.LISTENER_IS_SECURE_INDEX,
                                          webSocketConnection.getSession().isSecure() ? 1 : 0);
        webSocketEndpoint.setBooleanField(WebSocketConstants.LISTENER_IS_OPEN_INDEX,
                                          webSocketConnection.getSession().isOpen() ? 1 : 0);
    }

    public static void handleWebSocketCallback(Context context, CallableUnitCallback callback,
                                               ChannelFuture webSocketChannelFuture) {
        webSocketChannelFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                context.setReturnValues(HttpUtil.getError(context, cause));
            } else {
                context.setReturnValues();
            }
            callback.notifySuccess();
        });
    }

    /**
     * Refactor the given URI.
     *
     * @param uri URI to refactor.
     * @return refactored URI.
     */
    public static String refactorUri(String uri) {
        if (!uri.startsWith("/")) {
            uri = "/".concat(uri);
        }

        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    public static void readFirstFrame(WebSocketConnection webSocketConnection, BStruct webSocketConnector) {
        webSocketConnection.readNextFrame();
        webSocketConnector.setBooleanField(WebSocketConstants.CONNECTOR_IS_READY_INDEX, 1);
    }
}
