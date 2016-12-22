/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.nativeimpl.connectors.http;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "get",
        connectorName = HTTPConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                        type = TypeEnum.CONNECTOR),
                @Argument(name = "path", type = TypeEnum.STRING),
                @Argument(name = "message", type = TypeEnum.MESSAGE)
        },
        returnType = {TypeEnum.MESSAGE})
@Component(
        name = "action.net.http.get",
        immediate = true,
        service = AbstractNativeAction.class)
public class Get extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(Get.class);

    @Override
    public void execute(Context context) {

        logger.debug("Executing Native Action : Get");

        // Extract Argument values
        BConnector bConnector = (BConnector) getArgument(context, 0);
        String path = getArgument(context, 1).stringValue();
        BMessage bMessage = (BMessage) getArgument(context, 2);

        Connector connector = bConnector.value();
        if (!(connector instanceof HTTPConnector)) {
            logger.error("Need to use a HTTPConnector as the first argument");
            return;
        }
        // Prepare the message
        CarbonMessage cMsg = bMessage.value();
        prepareRequest(connector, path, cMsg);
        cMsg.setProperty(org.wso2.ballerina.core.runtime.net.http.Constants.HTTP_METHOD,
                org.wso2.ballerina.core.runtime.net.http.Constants.HTTP_METHOD_GET);

        // Execute the operation
        executeAction(context, cMsg);
    }
}
