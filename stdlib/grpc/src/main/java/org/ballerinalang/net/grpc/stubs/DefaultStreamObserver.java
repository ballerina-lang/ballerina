/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.net.grpc.stubs;

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServiceResource;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.callback.ClientCallableUnitCallBack;
import org.ballerinalang.net.grpc.exception.ClientRuntimeException;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.MessageUtils.getHeaderStruct;

/**
 * This is Stream Observer Implementation for gRPC Client Call.
 *
 * @since 1.0.0
 */
public class DefaultStreamObserver implements StreamObserver {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultStreamObserver.class);
    private Map<String, ServiceResource> resourceMap = new HashMap<>();
    
    public DefaultStreamObserver(Service callbackService) throws
            GrpcClientException {
        if (callbackService == null) {
            throw new GrpcClientException("Error while building the connection. Listener Service does not exist");
        }
        for (Resource resource : callbackService.getResources()) {
            resourceMap.put(resource.getName(), new ServiceResource(resource));
        }
    }
    
    @Override
    public void onNext(Message value) {
        ServiceResource resource = resourceMap.get(GrpcConstants.ON_MESSAGE_RESOURCE);
        if (resource == null) {
            String message = "Error in listener service definition. onNext resource does not exists";
            LOG.error(message);
            throw new ClientRuntimeException(message);
        }
        List<ParamDetail> paramDetails = resource.getParamDetailList();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        BMap<String, BValue> headerStruct = null;
        if (resource.isHeaderRequired()) {
            headerStruct = getHeaderStruct(resource.getProgramFile());
        }
        BValue requestParam = value.getbMessage();
        if (requestParam != null) {
            signatureParams[0] = requestParam;
        }
        if (headerStruct != null) {
            signatureParams[signatureParams.length - 1] = headerStruct;
        }
        CallableUnitCallback callback = new ClientCallableUnitCallBack();
        Executor.submit(resource.getResource(), callback, null, null, signatureParams);
    }
    
    @Override
    public void onError(Message error) {
        ServiceResource onError = resourceMap.get(GrpcConstants.ON_ERROR_RESOURCE);
        if (onError == null) {
            String message = "Error in listener service definition. onError resource does not exists";
            LOG.error(message);
            throw new ClientRuntimeException(message);
        }
        List<ParamDetail> paramDetails = onError.getParamDetailList();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        BMap<String, BValue> headerStruct = null;
        if (onError.isHeaderRequired()) {
            headerStruct = getHeaderStruct(onError.getProgramFile());
        }
        BType errorType = paramDetails.get(0).getVarType();
        BError errorStruct = MessageUtils.getConnectorError((BErrorType) errorType, error.getError());
        signatureParams[0] = errorStruct;
        if (headerStruct != null && signatureParams.length == 2) {
            signatureParams[1] = headerStruct;
        }
        CallableUnitCallback callback = new ClientCallableUnitCallBack();
        Executor.submit(onError.getResource(), callback, null, null, signatureParams);
    }
    
    @Override
    public void onCompleted() {
        ServiceResource onCompleted = resourceMap.get(GrpcConstants.ON_COMPLETE_RESOURCE);
        if (onCompleted == null) {
            String message = "Error in listener service definition. onCompleted resource does not exists";
            LOG.error(message);
            throw new ClientRuntimeException(message);
        }
        List<ParamDetail> paramDetails = onCompleted.getParamDetailList();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        BMap<String, BValue> headerStruct = null;
        if (onCompleted.isHeaderRequired()) {
            headerStruct = getHeaderStruct(onCompleted.getProgramFile());
        }
        if (headerStruct != null && signatureParams.length == 1) {
            signatureParams[0] = headerStruct;
        }
        CallableUnitCallback callback = new ClientCallableUnitCallBack();
        Executor.submit(onCompleted.getResource(), callback, null, null, signatureParams);
    }
}
