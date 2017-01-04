/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.nativeimpl.connectors.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.http.Constants;
import org.wso2.ballerina.core.runtime.dispatching.ServiceDispatcher;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Dispatcher for HTTP Protocol
 *
 * @since 1.0.0
 */
public class HTTPServiceDispatcher implements ServiceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(HTTPServiceDispatcher.class);

    // Outer Map key=interface, Inner Map key=basePath
    private Map<String, Map<String, Service>> services = new HashMap<>();

    public Service findService(CarbonMessage cMsg, CarbonCallback callback) {

        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = Constants.DEFAULT_INTERFACE;
        }

        Map<String, Service> servicesOnInterface = services.get(interfaceId);
        if (servicesOnInterface == null) {
            throw new BallerinaException("No services found for interface : " + interfaceId);
        }

        String uri = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO);
        if (uri == null) {
            throw new BallerinaException("URI not found in the message");
        }
        uri = uri.split("\\?")[0]; //remove if any query parameters, before matching a service
        if (!uri.startsWith("/")) {
            uri = "/".concat(uri);
        }

        String[] path = uri.split("/");
        String basePath;
        if (path.length > 1) {
            basePath = "/".concat(path[1]);
        } else {
            basePath = Constants.DEFAULT_BASE_PATH;
        }
        String subPath = "";

        //TODO: Add regex support
        Service service = servicesOnInterface.get(basePath);  // 90% of the time we will find service from here
        if (service == null) {
            for (int i = 2; i < path.length; i++) {
                basePath = basePath.concat("/").concat(path[i]);
                service = servicesOnInterface.get(basePath);
                if (service != null) {
                    break;
                }
            }
        }

        // Check if there is a service with default base path ("/")
        if (service == null) {
            service = servicesOnInterface.get(Constants.DEFAULT_BASE_PATH);
            basePath = Constants.DEFAULT_BASE_PATH;
            subPath = uri;
        } else {
            String[] tempPaths = uri.split(basePath);
            if (tempPaths.length > 1) {
                subPath = tempPaths[1];
            }
        }

        if (service == null) {
            throw new BallerinaException("No Service found to handle request sent to : " + uri);
        }

        cMsg.setProperty(Constants.BASE_PATH, basePath);
        cMsg.setProperty(Constants.SUB_PATH, subPath);

        return service;
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_HTTP;
    }

    @Override
    public void serviceRegistered(Service service) {

        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getSymbolName().getName();
        for (Annotation annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getValueOfKeyValuePair(Constants.ANNOTATION_SOURCE_KEY_INTERFACE);
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePath = annotation.getValue();
            }
        }
        if (basePath.startsWith("\"")) {
            basePath = basePath.substring(1, basePath.length() - 1);
        }

        if (!basePath.startsWith("/")) {
            basePath = "/".concat(basePath);
        }

        Map<String, Service> servicesOnInterface = services.get(listenerInterface);
        if (servicesOnInterface == null) {
            // Assumption : this is always sequential, no two simultaneous calls can get here
            servicesOnInterface = new HashMap<>();
            services.put(listenerInterface, servicesOnInterface);
            HTTPListenerManager.getInstance().bindInterface(listenerInterface);
        }
        if (servicesOnInterface.containsKey(basePath)) {
            throw new BallerinaException(
                    "Service with base path :" + basePath + " already exists in listener : " + listenerInterface);
        }

        servicesOnInterface.put(basePath, service);
        log.info("Service deployed : {}:{} with context {}", service.getSymbolName().getPkgName(),
                service.getSymbolName().getName(), basePath);

    }

    @Override
    public void serviceUnregistered(Service service) {

        String listenerInterface = Constants.DEFAULT_INTERFACE;
        // String basePath = Constants.DEFAULT_BASE_PATH;
        String basePath = service.getSymbolName().getName();
        
        for (Annotation annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getValueOfKeyValuePair(Constants.ANNOTATION_SOURCE_KEY_INTERFACE);
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePath = annotation.getValue();
            }
        }
        
        if (basePath.startsWith("\"")) {
            basePath = basePath.substring(1, basePath.length() - 1);
        }

        if (!basePath.startsWith("/")) {
            basePath = "/".concat(basePath);
        }
        
        Map<String, Service> servicesOnInterface = services.get(listenerInterface);
        if (servicesOnInterface != null) {
            servicesOnInterface.remove(basePath);
            if (servicesOnInterface.isEmpty()) {
                services.remove(listenerInterface);
                HTTPListenerManager.getInstance().unbindInterface(listenerInterface);
            }
        }
    }
}
