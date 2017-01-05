/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 **/

package org.wso2.ballerina.core.nativeimpl.lang.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.core.nativeimpl.lang.utils.ErrorHandler;

/**
 * Evaluate jsonpath on a JSON object and returns the integer value.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.json",
        functionName = "getInt",
        args = {@Argument(name = "json", type = TypeEnum.JSON),
                @Argument(name = "jsonPath", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
public class GetInt extends AbstractJSONFunction {

    private static final String OPERATION = "get integer from json";

    @Override
    public BValue[] execute(Context ctx) {
        String jsonPath = null;
        BValue result = null;
        try {
            // Accessing Parameters.
            BJSON json = (BJSON) getArgument(ctx, 0);
            jsonPath = getArgument(ctx, 1).stringValue();

            // Getting the value from JSON
            ReadContext jsonCtx = JsonPath.parse(json.value());
            Object elementObj = jsonCtx.read(jsonPath);
            if (elementObj == null) {
                throw new BallerinaException("No matching element found for jsonpath: " + jsonPath);
            } else if (elementObj instanceof JsonElement) {

                JsonElement element = (JsonElement) elementObj;

                if (element.isJsonPrimitive()) {
                    // if the resulting value is a primitive, return the respective primitive value object
                    JsonPrimitive value = element.getAsJsonPrimitive();
                    if (value.isNumber()) {
                        Number number = value.getAsNumber();
                    if (number instanceof Integer | number instanceof Long | number instanceof Short) {
                            result = new BInteger(number.intValue());
                        } else {
                            throw new BallerinaException(
                                    "The element matching path: " + jsonPath + " is not an Integer.");
                        }
                    } else {
                        throw new BallerinaException("The element matching path: " + jsonPath + " is not an Integer.");
                    }
                } else {
                    throw new BallerinaException(
                            "The element matching path: " + jsonPath + " is a JSON, not an Integer.");
                }
            } else if (elementObj instanceof Integer) {
                // this handles the JsonPath's length() function
                result = new BInteger((Integer) elementObj);
            }
        } catch (PathNotFoundException e) {
            ErrorHandler.handleNonExistingJsonpPath(OPERATION, jsonPath, e);
        } catch (InvalidPathException e) {
            ErrorHandler.handleInvalidJsonPath(OPERATION, e);
        } catch (JsonPathException e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        }

        // Setting output value.
        return getBValues(result);
    }
}
