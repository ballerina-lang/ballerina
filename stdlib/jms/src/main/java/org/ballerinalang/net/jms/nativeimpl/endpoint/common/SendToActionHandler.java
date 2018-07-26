/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.net.jms.nativeimpl.endpoint.common;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

/**
 * Message send action handler.
 */
public class SendToActionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendToActionHandler.class);

    private SendToActionHandler() {
    }

    public static void handle(Context context) {
        Struct queueSenderBObject = BallerinaAdapter.getReceiverObject(context);
        MessageProducer messageProducer = BallerinaAdapter.getNativeObject(queueSenderBObject,
                                                                           Constants.JMS_PRODUCER_OBJECT,
                                                                           MessageProducer.class,
                                                                           context);
        SessionConnector sessionConnector = BallerinaAdapter.getNativeObject(queueSenderBObject,
                                                                             Constants.SESSION_CONNECTOR_OBJECT,
                                                                             SessionConnector.class,
                                                                             context);
        
        BMap<String, BValue> destinationBObject = ((BMap<String, BValue>) context.getRefArgument(1));
        BMap<String, BValue> messageBObject = ((BMap<String, BValue>) context.getRefArgument(2));
        Message message = BallerinaAdapter.getNativeObject(messageBObject,
                                                           Constants.JMS_MESSAGE_OBJECT,
                                                           Message.class,
                                                           context);
        Destination destination = BallerinaAdapter.getNativeObject(destinationBObject,
                                                           Constants.JMS_DESTINATION_OBJECT,
                                                           Destination.class,
                                                           context);
        try {
            sessionConnector.handleTransactionBlock(context);
            messageProducer.send(destination, message);
        } catch (JMSException e) {
            BallerinaAdapter.returnError("Message receiving failed.", context, e);
        }
    }
}
