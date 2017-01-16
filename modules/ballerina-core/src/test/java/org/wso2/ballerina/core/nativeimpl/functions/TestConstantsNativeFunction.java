/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConstant;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogUtil;

/**
 * Native function ballerina.lang.system:Log
 */
@BallerinaFunction(
        packageName = "ballerina.test.constant",
        functionName = "testConstant",
        args = {@Argument(name = "logLevel", type = TypeEnum.INT),
                @Argument(name = "string", type = TypeEnum.STRING)},
        isPublic = true,
        consts = {
                @BallerinaConstant(identifier = "LOG_LEVEL_TRACE", type = TypeEnum.INT, value = "1",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_DEBUG", type = TypeEnum.LONG, value = "2",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_INFO", type = TypeEnum.FLOAT, value = "3",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_WARN", type = TypeEnum.DOUBLE, value = "4",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_ERROR", type = TypeEnum.BOOLEAN, value = "5",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_ERROR", type = TypeEnum.STRING, value = "6",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_ERROR", type = TypeEnum.JSON, value = "7",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_ERROR", type = TypeEnum.XML, value = "8",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_ERROR", type = TypeEnum.INT, value = "hello",
                        argumentRefs = {"logLevel"})
        }
)
public class TestConstantsNativeFunction extends AbstractNativeFunction {

    private static final Logger logger = LoggerFactory.getLogger(TestConstantsNativeFunction.class);

    public BValue[] execute(Context ctx) {
        // TODO : Improve this with trace log.
        LogUtil.log(logger, ((BInteger) getArgument(ctx, 0)).intValue(), getArgument(ctx, 1).stringValue());
        return VOID_RETURN;
    }
}
