/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStringSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @since 0.94
 */
public class SymbolTable {

    private static final CompilerContext.Key<SymbolTable> SYM_TABLE_KEY =
            new CompilerContext.Key<>();

    public static final PackageID TRANSACTION = new PackageID(Names.BUILTIN_ORG, Names.TRANSACTION_PACKAGE, 
                                                              Names.EMPTY);
    
    public static final Integer BBYTE_MIN_VALUE = 0;
    public static final Integer BBYTE_MAX_VALUE = 255;

    public final BLangPackage rootPkgNode;
    public final BPackageSymbol rootPkgSymbol;
    public final BSymbol notFoundSymbol;
    public final BSymbol invalidUsageSymbol;
    public final Scope rootScope;

    public final BType noType = new BNoType(TypeTags.NONE);
    public final BType nilType = new BNilType();
    public final BType intType = new BType(TypeTags.INT, null);
    public final BType byteType = new BType(TypeTags.BYTE, null);
    public final BType floatType = new BType(TypeTags.FLOAT, null);
    public final BType decimalType = new BType(TypeTags.DECIMAL, null);
    public final BType stringType = new BType(TypeTags.STRING, null);
    public final BType booleanType = new BType(TypeTags.BOOLEAN, null);
    public final BType jsonType = new BJSONType(TypeTags.JSON, null);
    public final BType xmlType = new BXMLType(TypeTags.XML, null);
    public final BType tableType = new BTableType(TypeTags.TABLE, noType, null);
    public final BType anyType = new BAnyType(TypeTags.ANY, null);
    public final BType anydataType = new BAnydataType(TypeTags.ANYDATA, null);
    public final BType mapType = new BMapType(TypeTags.MAP, anyType, null);
    public final BType mapStringType = new BMapType(TypeTags.MAP, stringType, null);
    public final BType mapJsonType = new BMapType(TypeTags.MAP, jsonType, null);
    public final BType mapAnydataType = new BMapType(TypeTags.MAP, anydataType, null);
    public final BType futureType = new BFutureType(TypeTags.FUTURE, nilType, null);
    public final BType arrayType = new BArrayType(noType);
    public final BType tupleType = new BTupleType(Lists.of(noType));
    public final BType recordType = new BRecordType(null);
    public final BType stringArrayType = new BArrayType(stringType);
    public final BType jsonArrayType = new BArrayType(jsonType);
    public final BType anydataArrayType = new BArrayType(anydataType);
    public final BType anyServiceType = new BServiceType(null);
    public final BType handleType = new BHandleType(TypeTags.HANDLE, null);
    public final BType typeDesc = new BTypedescType(this.anyType, null);

    public final BType semanticError = new BType(TypeTags.SEMANTIC_ERROR, null);

    public BErrorType errorType;
    public BRecordType detailType;
    public BConstructorSymbol errorConstructor;
    public BUnionType anyOrErrorType;
    public BUnionType pureType;
    public BUnionType errorOrNilType;
    public BType streamType = new BStreamType(TypeTags.STREAM, pureType, null);
    public BFiniteType trueType;
    public BObjectType intRangeType;
    public BMapType mapAllType;

    // builtin subtypes
    public final BIntSubType signed32IntType = new BIntSubType(TypeTags.SIGNED32_INT, Names.SIGNED32);
    public final BIntSubType signed16IntType = new BIntSubType(TypeTags.SIGNED16_INT, Names.SIGNED16);
    public final BIntSubType signed8IntType = new BIntSubType(TypeTags.SIGNED8_INT, Names.SIGNED8);
    public final BIntSubType unsigned32IntType = new BIntSubType(TypeTags.UNSIGNED32_INT, Names.UNSIGNED32);
    public final BIntSubType unsigned16IntType = new BIntSubType(TypeTags.UNSIGNED16_INT, Names.UNSIGNED16);
    public final BIntSubType unsigned8IntType = new BIntSubType(TypeTags.UNSIGNED8_INT, Names.UNSIGNED8);
    public final BStringSubType charStringType = new BStringSubType(TypeTags.CHAR_STRING, Names.CHAR);

