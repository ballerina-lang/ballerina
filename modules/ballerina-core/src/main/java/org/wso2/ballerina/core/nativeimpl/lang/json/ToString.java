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

package org.wso2.ballerina.core.nativeimpl.lang.json;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.lang.utils.ErrorHandler;

/**
 * Native function ballerina.lang.json:toString
 */
@BallerinaFunction(
        packageName = "ballerina.lang.json",
        functionName = "toString",
        args = {@Argument(name = "json", type = TypeEnum.JSON)},
        returnType = {TypeEnum.STRING},
        isPublic = true
)
@Component(
        name = "func.lang.json_toString",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class ToString extends AbstractJSONFunction {

    private static final Logger log = LoggerFactory.getLogger(ToString.class);

    @Override
    public BValue<?>[] execute(Context ctx) {
        StringValue jsonStr = null;
        try {
            // Accessing Parameters.
            JSONValue json = (JSONValue) getArgument(ctx, 0).getBValue();
            
            jsonStr = json.getString();
            if (log.isDebugEnabled()) {
                log.debug("Output JSON: " + jsonStr.getValue());
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("convert json to string", e);
        }
        
        return getBValues(jsonStr);
    }
}
