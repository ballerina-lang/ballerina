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

package org.wso2.ballerina.core.nativeimpl.lang.string;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.core.nativeimpl.lang.utils.ErrorHandler;

/**
 * Native function ballerina.lang.string:valueOf.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.string",
        functionName = "valueOf",
        args = {@Argument(name = "json", type = TypeEnum.JSON)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class JsonValueOf extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BJSON json = (BJSON) getArgument(context, 0);
        BString jsonStr = null;
        try {
            jsonStr = new BString(json.stringValue());
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("get json as string", e);
        }
        return getBValues(jsonStr);
    }
}
