/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors.http.function;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.carbon.messaging.Headers;

/**
 * Convert the message into an HTTP Response
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "convertToResponse",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE)},
        isPublic = true
)

@Component(
        name = "func.net.http_convertToResponse",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class ConvertToResponse extends AbstractNativeFunction {
    public BValue[] execute(Context ctx) {
        if (!org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE.
                equals(ctx.getCarbonMessage().getProperty(org.wso2.carbon.messaging.Constants.DIRECTION))) {
            ctx.getCarbonMessage().getHeaders().clear();
            // Set any intermediate headers set during ballerina execution
            if (ctx.getCarbonMessage().getProperty(Constants.INTERMEDIATE_HEADERS) != null) {
                Headers headers = (Headers) ctx.getCarbonMessage().getProperty(Constants.INTERMEDIATE_HEADERS);
                ctx.getCarbonMessage().setHeaders(headers.getAll());
            }
        }
        return VOID_RETURN;
    }
}
