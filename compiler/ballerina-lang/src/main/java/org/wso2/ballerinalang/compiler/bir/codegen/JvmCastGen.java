/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.D2F;
import static org.objectweb.asm.Opcodes.D2I;
import static org.objectweb.asm.Opcodes.D2L;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2C;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.I2F;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.L2F;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BHANDLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DOUBLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NUMBER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REF_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SIMPLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CONVERTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.I_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addBoxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmLabelGen.LabelGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadType;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.getSignatureForJType;


// ------------------------------------------------------------------
//                  Generate Check Cast Methods
// ------------------------------------------------------------------

//import ballerina/bir;
//import ballerina/io;
//import ballerina/jvm;

public class JvmCastGen {
    static void generatePlatformCheckCast(MethodVisitor mv, BalToJVMIndexMap indexMap, BType sourceType, BType targetType) {
        if (sourceType.tag == JTypeTags.JTYPE) {
            // If a target type is bir type, then we can guarantee source type is a jvm type, hence the cast
            generateJToBCheckCast(mv, indexMap, (JType) sourceType, targetType);
        } else {
            // else target type is jvm and source type is bir
            generateBToJCheckCast(mv, sourceType, (JType) targetType);
        }
    }

    public static void generateBToJCheckCast(MethodVisitor mv, BType sourceType, JType targetType) {
        if (targetType.tag == JTypeTags.JREF && targetType.type == I_STRING_VALUE) {
            generateCheckCastBToJString(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JBYTE) {
            generateCheckCastBToJByte(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JCHAR) {
            generateCheckCastBToJChar(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JSHORT) {
            generateCheckCastBToJShort(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JINT) {
            generateCheckCastBToJInt(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JLONG) {
            generateCheckCastBToJLong(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JFLOAT) {
            generateCheckCastBToJFloat(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JDOUBLE) {
            generateCheckCastBToJDouble(mv, sourceType);
            return;
        } else if (targetType.tag == JTypeTags.JREF) {
            if (targetType.type == I_STRING_VALUE) {
                generateCheckCastBToJString(mv, sourceType);
            } else {
                generateCheckCastBToJRef(mv, sourceType, targetType);
            }
            return;
        } else if (targetType.tag == JTypeTags.JARRAY) {
            generateCheckCastBToJRef(mv, sourceType, targetType);
            return;
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java %s'", sourceType, targetType));
            throw err;
        }
    }

    static void generateCheckCastBToJString(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.STRING) {
            mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "fromString",
                    String.format("(L%s;)L%s;", STRING_VALUE, I_STRING_VALUE), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java byte'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJByte(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BYTE) {
            // do nothing
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2I);
            mv.visitInsn(I2B);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitInsn(D2I);
            mv.visitInsn(I2B);
        } else if (sourceType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        } else if (sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", String.format("(L%s;)I", OBJECT), false);
            mv.visitInsn(I2B);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java byte'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJChar(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2C);
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2I);
            mv.visitInsn(I2C);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitInsn(D2I);
            mv.visitInsn(I2C);
        } else if (sourceType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        } else if (sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", String.format("(L%s;)J", OBJECT), false);
            mv.visitInsn(L2I);
            mv.visitInsn(I2C);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java char'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJShort(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2S);
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2I);
            mv.visitInsn(I2S);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitInsn(D2I);
            mv.visitInsn(I2S);
        } else if (sourceType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        } else if (sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", String.format("(L%s;)J", OBJECT), false);
            mv.visitInsn(L2I);
            mv.visitInsn(I2S);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java short'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJInt(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BYTE) {
            // do nothing
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2I);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitInsn(D2I);
        } else if (sourceType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        } else if (sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", String.format("(L%s;)J", OBJECT), false);
            mv.visitInsn(L2I);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java int'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJLong(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2L);
        } else if (sourceType.tag == TypeTags.INT) {
            // do nothing
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitInsn(D2L);
        } else if (sourceType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        } else if (sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", String.format("(L%s;)J", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java long'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJFloat(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2F);
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2F);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitInsn(D2F);
        } else if (sourceType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        } else if (sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToFloat", String.format("(L%s;)D", OBJECT), false);
            mv.visitInsn(D2F);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java float'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJDouble(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2D);
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2D);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            // do nothing
        } else if (sourceType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        } else if (sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToFloat", String.format("(L%s;)D", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java double'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastBToJRef(MethodVisitor mv, BType sourceType, JType targetType) {
        if (sourceType.tag == TypeTags.HANDLE) {
            if (targetType.tag == JTypeTags.JREF &&
                    (targetType.type.equals(HANDLE_VALUE) || targetType.type.equals(BHANDLE))) {
                // do nothing
                return;
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
            String sig = getSignatureForJType(targetType);
            mv.visitTypeInsn(CHECKCAST, sig);
        } else if (sourceType.tag == TypeTags.DECIMAL) {
            // do nothing
            return;
        } else {
            if (targetType.tag == JTypeTags.JREF) {
                addBoxInsn(mv, sourceType);
                mv.visitTypeInsn(CHECKCAST, targetType.type);
            } else {
                BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to '%s'", sourceType, targetType));
                throw err;
            }
        }
    }

    static void generateJToBCheckCast(MethodVisitor mv, BalToJVMIndexMap indexMap, JType sourceType, BType targetType) {
        if (targetType.tag == TypeTags.INT) {
            generateCheckCastJToBInt(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.FLOAT) {
            generateCheckCastJToBFloat(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.STRING) {
            generateCheckCastJToBString(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.DECIMAL) {
            generateCheckCastJToBDecimal(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.BOOLEAN) {
            generateCheckCastJToBBoolean(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.BYTE) {
            generateCheckCastJToBByte(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.NIL) {
            // Do nothing
            return;
        } else {
            if (targetType.tag == TypeTags.UNION) {
                generateCheckCastJToBUnionType(mv, indexMap, sourceType, (BUnionType) targetType);
            } else if (targetType.tag == TypeTags.ANYDATA) {
                generateCheckCastJToBAnyData(mv, indexMap, sourceType);
            } else if (targetType.tag == TypeTags.HANDLE) {
                generateJCastToBHandle(mv, sourceType);
            } else if (targetType.tag == TypeTags.ANY) {
                generateJCastToBAny(mv, indexMap, sourceType, targetType);
            } else if (targetType.tag == TypeTags.JSON) {
                generateCheckCastJToBJSON(mv, indexMap, sourceType);
            } else if (targetType.tag == TypeTags.FINITE) {
                generateCheckCastJToBFiniteType(mv, indexMap, sourceType, targetType);
                // TODO fix below properly - rajith
                //} else if (sourceType is bir:BXMLType && targetType is bir:BMapType) {
                //    generateXMLToAttributesMap(mv, sourceType);
                //    return;
                //} else if (targetType is bir:BFiniteType) {
                //    generateCheckCastToFiniteType(mv, sourceType, targetType);
                //    return;
                //} else if (sourceType is bir:BRecordType && (targetType is bir:BMapType && targetType.constraint is bir:BTypeAny)) {
                //    // do nothing
            }

            checkCast(mv, targetType);
            @Nilable String targetTypeClass = getTargetClass(targetType);
            if (targetTypeClass instanceof String) {
                mv.visitTypeInsn(CHECKCAST, targetTypeClass);
            }
        }
    }

    static void generateCheckCastJToBInt(MethodVisitor mv, JType sourceType) {
        if (sourceType.tag == JTypeTags.JBYTE) {
            mv.visitInsn(I2B);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
            mv.visitInsn(I2L);
        } else if (sourceType.tag == JTypeTags.JCHAR) {
            mv.visitInsn(I2L);
        } else if (sourceType.tag == JTypeTags.JSHORT) {
            mv.visitInsn(I2L);
        } else if (sourceType.tag == JTypeTags.JINT) {
            mv.visitInsn(I2L);
        } else if (sourceType.tag == JTypeTags.JLONG) {
            // do nothing
            // According to the spec doc, below two are not needed
            // } else if (sourceType is jvm:JFloat) {
            //     mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "jFloatToBInt", "(F)J", false);
            // } else if (sourceType is jvm:JDouble) {
            //     mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "jDoubleToBInt", "(D)J", false);
        } else if (sourceType.tag == JTypeTags.JREF) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", String.format("(L%s;)J", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'int'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastJToBFloat(MethodVisitor mv, JType sourceType) {
        if (sourceType.tag == JTypeTags.JBYTE) {
            mv.visitInsn(I2B);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
            mv.visitInsn(I2D);
        } else if (sourceType.tag == JTypeTags.JCHAR) {
            mv.visitInsn(I2D);
        } else if (sourceType.tag == JTypeTags.JSHORT) {
            mv.visitInsn(I2D);
        } else if (sourceType.tag == JTypeTags.JINT) {
            mv.visitInsn(I2D);
        } else if (sourceType.tag == JTypeTags.JLONG) {
            mv.visitInsn(L2D);
        } else if (sourceType.tag == JTypeTags.JFLOAT) {
            mv.visitInsn(F2D);
        } else if (sourceType.tag == JTypeTags.JDOUBLE) {
            // do nothing
        } else if (sourceType.tag == JTypeTags.JREF) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", String.format("(L%s;)D", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'float'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastJToBString(MethodVisitor mv, JType sourceType) {
        if (sourceType.tag == JTypeTags.JREF && sourceType.type == I_STRING_VALUE) {
            mv.visitMethodInsn(INVOKEINTERFACE, I_STRING_VALUE, "getValue", String.format("()L%s;", STRING_VALUE), true);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'string'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastJToBDecimal(MethodVisitor mv, JType sourceType) {
        if (sourceType.tag == JTypeTags.JBYTE) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", String.format("(B)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JCHAR) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", String.format("(C)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JSHORT) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", String.format("(S)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JINT) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", String.format("(I)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JLONG) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", String.format("(J)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JFLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", String.format("(F)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JDOUBLE) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", String.format("(D)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JREF) {
            mv.visitTypeInsn(CHECKCAST, DECIMAL_VALUE);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'decimal'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastJToBBoolean(MethodVisitor mv, JType sourceType) {
        if (sourceType.tag == JTypeTags.JBOOLEAN) {
            // do nothing
        } else if (sourceType.tag == JTypeTags.JREF) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToBoolean", String.format("(L%s;)Z", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'boolean'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastJToBByte(MethodVisitor mv, JType sourceType) {
        if (sourceType.tag == JTypeTags.JBYTE) {
            // do nothing
        } else if (sourceType.tag == JTypeTags.JREF) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", String.format("(L%s;)I", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'byte'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastJToBUnionType(MethodVisitor mv, BalToJVMIndexMap indexMap, JType sourceType, BUnionType targetType) {
        generateJCastToBAny(mv, indexMap, sourceType, targetType);
    }

    static void generateCheckCastJToBAnyData(MethodVisitor mv, BalToJVMIndexMap indexMap, JType sourceType) {
        if (!(sourceType.tag == JTypeTags.JREF || sourceType.tag == JTypeTags.JARRAY)) {
            // if value types, then ad box instruction
            generateJCastToBAny(mv, indexMap, sourceType, new BAnyType(TypeTags.ANY, null));
        }
    }

    static void generateJCastToBHandle(MethodVisitor mv, JType sourceType) {
        //  TODO do we need to support below? - rajith
        //if (sourceType is jvm:JByte) {
        //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(B)L%s;", HANDLE_VALUE), false);
        //} else if (sourceType is jvm:JChar) {
        //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(C)L%s;", HANDLE_VALUE), false);
        //} else if (sourceType is jvm:JShort) {
        //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(S)L%s;", HANDLE_VALUE), false);
        //} else if (sourceType is jvm:JInt) {
        //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(I)L%s;", HANDLE_VALUE), false);
        //} else if (sourceType is jvm:JLong) {
        //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(J)L%s;", HANDLE_VALUE), false);
        //} else if (sourceType is jvm:JFloat) {
        //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(F)L%s;", HANDLE_VALUE), false);
        //} else if (sourceType is jvm:JDouble) {
        //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(D)L%s;", HANDLE_VALUE), false);
        //} else if (sourceType is jvm:JRefType || sourceType is jvm:JArrayType) {
        // Here the corresponding Java method parameter type is 'jvm:JRefType'. This has been verified before

        // If the returned value is a HandleValue, do nothing
        LabelGenerator labelGen = new LabelGenerator();
        Label afterHandle = labelGen.getLabel("after_handle");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(INSTANCEOF, BHANDLE);
        mv.visitJumpInsn(IFNE, afterHandle);

        // Otherwise wrap it with a HandleValue
        mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", String.format("(L%s;)L%s;", OBJECT, HANDLE_VALUE), false);
        mv.visitLabel(afterHandle);

        //} else {
        //    error err = error(io:sprintf("Casting is not supported from '%s' to 'int'", sourceType));
        //    panic err;
        //}
    }

    static void generateJCastToBAny(MethodVisitor mv, BalToJVMIndexMap indexMap, JType sourceType, BType targetType) {
        if (sourceType.tag == JTypeTags.JBOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", String.format("(Z)L%s;", BOOLEAN_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JBYTE) {
            mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "valueOf", String.format("(I)L%s;", INT_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JCHAR) {
            mv.visitInsn(I2L);
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", String.format("(J)L%s;", LONG_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JSHORT) {
            mv.visitInsn(I2L);
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", String.format("(J)L%s;", LONG_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JINT) {
            mv.visitInsn(I2L);
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", String.format("(J)L%s;", LONG_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JLONG) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", String.format("(J)L%s;", LONG_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JFLOAT) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", String.format("(D)L%s;", DOUBLE_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JDOUBLE) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", String.format("(D)L%s;", DOUBLE_VALUE), false);
        } else if (sourceType.tag == JTypeTags.JREF) {
            Label afterHandle = new Label();
            if (sourceType.type == OBJECT) {
                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, ERROR_VALUE);
                mv.visitJumpInsn(IFNE, afterHandle);

                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, NUMBER);
                mv.visitJumpInsn(IFNE, afterHandle);

                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, BOOLEAN_VALUE);
                mv.visitJumpInsn(IFNE, afterHandle);

                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, SIMPLE_VALUE);
                mv.visitJumpInsn(IFNE, afterHandle);
            }

            if (isNillable(targetType)) {
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNULL, afterHandle);
            }

            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, REF_VALUE);
            mv.visitJumpInsn(IFNE, afterHandle);

            BIRVariableDcl retJObjectVarDcl = new BIRVariableDcl(null, new BAnyType(TypeTags.ANY, null),
                    new Name("$_ret_jobject_val_$"), VarScope.FUNCTION, VarKind.LOCAL, "");
            int returnJObjectVarRefIndex = indexMap.getIndex(retJObjectVarDcl);
            mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
            mv.visitTypeInsn(NEW, HANDLE_VALUE);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
            mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, "<init>", "(Ljava/lang/Object;)V", false);
            mv.visitLabel(afterHandle);
        } else if (sourceType.tag == JTypeTags.JARRAY) {
            // do nothing
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'any'", sourceType));
            throw err;
        }
    }

    static boolean isNillable(BType targetType) {
        if (targetType.tag == TypeTags.NIL ||
                targetType.tag == TypeTags.JSON ||
                targetType.tag == TypeTags.ANY ||
                targetType.tag == TypeTags.ANYDATA) {
            return true;
        } else if (targetType.tag == TypeTags.UNION) {
            return targetType.isNullable();
        } else if (targetType.tag == TypeTags.FINITE) {
            return targetType.isNullable();
        }

        return false;
    }

    static void generateCheckCastJToBJSON(MethodVisitor mv, BalToJVMIndexMap indexMap, JType sourceType) {
        if (sourceType.tag == JTypeTags.JREF || sourceType.tag == JTypeTags.JARRAY) {
            // TODO fix properly - rajith
            //checkCast(mv, bir:TYPE_JSON);
        } else {
            // if value types, then ad box instruction
            generateJCastToBAny(mv, indexMap, sourceType, new BJSONType(TypeTags.JSON, null));
        }
    }

    static void generateCheckCastJToBFiniteType(MethodVisitor mv, BalToJVMIndexMap indexMap, JType sourceType,
                                                BType targetType) {
        // Finite types are stored in ref registry at ballerina side. Therefore if the return
        // type if a primitive, then add a box instruction.
        if (!(sourceType.tag == JTypeTags.JREF || sourceType.tag == JTypeTags.JARRAY)) {
            generateJCastToBAny(mv, indexMap, sourceType, targetType);
        }
    }

    static void generateCheckCast(MethodVisitor mv, BType sourceType, BType targetType) {
        if (targetType.tag == TypeTags.INT) {
            generateCheckCastToInt(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.FLOAT) {
            generateCheckCastToFloat(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.STRING) {
            generateCheckCastToString(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.DECIMAL) {
            generateCheckCastToDecimal(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.BOOLEAN) {
            generateCheckCastToBoolean(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.BYTE) {
            generateCheckCastToByte(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.NIL) {
            checkCast(mv, targetType);
            return;
        } else if (targetType.tag == TypeTags.UNION) {
            generateCheckCastToUnionType(mv, sourceType, (BUnionType) targetType);
            return;
        } else if (targetType.tag == TypeTags.ANYDATA) {
            generateCheckCastToAnyData(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.ANY) {
            generateCastToAny(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.JSON) {
            generateCheckCastToJSON(mv, sourceType);
            return;
        } else if (sourceType.tag == TypeTags.XML && targetType.tag == TypeTags.MAP) {
            generateXMLToAttributesMap(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.FINITE) {
            generateCheckCastToFiniteType(mv, sourceType, (BFiniteType) targetType);
            return;
        } else if (sourceType.tag == TypeTags.RECORD && (targetType.tag == TypeTags.MAP
                && ((BMapType) targetType).constraint.tag == TypeTags.ANY)) {
            // do nothing
        } else {
            // do the ballerina checkcast
            checkCast(mv, targetType);
        }

        // cast to the specific java class
        @Nilable String targetTypeClass = getTargetClass(targetType);
        if (targetTypeClass instanceof String) {
            mv.visitTypeInsn(CHECKCAST, targetTypeClass);
        }
    }

    static void generateCheckCastToInt(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.INT) {
            // do nothing
        } else if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2B);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
            mv.visitInsn(I2L);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToInt", "(D)J", false);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.DECIMAL ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", String.format("(L%s;)J", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'int'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastToFloat(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.FLOAT) {
            // do nothing
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2D);
        } else if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2D);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.DECIMAL ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToFloat", String.format("(L%s;)D", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'float'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastToDecimal(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.DECIMAL) {
            // do nothing
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToDecimal",
                    String.format("(L%s;)L%s;", OBJECT, DECIMAL_VALUE), false);
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", String.format("(J)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", String.format("(D)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == TypeTags.BYTE) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", String.format("(I)L%s;", DECIMAL_VALUE), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'decimal'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastToString(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.STRING) {
            // do nothing
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.FINITE) {
            checkCast(mv, new BType(TypeTags.STRING, null));
            mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "toString", String.format("(J)L%s;", STRING_VALUE), false);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "toString", String.format("(D)L%s;", STRING_VALUE), false);
        } else if (sourceType.tag == TypeTags.BOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "toString", String.format("(Z)L%s;", STRING_VALUE), false);
        } else if (sourceType.tag == TypeTags.DECIMAL) {
            mv.visitMethodInsn(INVOKESTATIC, STRING_VALUE, "valueOf", String.format("(L%s;)L%s;", OBJECT, STRING_VALUE),
                    false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'string'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastToBoolean(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BOOLEAN) {
            // do nothing
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToBoolean", String.format("(L%s;)Z", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'boolean'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastToByte(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.INT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToByte", "(J)I", false);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToByte", "(D)I", false);
        } else if (sourceType.tag == TypeTags.DECIMAL) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", String.format("(L%s;)I", OBJECT), false);
        } else if (sourceType.tag == TypeTags.BYTE) {
            // do nothing
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.FINITE ||
                sourceType.tag == TypeTags.JSON) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", String.format("(L%s;)I", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'byte'", sourceType));
            throw err;
        }
    }

    static void generateCheckCastToAnyData(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.ANY || sourceType.tag == TypeTags.UNION) {
            checkCast(mv, new BAnyType(TypeTags.ANY, null));
        } else {
            // if value types, then ad box instruction
            generateCastToAny(mv, sourceType);
        }
    }

    static void generateCheckCastToJSON(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.MAP) {
            checkCast(mv, new BAnyType(TypeTags.ANY, null));
        } else {
            // if value types, then ad box instruction
            generateCastToAny(mv, sourceType);
        }
    }

    static void generateCheckCastToUnionType(MethodVisitor mv, BType sourceType, BUnionType targetType) {
        generateCastToAny(mv, sourceType);
        checkCast(mv, targetType);
    }

    static void checkCast(MethodVisitor mv, BType targetType) {
        loadType(mv, targetType);
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkCast",
                String.format("(L%s;L%s;)L%s;", OBJECT, BTYPE, OBJECT), false);
    }

    static @Nilable
    String getTargetClass(BType targetType) {
        String targetTypeClass;
        if (targetType.tag == TypeTags.ARRAY || targetType.tag == TypeTags.TUPLE) {
            targetTypeClass = ARRAY_VALUE;
        } else if (targetType.tag == TypeTags.MAP) {
            targetTypeClass = MAP_VALUE;
        } else if (targetType.tag == TypeTags.RECORD) {
            targetTypeClass = MAP_VALUE;
        } else if (targetType.tag == TypeTags.TABLE) {
            targetTypeClass = TABLE_VALUE;
        } else if (targetType.tag == TypeTags.OBJECT || targetType.tag == TypeTags.SERVICE) {
            targetTypeClass = OBJECT_VALUE;
        } else if (targetType.tag == TypeTags.ERROR) {
            targetTypeClass = ERROR_VALUE;
        } else if (targetType.tag == TypeTags.XML) {
            targetTypeClass = XML_VALUE;
        } else if (targetType.tag == TypeTags.TYPEDESC) {
            targetTypeClass = TYPEDESC_VALUE;
        } else if (targetType.tag == TypeTags.INVOKABLE) {
            targetTypeClass = FUNCTION_POINTER;
        } else if (targetType.tag == TypeTags.FUTURE) {
            targetTypeClass = FUTURE_VALUE;
        } else if (targetType.tag == TypeTags.HANDLE) {
            targetTypeClass = HANDLE_VALUE;
        } else {
            return null;
        }

        return targetTypeClass;
    }

    static void generateCheckCastToFiniteType(MethodVisitor mv, BType sourceType, BFiniteType targetType) {
        generateCastToAny(mv, sourceType);
        checkCast(mv, targetType);
    }


    // ------------------------------------------------------------------
    //   Generate Cast Methods - Performs cast without type checking
    // ------------------------------------------------------------------

    static void generateCast(MethodVisitor mv, BType sourceType, BType targetType, boolean useBString /* = false */) {
        if (targetType.tag == TypeTags.INT) {
            generateCastToInt(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.FLOAT) {
            generateCastToFloat(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.STRING) {
            generateCastToString(mv, sourceType, useBString);
            return;
        } else if (targetType.tag == TypeTags.BOOLEAN) {
            generateCastToBoolean(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.BYTE) {
            generateCastToByte(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.DECIMAL) {
            generateCastToDecimal(mv, sourceType);
            return;
        } else if (targetType.tag == TypeTags.NIL) {
            // do nothing
            return;
        } else if (targetType.tag == TypeTags.UNION ||
                targetType.tag == TypeTags.ANYDATA ||
                targetType.tag == TypeTags.ANY ||
                targetType.tag == TypeTags.JSON ||
                targetType.tag == TypeTags.FINITE) {
            generateCastToAny(mv, sourceType);
            return;
        }

        // cast to the specific java class
        @Nilable String targetTypeClass = getTargetClass(targetType);
        if (targetTypeClass instanceof String) {
            mv.visitTypeInsn(CHECKCAST, targetTypeClass);
        }
    }

    static void generateCastToInt(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.INT) {
            // do nothing
        } else if (sourceType.tag == TypeTags.BYTE) {
            mv.visitInsn(I2L);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitInsn(D2L);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", String.format("(L%s;)J", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'int'", sourceType));
            throw err;
        }
    }

    static void generateCastToFloat(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.FLOAT) {
            // do nothing
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2D);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToFloat", String.format("(L%s;)D", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'float'", sourceType));
            throw err;
        }
    }

    static void generateCastToString(MethodVisitor mv, BType sourceType, boolean useBString /* = false */) {
        if (sourceType.tag == TypeTags.STRING) {
            // do nothing
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "toString", String.format("(J)L%s;", STRING_VALUE), false);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "toString", String.format("(D)L%s;", STRING_VALUE), false);
        } else if (sourceType.tag == TypeTags.BOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "toString", String.format("(Z)L%s;", STRING_VALUE), false);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON) {
            if (useBString) {
                mv.visitTypeInsn(CHECKCAST, I_STRING_VALUE);
            } else {
                mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
            }
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'string'", sourceType));
            throw err;
        }
    }

    static void generateCastToDecimal(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.DECIMAL) {
            // do nothing
        } else if (sourceType.tag == TypeTags.INT) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", String.format("(J)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", String.format("(D)L%s;", DECIMAL_VALUE), false);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToDecimal",
                    String.format("(L%s;)L%s;", OBJECT, DECIMAL_VALUE), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'decimal'", sourceType));
            throw err;
        }
    }

    static void generateCastToBoolean(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.BOOLEAN) {
            // do nothing
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON) {
            mv.visitTypeInsn(CHECKCAST, BOOLEAN_VALUE);
            mv.visitMethodInsn(INVOKEVIRTUAL, BOOLEAN_VALUE, "booleanValue", "()Z", false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'boolean'", sourceType));
            throw err;
        }
    }

    static void generateCastToByte(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.INT) {
            mv.visitInsn(L2I);
        } else if (sourceType.tag == TypeTags.BYTE) {
            // do nothing
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", String.format("(L%s;)I", OBJECT), false);
        } else {
            BLangCompilerException err = new BLangCompilerException(String.format("Casting is not supported from '%s' to 'byte'", sourceType));
            throw err;
        }
    }

    static void generateCastToAny(MethodVisitor mv, BType sourceType) {
        if (sourceType.tag == TypeTags.INT) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", String.format("(J)L%s;", LONG_VALUE), false);
        } else if (sourceType.tag == TypeTags.BYTE) {
            mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "valueOf", String.format("(I)L%s;", INT_VALUE), false);
        } else if (sourceType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", String.format("(D)L%s;", DOUBLE_VALUE), false);
        } else if (sourceType.tag == TypeTags.BOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", String.format("(Z)L%s;", BOOLEAN_VALUE), false);
        } else {
            // do nothing
            return;
        }
    }

    static void generateXMLToAttributesMap(MethodVisitor mv, BType sourceType) {
        mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getAttributesMap",
                String.format("()L%s;", MAP_VALUE), false);
    }
}
