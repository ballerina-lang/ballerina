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

package org.ballerinalang.messaging.rabbitmq.nativeimpl.message;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.util.ChannelUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Acknowledge one or several received messages.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "basicAck",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.MESSAGE_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class BasicAck extends BlockingNativeCallableUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAck.class);

    @Override
    public void execute(Context context) {
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> messageObject = (BMap<String, BValue>) context.getRefArgument(0);
        Channel channel = RabbitMQUtils.getNativeObject(messageObject,
                RabbitMQConstants.CHANNEL_NATIVE_OBJECT, Channel.class, context);
        long deliveryTag = (long) messageObject.getNativeData(RabbitMQConstants.DELIVERY_TAG);
        boolean multiple = context.getBooleanArgument(0);
        boolean multipleAck = ChannelUtils.validateMultipleAcknowledgements(messageObject);
        boolean ackMode = ChannelUtils.validateAckMode(messageObject);
        if (!multipleAck && ackMode) {
            try {
                channel.basicAck(deliveryTag, multiple);
                messageObject.addNativeData(RabbitMQConstants.MESSAGE_ACK_STATUS, true);
            } catch (IOException exception) {
                LOGGER.error("Error occurred while positively acknowledging the message", exception);
                RabbitMQUtils.returnError("Error occurred while positively acknowledging the message: ",
                        context, exception);
            } catch (AlreadyClosedException exception) {
                LOGGER.error(RabbitMQConstants.CHANNEL_CLOSED_ERROR, exception);
                RabbitMQUtils.returnError(RabbitMQConstants.CHANNEL_CLOSED_ERROR,
                        context, exception);
            }
        } else if (multipleAck && ackMode) {
            RabbitMQUtils.returnError(RabbitMQConstants.MULTIPLE_ACK_ERROR, context,
                    new RabbitMQConnectorException(RabbitMQConstants.MULTIPLE_ACK_ERROR));
        } else {
            RabbitMQUtils.returnError(RabbitMQConstants.ACK_MODE_ERROR, context,
                    new RabbitMQConnectorException(RabbitMQConstants.ACK_MODE_ERROR));
        }
    }
}
