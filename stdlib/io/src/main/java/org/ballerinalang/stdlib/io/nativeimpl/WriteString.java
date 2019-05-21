/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.data.WriteStringEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina.io#writeString.
 *
 * @since 0.974.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "writeString",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WritableDataChannel",
                structPackage = "ballerina.io"),
        args = {@Argument(name = "value", type = TypeKind.STRING),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        isPublic = true
)
public class WriteString implements NativeCallableUnit {
    /**
     * Represents data channel.
     */
    private static final int DATA_CHANNEL_INDEX = 0;
    /**
     * Index which holds the value of the data to be written.
     */
    private static final int VALUE_INDEX = 0;
    /**
     * Index which will hold the string encoding.
     */
    private static final int ENCODING_INDEX = 1;

    /**
     * Triggers upon receiving the response.
     *
     * @param result the response received after writing string.
     */
    private static EventResult writeResponse(EventResult<Long, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        CallableUnitCallback callback = eventContext.getCallback();
        if (null != error) {
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, error.getMessage());
            context.setReturnValues(errorStruct);
        }
        callback.notifySuccess();
        return result;
    }

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> dataChannelStruct = (BMap<String, BValue>) context.getRefArgument(DATA_CHANNEL_INDEX);
        DataChannel channel = (DataChannel) dataChannelStruct.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        String value = context.getStringArgument(VALUE_INDEX);
        String encoding = context.getStringArgument(ENCODING_INDEX);
        EventContext eventContext = new EventContext(context, callback);
        WriteStringEvent writeStringEvent = new WriteStringEvent(channel, value, encoding, eventContext);
        Register register = EventRegister.getFactory().register(writeStringEvent, WriteString::writeResponse);
        eventContext.setRegister(register);
        register.submit();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static Object writeString(Strand strand, ObjectValue dataChannelObj, String value, String encoding) {
        //TODO : NonBlockingCallback is temporary fix to handle non blocking call
        NonBlockingCallback callback = new NonBlockingCallback(strand);

        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        EventContext eventContext = new EventContext(callback);
        WriteStringEvent writeStringEvent = new WriteStringEvent(channel, value, encoding, eventContext);
        Register register = EventRegister.getFactory().register(writeStringEvent, WriteString::writeStringResponse);
        eventContext.setRegister(register);
        register.submit();
        //TODO : Remove callback once strand non-blocking support is given
        callback.sync();
        return callback.getReturnValue();
    }

    /**
     * Triggers upon receiving the response.
     *
     * @param result the response received after writing string.
     */
    private static EventResult writeStringResponse(EventResult<Long, EventContext> result) {
        EventContext eventContext = result.getContext();
        //TODO : Remove callback once strand non-blocking support is given
        NonBlockingCallback callback = eventContext.getNonBlockingCallback();
        Throwable error = eventContext.getError();
        if (null != error) {
            callback.setReturnValues(IOUtils.createError(error.getMessage()));
        }
        callback.notifySuccess();
        return result;
    }
}
