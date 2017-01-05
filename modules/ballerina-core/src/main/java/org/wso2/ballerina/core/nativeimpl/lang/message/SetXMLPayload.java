/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.nativeimpl.lang.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.lang.utils.Constants;

/**
 * Set the payload of the Message as a XML
 */
@BallerinaFunction(
        packageName = "ballerina.lang.message",
        functionName = "setXmlPayload",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE),
                @Argument(name = "payload", type = TypeEnum.XML)},
        isPublic = true
)
public class SetXMLPayload extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(SetXMLPayload.class);

    @Override
    public BValue[] execute(Context context) {
        // Accessing First Parameter Value.
        BMessage msg = (BMessage) getArgument(context, 0);
        BXML payload = (BXML) getArgument(context, 1);

        // Setting the payload
        msg.setBuiltPayload(payload);
        msg.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_XML);
        return VOID_RETURN;
    }
}