    public BPackageSymbol langInternalModuleSymbol;
    public BPackageSymbol langAnnotationModuleSymbol;
    public BPackageSymbol langArrayModuleSymbol;
    public BPackageSymbol langDecimalModuleSymbol;
    public BPackageSymbol langErrorModuleSymbol;
    public BPackageSymbol langFloatModuleSymbol;
    public BPackageSymbol langFutureModuleSymbol;
    public BPackageSymbol langIntModuleSymbol;
    public BPackageSymbol langMapModuleSymbol;
    public BPackageSymbol langObjectModuleSymbol;
    public BPackageSymbol langStreamModuleSymbol;
    public BPackageSymbol langStringModuleSymbol;
    public BPackageSymbol langTableModuleSymbol;
    public BPackageSymbol langTypedescModuleSymbol;
    public BPackageSymbol langValueModuleSymbol;
    public BPackageSymbol langXmlModuleSymbol;

    private Names names;
    public Map<BPackageSymbol, SymbolEnv> pkgEnvMap = new HashMap<>();

    public static SymbolTable getInstance(CompilerContext context) {
        SymbolTable symTable = context.get(SYM_TABLE_KEY);
        if (symTable == null) {
            symTable = new SymbolTable(context);
        }

        return symTable;
    }

    private SymbolTable(CompilerContext context) {
        context.put(SYM_TABLE_KEY, this);

        this.names = Names.getInstance(context);

        this.rootPkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.rootPkgSymbol = new BPackageSymbol(PackageID.ANNOTATIONS, null);
        this.rootPkgNode.pos = new DiagnosticPos(new BDiagnosticSource(rootPkgSymbol.pkgID, Names.EMPTY.value), 0, 0,
                0, 0);
        this.rootPkgNode.symbol = this.rootPkgSymbol;
        this.rootScope = new Scope(rootPkgSymbol);
        this.rootPkgSymbol.scope = this.rootScope;

        this.notFoundSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID,
                rootPkgSymbol.pkgID, noType, rootPkgSymbol);
        this.invalidUsageSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID, rootPkgSymbol.pkgID, noType,
                                              rootPkgSymbol);
        // Initialize built-in types in Ballerina
        initializeType(intType, TypeKind.INT.typeName());
        initializeType(byteType, TypeKind.BYTE.typeName());
        initializeType(floatType, TypeKind.FLOAT.typeName());
        initializeType(decimalType, TypeKind.DECIMAL.typeName());
        initializeType(stringType, TypeKind.STRING.typeName());
        initializeType(booleanType, TypeKind.BOOLEAN.typeName());
        initializeType(jsonType, TypeKind.JSON.typeName());
        initializeType(xmlType, TypeKind.XML.typeName());
        initializeType(tableType, TypeKind.TABLE.typeName());
        initializeType(streamType, TypeKind.STREAM.typeName());
        initializeType(mapType, TypeKind.MAP.typeName());
        initializeType(mapStringType, TypeKind.MAP.typeName());
        initializeType(mapAnydataType, TypeKind.MAP.typeName());
        initializeType(futureType, TypeKind.FUTURE.typeName());
        initializeType(anyType, TypeKind.ANY.typeName());
        initializeType(anydataType, TypeKind.ANYDATA.typeName());
        initializeType(nilType, TypeKind.NIL.typeName());
        initializeType(anyServiceType, TypeKind.SERVICE.typeName());
        initializeType(handleType, TypeKind.HANDLE.typeName());
        initializeType(typeDesc, TypeKind.TYPEDESC.typeName());

        // Define subtypes
        initializeTSymbol(signed32IntType, Names.SIGNED32, PackageID.INT);
        initializeTSymbol(signed16IntType, Names.SIGNED16, PackageID.INT);
        initializeTSymbol(signed8IntType, Names.SIGNED8, PackageID.INT);
        initializeTSymbol(unsigned32IntType, Names.UNSIGNED32, PackageID.INT);
        initializeTSymbol(unsigned16IntType, Names.UNSIGNED16, PackageID.INT);
        initializeTSymbol(unsigned8IntType, Names.UNSIGNED8, PackageID.INT);
        initializeTSymbol(charStringType, Names.CHAR, PackageID.STRING);

        BLangLiteral trueLiteral = new BLangLiteral();
        trueLiteral.type = this.booleanType;
        trueLiteral.value = Boolean.TRUE;

        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, Flags.PUBLIC,
                names.fromString("$anonType$TRUE"),
                rootPkgNode.packageID, null, rootPkgNode.symbol.owner);
        this.trueType = new BFiniteType(finiteTypeSymbol, new HashSet<BLangExpression>() {{
            add(trueLiteral);
        }});
    }

    public BType getTypeFromTag(int tag) {
        switch (tag) {
            case TypeTags.INT:
                return intType;
            case TypeTags.BYTE:
                return byteType;
            case TypeTags.FLOAT:
                return floatType;
            case TypeTags.DECIMAL:
                return decimalType;
            case TypeTags.STRING:
                return stringType;
            case TypeTags.BOOLEAN:
                return booleanType;
            case TypeTags.JSON:
                return jsonType;
            case TypeTags.XML:
                return xmlType;
            case TypeTags.TABLE:
                return tableType;
            case TypeTags.STREAM:
                return streamType;
            case TypeTags.NIL:
                return nilType;
            case TypeTags.ERROR:
                return errorType;
            default:
                return semanticError;
        }
    }

    public BType getLangLibSubType(String name) {
        // Assuming subtype names are unique across LangLib
        switch (name) {
            case Names.STRING_SIGNED32:
                return this.signed32IntType;
            case Names.STRING_SIGNED16:
                return this.signed16IntType;
            case Names.STRING_SIGNED8:
                return this.signed8IntType;
            case Names.STRING_UNSIGNED32:
                return this.unsigned32IntType;
            case Names.STRING_UNSIGNED16:
                return this.unsigned16IntType;
            case Names.STRING_UNSIGNED8:
                return this.unsigned8IntType;
            case Names.STRING_CHAR:
                return this.charStringType;
        }
        throw new IllegalStateException("LangLib Subtype not found: " + name);
    }

    private void initializeType(BType type, String name) {
        initializeType(type, names.fromString(name));
    }

    private void initializeType(BType type, Name name) {
        defineType(type, new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, name, rootPkgSymbol.pkgID, type, rootPkgSymbol));
    }

    private void initializeTSymbol(BType type, Name name, PackageID packageID) {

        type.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, name, packageID, type, rootPkgSymbol);
    }

    private void defineType(BType type, BTypeSymbol tSymbol) {
        type.tsymbol = tSymbol;
        rootScope.define(tSymbol.name, tSymbol);
    }

    public void defineOperators() {
        // Binary arithmetic operators
        defineBinaryOperator(OperatorKind.ADD, xmlType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, xmlType, stringType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, stringType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, intType, intType, intType);
        defineBinaryOperator(OperatorKind.ADD, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.ADD, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.ADD, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.ADD, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.ADD, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.ADD, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, intType, intType, intType);
        defineBinaryOperator(OperatorKind.SUB, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.SUB, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.SUB, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.SUB, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.SUB, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, intType, intType, intType);
        defineBinaryOperator(OperatorKind.DIV, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.DIV, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.DIV, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.DIV, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.DIV, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.DIV, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, intType, intType, intType);
        defineBinaryOperator(OperatorKind.MUL, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.MUL, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.MUL, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.MUL, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.MUL, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MOD, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, intType, intType, intType);
        defineBinaryOperator(OperatorKind.MOD, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.MOD, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.MOD, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.MOD, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.MOD, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MOD, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, byteType, intType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, intType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, byteType, intType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT, intType, byteType, intType);

        // Binary equality operators ==, !=
        defineBinaryOperator(OperatorKind.EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, jsonType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, jsonType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, anyType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anyType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, anydataType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anydataType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, jsonType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, jsonType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anyType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anyType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anydataType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anydataType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, nilType, booleanType);

        // Binary reference equality operators ===, !==
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, intType, booleanType);

        // Binary comparison operators <=, <, >=, >
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.AND, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.OR, booleanType, booleanType, booleanType);

        // Unary operator symbols
        defineUnaryOperator(OperatorKind.ADD, floatType, floatType);
        defineUnaryOperator(OperatorKind.ADD, decimalType, decimalType);
        defineUnaryOperator(OperatorKind.ADD, intType, intType);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType);
        defineUnaryOperator(OperatorKind.SUB, decimalType, decimalType);
        defineUnaryOperator(OperatorKind.SUB, intType, intType);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, byteType, byteType);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, intType, intType);

    }

    public void defineBinaryOperator(OperatorKind kind,
                                     BType lhsType,
                                     BType rhsType,
                                     BType retType) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        defineOperator(names.fromString(kind.value()), paramTypes, retType);
    }

    private void defineUnaryOperator(OperatorKind kind,
                                     BType type,
                                     BType retType) {
        List<BType> paramTypes = Lists.of(type);
        defineOperator(names.fromString(kind.value()), paramTypes, retType);
    }

    private void defineOperator(Name name,
                                List<BType> paramTypes,
                                BType retType) {
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BOperatorSymbol symbol = new BOperatorSymbol(name, rootPkgSymbol.pkgID, opType, rootPkgSymbol);
        rootScope.define(name, symbol);
    }
}
