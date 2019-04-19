/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats.nativeimpl.connection;

import io.nats.streaming.StreamingConnection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.nats.nativeimpl.Utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Close a given connection in the NATS server.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "close",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Connection", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Close implements NativeCallableUnit {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            Struct connectorEndpointStruct = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            StreamingConnection connection = (StreamingConnection) connectorEndpointStruct
                    .getNativeData(Constants.NATS_CONNECTION);
            connection.close();
        } catch (IOException | TimeoutException e) {
            // Both the error messages returned would not give java specific information
            // Hence the error message will not be overridden
            context.setReturnValues(Utils.createError(context, Constants.NATS_ERROR_CODE, e.getMessage()));
        } catch (InterruptedException ignore) {
            // ignore
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
