/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.records.DelimitedRecordWriteEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina/io#writeTextRecord.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "WritableTextRecordChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class WriteTextRecord implements NativeCallableUnit {

    /**
     * Index of the record channel in ballerina/io#writeTextRecord.
     */
    private static final int RECORD_CHANNEL_INDEX = 0;

    /**
     * Index of the content in ballerina/io#writeTextRecord.
     */
    private static final int CONTENT_INDEX = 1;

    /**
     * Callback response received after the bytes are written.
     *
     * @param result the response received.
     * @return the result context.
     */
    private static EventResult writeResponse(EventResult<Integer, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        CallableUnitCallback callback = eventContext.getCallback();
        Throwable error = eventContext.getError();
        if (null != error) {
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, error.getMessage());
            context.setReturnValues(errorStruct);
        }
        callback.notifySuccess();
        return result;
    }

    /**
     * Writes records to a given file.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> channel = (BMap<String, BValue>) context.getRefArgument(RECORD_CHANNEL_INDEX);
        BValueArray content = (BValueArray) context.getRefArgument(CONTENT_INDEX);
        DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel.getNativeData(IOConstants
                .TXT_RECORD_CHANNEL_NAME);
        EventContext eventContext = new EventContext(context, callback);
        DelimitedRecordWriteEvent recordWriteEvent = new DelimitedRecordWriteEvent(delimitedRecordChannel, content,
                eventContext);
        Register register = EventRegister.getFactory().register(recordWriteEvent, WriteTextRecord::writeResponse);
        eventContext.setRegister(register);
        register.submit();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static Object write(Strand strand, ObjectValue channel, ArrayValue content) {
        //TODO : NonBlockingCallback is temporary fix to handle non blocking call
        NonBlockingCallback callback = new NonBlockingCallback(strand);

        DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel.getNativeData(
                IOConstants.TXT_RECORD_CHANNEL_NAME);
        EventContext eventContext = new EventContext(callback);
        DelimitedRecordWriteEvent recordWriteEvent = new DelimitedRecordWriteEvent(delimitedRecordChannel, content,
                                                                                   eventContext, true);
        Register register = EventRegister.getFactory().register(recordWriteEvent, WriteTextRecord::writeTextResponse);
        eventContext.setRegister(register);
        register.submit();
        //TODO : Remove callback once strand non-blocking support is given
        callback.sync();
        return callback.getReturnValue();
    }

    /**
     * Callback response received after the bytes are written.
     *
     * @param result the response received.
     * @return the result context.
     */
    private static EventResult writeTextResponse(EventResult<Integer, EventContext> result) {
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
