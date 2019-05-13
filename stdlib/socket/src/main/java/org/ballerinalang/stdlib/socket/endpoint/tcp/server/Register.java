/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.endpoint.tcp.server;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.tcp.SocketService;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;

import java.nio.channels.ServerSocketChannel;
import java.util.Map;

import static org.ballerinalang.stdlib.socket.SocketConstants.READ_TIMEOUT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SERVER_SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;

/**
 * Register socket listener service.
 *
 * @since 0.985.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Register extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        Struct listenerEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        final SocketService socketService = getSocketService(context, listenerEndpoint);
        listenerEndpoint.addNativeData(SOCKET_SERVICE, socketService);
        context.setReturnValues();
    }

    private SocketService getSocketService(Context context, Struct listenerEndpoint) {
        Map<String, Resource> resources = getResourceMap(context);
        ServerSocketChannel serverSocket = (ServerSocketChannel) listenerEndpoint.getNativeData(SERVER_SOCKET_KEY);
        long timeout = (long) listenerEndpoint.getNativeData(READ_TIMEOUT);
        return new SocketService(serverSocket, resources, timeout);
    }

    private Map<String, Resource> getResourceMap(Context context) {
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        return SocketUtils.getResourceRegistry(service);
    }
}
