/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.calleraction;

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.util.observability.ObserveUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.ballerinalang.net.grpc.GrpcConstants.CALLER;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_RESPONDER_REF_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageUtils.getMappingHttpStatusCode;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;

/**
 * Extern function to send server error the caller.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "sendError",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CALLER,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class SendError extends BlockingNativeCallableUnit {
    private static final Logger LOG = LoggerFactory.getLogger(SendError.class);
    private static final int MESSAGE_HEADER_REF_INDEX = 1;

    @Override
    public void execute(Context context) {
        BMap<String, BValue> endpointClient = (BMap<String, BValue>) context.getRefArgument(CLIENT_RESPONDER_REF_INDEX);
        BValue headerValues = context.getNullableRefArgument(MESSAGE_HEADER_REF_INDEX);
        long statusCode = context.getIntArgument(0);
        String errorMsg = context.getStringArgument(0);
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(context);

        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);
        if (responseObserver == null) {
            context.setError(MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while sending the " +
                            "error. Response observer not found."))));
        } else {
            try {
                // Update response headers when request headers exists in the context.
                HttpHeaders headers = null;
                Message errorMessage = new Message(new StatusRuntimeException(Status.fromCodeValue((int) statusCode)
                        .withDescription(errorMsg)));
                if (headerValues != null && headerValues.getType().getTag() == TypeTags.OBJECT_TYPE_TAG) {
                    headers = (HttpHeaders) ((BMap<String, BValue>) headerValues).getNativeData(MESSAGE_HEADERS);
                }
                if (headers != null) {
                    errorMessage.setHeaders(headers);
                }
                observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_HTTP_STATUS_CODE,
                        String.valueOf(getMappingHttpStatusCode((int) statusCode))));
                responseObserver.onError(errorMessage);
            } catch (Exception e) {
                LOG.error("Error while sending error to caller.", e);
                context.setError(MessageUtils.getConnectorError(e));
            }
        }
    }
}
