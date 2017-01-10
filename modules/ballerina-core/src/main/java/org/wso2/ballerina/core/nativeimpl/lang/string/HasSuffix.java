/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.nativeimpl.lang.string;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native function ballerina.lang.string:hasSuffix.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.string",
        functionName = "hasSuffix",
        args = {@Argument(name = "string", type = TypeEnum.STRING),
                @Argument(name = "suffix", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)},
        isPublic = true
)
public class HasSuffix extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String param1 = getArgument(context, 0).stringValue();
        String suffix = getArgument(context, 1).stringValue();

        BBoolean booleanValue = new BBoolean(param1.endsWith(suffix));
        return getBValues(booleanValue);
    }
}
