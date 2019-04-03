/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.writer;

import io.netty.buffer.ByteBuf;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLAttributesType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

import java.util.LinkedList;

/**
 * Writes bType to a Byte Buffer in binary format.
 * A ConstPool is used to store string typed information.
 * 
 * @since 0.995.0
 */
public class BIRTypeWriter implements TypeVisitor {
    public static final int TYPE_TAG_SELF = 50;
    private final ByteBuf buff;

    private final ConstantPool cp;
    private LinkedList<Object> compositeStack = new LinkedList<>();

    public BIRTypeWriter(ByteBuf buff, ConstantPool cp) {
        this.buff = buff;
        this.cp = cp;
    }

    @Override
    public void visit(BAnnotationType bAnnotationType) {
        throwUnimplementedError(bAnnotationType);
    }

    @Override
    public void visit(BArrayType bArrayType) {
        buff.writeByte(bArrayType.tag);
        buff.writeByte(bArrayType.state.getValue());
        bArrayType.getElementType().accept(this);
    }

    @Override
    public void visit(BBuiltInRefType bBuiltInRefType) {
        throwUnimplementedError(bBuiltInRefType);
    }

    @Override
    public void visit(BAnyType bAnyType) {
        buff.writeByte(bAnyType.tag);
    }

    @Override
    public void visit(BErrorType bErrorType) {
        buff.writeByte(bErrorType.tag);
        BTypeSymbol tsymbol = bErrorType.tsymbol;
        compositeStack.push(tsymbol);
        bErrorType.reasonType.accept(this);
        bErrorType.detailType.accept(this);
        Object popped = compositeStack.pop();
        assert popped == tsymbol;
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        throwUnimplementedError(bFiniteType);
    }

    @Override
    public void visit(BIntermediateCollectionType bIntermediateCollectionType) {
        throwUnimplementedError(bIntermediateCollectionType);
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        buff.writeByte(bInvokableType.tag);
        buff.writeInt(bInvokableType.paramTypes.size());
        for (BType params : bInvokableType.paramTypes) {
            params.accept(this);
        }
        bInvokableType.retType.accept(this);
    }

    @Override
    public void visit(BJSONType bjsonType) {
        buff.writeByte(bjsonType.tag);
    }

    @Override
    public void visit(BMapType bMapType) {
        buff.writeByte(bMapType.tag);
        bMapType.constraint.accept(this);
    }

    @Override
    public void visit(BFutureType bFutureType) {
        buff.writeByte(bFutureType.tag);
        bFutureType.constraint.accept(this);
    }

    @Override
    public void visit(BNilType bNilType) {
        buff.writeByte(bNilType.tag);
    }

    @Override
    public void visit(BNoType bNoType) {
        buff.writeByte(bNoType.tag);
    }

    @Override
    public void visit(BAnydataType bAnydataType) {
        buff.writeByte(bAnydataType.tag);
    }

    @Override
    public void visit(BPackageType bPackageType) {
        throwUnimplementedError(bPackageType);
    }

    @Override
    public void visit(BServiceType bServiceType) {
        throwUnimplementedError(bServiceType);
    }

    @Override
    public void visit(BStructureType bStructureType) {

        throwUnimplementedError(bStructureType);
    }

    @Override
    public void visit(BTupleType bTupleType) {
        buff.writeByte(bTupleType.tag);
        buff.writeInt(bTupleType.tupleTypes.size());
        for (BType memberType : bTupleType.tupleTypes) {
            memberType.accept(this);
        }
    }

    @Override
    public void visit(BUnionType bUnionType) {
        buff.writeByte(bUnionType.tag);
        buff.writeInt(bUnionType.getMemberTypes().size());
        for (BType memberType : bUnionType.getMemberTypes()) {
            if (!writeSelfRef(memberType.tsymbol)) {
                memberType.accept(this);
            }
        }
    }

    @Override
    public void visit(BRecordType bRecordType) {
        buff.writeByte(bRecordType.tag);
        buff.writeInt(addStringCPEntry(bRecordType.tsymbol.name.value));
        buff.writeBoolean(bRecordType.sealed);
        bRecordType.restFieldType.accept(this);
        buff.writeInt(bRecordType.fields.size());
        for (BField field : bRecordType.fields) {
            // TODO add position
            buff.writeInt(addStringCPEntry(field.name.value));
            field.type.accept(this);
        }
    }

    @Override
    public void visit(BObjectType bObjectType) {
        BObjectTypeSymbol tsymbol = (BObjectTypeSymbol) bObjectType.tsymbol;
        if (writeSelfRef(bObjectType.tsymbol)) {
            return;
        }
        compositeStack.push(tsymbol);

        buff.writeByte(bObjectType.tag);
        buff.writeInt(addStringCPEntry(tsymbol.name.value));
        buff.writeInt(bObjectType.fields.size());
        for (BField field : bObjectType.fields) {
            buff.writeInt(addStringCPEntry(field.name.value));
            // TODO add position
            buff.writeByte(getVisibility(field.symbol).value());
            field.type.accept(this);
        }
        Object popped = compositeStack.pop();
        assert popped == tsymbol;
        buff.writeInt(tsymbol.attachedFuncs.size());
        for (BAttachedFunction attachedFunc : tsymbol.attachedFuncs) {
            buff.writeInt(addStringCPEntry(attachedFunc.funcName.value));
            buff.writeByte(getVisibility(attachedFunc.symbol).value());
            attachedFunc.type.accept(this);
        }
    }

    private boolean writeSelfRef(BTypeSymbol tsymbol) {
        int i = compositeStack.indexOf(tsymbol);
        if (i >= 0) {
            buff.writeByte(TYPE_TAG_SELF);
            buff.writeInt(i);
            return true;
        }
        return false;
    }

    @Override
    public void visit(BType bType) {
        buff.writeByte(bType.tag);
    }

    @Override
    public void visit(BXMLAttributesType bxmlAttributesType) {
        throwUnimplementedError(bxmlAttributesType);
    }

    @Override
    public void visit(BXMLType bxmlType) {
        throwUnimplementedError(bxmlType);
    }

    private void throwUnimplementedError(BType bType) {
        throw new AssertionError("Type serialization is not implemented for " + bType.getClass());
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new StringCPEntry(value));
    }

    private Visibility getVisibility(BSymbol symbol) {
        if (Symbols.isPublic(symbol)) {
            return Visibility.PUBLIC;
        } else if (Symbols.isPrivate(symbol)) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.PACKAGE_PRIVATE;
        }
    }
}
