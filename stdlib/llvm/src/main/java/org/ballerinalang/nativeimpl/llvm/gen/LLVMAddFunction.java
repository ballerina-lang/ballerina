// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMAddFunction",
        args = {
                @Argument(name = "m", type = RECORD, structType = "LLVMModuleRef"),
                @Argument(name = "name", type = STRING),
                @Argument(name = "functionTy", type = RECORD, structType = "LLVMTypeRef"),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMValueRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMAddFunction {

    public static Object llvmAddFunction(Strand strand, MapValue<String, Object> m, String name, MapValue<String, Object> functionTy) {
        LLVM.LLVMModuleRef mRef = (LLVM.LLVMModuleRef) FFIUtil.getRecodeArgumentNative(m);
        LLVM.LLVMTypeRef functionTyRef = (LLVM.LLVMTypeRef) FFIUtil.getRecodeArgumentNative(functionTy);
        LLVMValueRef returnValue =  LLVM.LLVMAddFunction(mRef, name, functionTyRef);
        MapValue<String, Object> returnWrappedRecord = FFIUtil.newRecord(new BPackage("ballerina",
                "llvm"), "LLVMValueRef");
        FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);
        return returnWrappedRecord;
    }
}
