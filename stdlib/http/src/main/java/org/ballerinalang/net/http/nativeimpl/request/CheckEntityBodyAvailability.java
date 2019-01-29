/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.mime.util.EntityBodyHandler.checkEntityBodyAvailability;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.net.http.HttpUtil.checkRequestBodySizeHeadersAvailability;

/**
 * Check whether the entity body is present. Entity body can either be a byte channel, fully constructed
 * message data source or a set of body parts.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "checkEntityBodyAvailability",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Request",
                             structPackage = "ballerina/http"),
        isPublic = false
)
public class CheckEntityBodyAvailability extends BlockingNativeCallableUnit {

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Context context) {
        BMap<String, BValue> requestStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
        BMap<String, BValue> entity = (BMap<String, BValue>) requestStruct.get(REQUEST_ENTITY_FIELD);
        context.setReturnValues(
                new BBoolean(lengthHeaderCheck(requestStruct) || checkEntityBodyAvailability(entity)));
    }

    private boolean lengthHeaderCheck(BMap<String, BValue> requestStruct) {
        Object outboundMsg = requestStruct.getNativeData(TRANSPORT_MESSAGE);
        if (outboundMsg == null) {
            return false;
        }
        return checkRequestBodySizeHeadersAvailability((HttpCarbonMessage) outboundMsg);
    }
}
