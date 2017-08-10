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
package org.ballerinalang.nativeimpl.lang.files;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Writes a line to a file using the given input stream.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "writeln",
        args = {@Argument(name = "blob", type = TypeEnum.BLOB),
                @Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files")},
        isPublic = true
)
public class Writeln extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        byte[] content = getBlobArgument(context, 0);
//        byte[] newLine = System.getProperty("line.separator").getBytes(Charset.defaultCharset());
        byte[] newLine = System.lineSeparator().getBytes(Charset.defaultCharset());
        BStruct destination = (BStruct) getRefArgument(context, 0);
        try {
            OutputStream outputStream = (BufferedOutputStream) destination.getNativeData("outStream");
            if (outputStream == null) {
                throw new BallerinaException("file is not opened in write or append mode:"
                        + destination.getStringField(0));
            }
            outputStream.write(content);
            outputStream.write(newLine);
            outputStream.flush();

        } catch (Throwable e) {
            throw new BallerinaException("failed to write to file: " + e.getMessage(), e);
        }
        return VOID_RETURN;
    }
}
