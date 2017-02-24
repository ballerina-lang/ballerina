/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.nativeimpl.lang.exceptions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native functions for ballerina.model.exceptions to Get the message of an exception.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.exceptions",
        functionName = "setMessage",
        args = {@Argument(name = "e", type = TypeEnum.EXCEPTION),
                @Argument(name = "m", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets exception message specified exception") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "e",
        value = "The exception object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "message",
        value = "The exception message to be added") })
public class SetMessage extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BException exception = (BException) getArgument(context, 0);
        String message = getArgument(context, 1).stringValue();
        exception.value().setMessage(message);
        return VOID_RETURN;
    }
}
