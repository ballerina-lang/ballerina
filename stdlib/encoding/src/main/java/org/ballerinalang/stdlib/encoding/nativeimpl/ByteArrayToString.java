/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.stdlib.encoding.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Convert byte array to string.
 *
 * @since 0.980
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "encoding", functionName = "byteArrayToString",
        args = {
                @Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "encoding", type = TypeKind.STRING)
        },
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ByteArrayToString extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        byte[] bytes = ((BValueArray) context.getRefArgument(0)).getBytes();
        String encoding = context.getStringArgument(0);
        try {
            String value = new String(bytes, encoding);
            context.setReturnValues(new BString(value));
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("unsupported encoding: " + encoding , e);
        }
    }

    public static String byteArrayToString(Strand strand, ArrayValue bytes, String encoding) {
        try {
            // TODO : Remove null check once extern functions are supported with default value parameters.
            if (encoding == null) {
                encoding = StandardCharsets.UTF_8.name();;
            }
            return new String(bytes.getBytes(), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaException("unsupported encoding: " + encoding , e);
        }
    }
}
