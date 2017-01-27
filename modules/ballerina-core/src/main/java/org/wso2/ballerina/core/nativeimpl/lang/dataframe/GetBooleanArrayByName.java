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

package org.wso2.ballerina.core.nativeimpl.lang.dataframe;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDataframe;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native function to get boolean array value of a given column name.
 * ballerina.lang.dataframe:getBooleanArray(dataframe, int)
 */
@BallerinaFunction(
        packageName = "ballerina.lang.dataframe",
        functionName = "getBooleanArray",
        args = {@Argument(name = "dataframe", type = TypeEnum.DATAFRAME),
                @Argument(name = "name", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.BOOLEAN)},
        isPublic = true
)
public class GetBooleanArrayByName extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BDataframe dataframe = (BDataframe) getArgument(ctx, 0);
        String columnName = (getArgument(ctx, 1)).stringValue();
        BArray<BBoolean> array = new BArray<>(BBoolean.class);
        boolean[] booleanArray = dataframe.getBooleanArray(columnName);
        for (int i = 0; i < booleanArray.length; i++) {
            array.add(i, new BBoolean(booleanArray[i]));
        }
        return getBValues(array);
    }
}
