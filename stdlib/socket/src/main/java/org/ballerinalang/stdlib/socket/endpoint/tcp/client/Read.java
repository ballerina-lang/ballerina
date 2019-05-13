/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.socket.endpoint.tcp.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.ReadPendingCallback;
import org.ballerinalang.stdlib.socket.tcp.ReadPendingSocketMap;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketService;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.DEFAULT_EXPECTED_READ_LENGTH;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * 'Read' method implementation of the socket caller action.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "read",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Read implements NativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Read.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> clientEndpoint = (BMap<String, BValue>) context.getRefArgument(0);
        int expectedLength = (int) context.getIntArgument(0);
        if (expectedLength != DEFAULT_EXPECTED_READ_LENGTH && expectedLength < 1) {
            String msg = "Requested byte length need to be 1 or more";
            callback.notifyFailure(SocketUtils.createSocketError(context, msg));
            return;
        }
        SocketService socketService = (SocketService) clientEndpoint.getNativeData(SocketConstants.SOCKET_SERVICE);
        SocketChannel socketChannel = (SocketChannel) clientEndpoint.getNativeData(SocketConstants.SOCKET_KEY);
        int socketHash = socketChannel.hashCode();
        ReadPendingCallback readPendingCallback = new ReadPendingCallback(context, callback, expectedLength, socketHash,
                socketService.getReadTimeout());
        ReadPendingSocketMap.getInstance().add(socketChannel.hashCode(), readPendingCallback);
        log.debug("Notify to invokeRead");
        SelectorManager.getInstance().invokeRead(socketHash, socketService.getResources() != null);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
