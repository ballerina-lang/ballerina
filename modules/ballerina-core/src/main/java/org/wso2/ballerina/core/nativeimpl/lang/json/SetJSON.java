/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.wso2.ballerina.core.nativeimpl.lang.json;

import com.google.gson.JsonElement;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.WriteContext;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.lang.utils.ErrorHandler;

/**
 * Set the JSON value of a element that matches the given jsonpath.
 * If the jsonpath doesn't match any element, then this operation would have no effect.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.json",
        functionName = "set",
        args = {@Argument(name = "json", type = TypeEnum.JSON),
                @Argument(name = "jsonPath", type = TypeEnum.STRING),
                @Argument(name = "value", type = TypeEnum.JSON)},
        isPublic = true
)
@Component(
        name = "func.lang.json_setJson",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class SetJSON extends AbstractJSONFunction {

    private static final String OPERATION = "set element in json";
    
    @Override
    public BValue<?>[] execute(Context ctx) {
        String jsonPath = null;
        try {
            // Accessing Parameters.
            JSONValue json = (JSONValue) getArgument(ctx, 0).getBValue();
            jsonPath = getArgument(ctx, 1).getString();
            JsonElement value = getArgument(ctx, 2).getJSON();

            // Setting the value to JSON
            WriteContext jsonCtx = JsonPath.parse(json.getValue());
            jsonCtx.set(jsonPath, value);
        } catch (PathNotFoundException e) {
            ErrorHandler.handleNonExistingJsonpPath(OPERATION, jsonPath, e);
        } catch (InvalidPathException e) {
            ErrorHandler.handleInvalidJsonPath(OPERATION, e);
        } catch (JsonPathException e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        }
        
        return VOID_RETURN;
    }
}
