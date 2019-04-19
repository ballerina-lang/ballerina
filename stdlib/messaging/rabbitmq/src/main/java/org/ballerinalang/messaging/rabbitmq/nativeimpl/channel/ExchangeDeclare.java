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

package org.ballerinalang.messaging.rabbitmq.nativeimpl.channel;

import com.rabbitmq.client.Channel;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.util.ChannelUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actively declare a non-autodelete exchange with no extra arguments.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "exchangeDeclare",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.CHANNEL_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class ExchangeDeclare extends BlockingNativeCallableUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeDeclare.class);

    @Override
    public void execute(Context context) {
        BMap<String, BValue> channelObject = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> exchangeConfig = (BMap<String, BValue>) context.getRefArgument(1);
        Channel channel = RabbitMQUtils.getNativeObject(channelObject, RabbitMQConstants.CHANNEL_NATIVE_OBJECT,
                Channel.class, context);
        try {
            ChannelUtils.exchangeDeclare(channel, exchangeConfig);
        } catch (BallerinaException exception) {
            LOGGER.error("I/O exception while declaring the exchange", exception);
            RabbitMQUtils.returnError("RabbitMQ Client Error:", context, exception);
        }
    }
}
