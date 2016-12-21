/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.core.nativeimpl.lang.message;


import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.ArrayValueOld;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Get the Headers of the Message
 */
@BallerinaFunction(
        packageName = "ballerina.lang.message",
        functionName = "getHeaders",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE),
                @Argument(name = "headerName", type = TypeEnum.STRING)},
        returnType = {TypeEnum.ARRAY},
        isPublic = true
)

@Component(
        name = "func.lang.message_getHeaders",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class GetHeaders extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        MessageValue msg = (MessageValue) getArgument(ctx, 0).getBValue();
        String headerName = ((StringValue) getArgument(ctx, 1).getBValue()).getValue();
        String[] headerValue = msg.getHeaders(headerName);
        ArrayValueOld<StringValue> headers = new ArrayValueOld<>(headerValue.length);
        int i = 0;
        for (String header : headerValue) {
            headers.insert(i++, new StringValue(header));
        }
        return getBValues(headers);
    }
}
