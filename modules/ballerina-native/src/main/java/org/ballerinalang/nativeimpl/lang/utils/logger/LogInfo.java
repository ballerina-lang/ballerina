/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.lang.utils.logger;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.slf4j.Logger;

/**
 * Native function ballerina.utils.logger:info
 */

@BallerinaFunction(
        packageName = "ballerina.utils.logger",
        functionName = "info",
        args = {@Argument(name = "msg", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
                                                                              value = "Logs a message marked with" +
                                                                                      " the priority level 'info'")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "msg",
                                                                        value = "String value to be logged")})
public class LogInfo extends AbstractNativeFunction {
    private Logger logger;

    public BValue[] execute(Context ctx) {
        logger = BallerinaLoggerHandler.getLogger(ctx);
        logger.info(getArgument(ctx, 0).stringValue());

        return VOID_RETURN;
    }
}
