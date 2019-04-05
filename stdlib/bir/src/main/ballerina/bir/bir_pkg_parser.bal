// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type PackageParser object {
    BirChannelReader reader;
    TypeParser typeParser;
    map<VariableDcl> globalVarMap;
    boolean dumpBIR;

    public function __init(BirChannelReader reader, TypeParser typeParser, boolean dumpBIR) {
        self.reader = reader;
        self.typeParser = typeParser;
        self.globalVarMap = {};
        self.dumpBIR = dumpBIR;
    }

    public function parseVariableDcl() returns VariableDcl {
        VarKind kind = parseVarKind(self.reader);
        var typeValue = self.typeParser.parseType();
        var name = self.reader.readStringCpRef();
        VariableDcl dcl = {
            typeValue: typeValue,
            name: { value: name },
            kind: kind
        };
        return dcl;
    }

    public function parseFunction(TypeDef?[] typeDefs) returns Function {
        var name = self.reader.readStringCpRef();
        var isDeclaration = self.reader.readBoolean();
        var visibility = parseVisibility(self.reader);
        var typeTag = self.reader.readInt8();
        if (typeTag != self.typeParser.TYPE_TAG_INVOKABLE) {
            error err = error("Illegal function signature type tag" + typeTag);
            panic err;
        }
        var sig = self.typeParser.parseInvokableType();
        var argsCount = self.reader.readInt32();
        var numLocalVars = self.reader.readInt32();

        VariableDcl?[] dcls = [];
        map<VariableDcl> localVarMap = {};
        int i = 0;
        while (i < numLocalVars) {
            var dcl = self.parseVariableDcl();
            dcls[i] = dcl;
            localVarMap[dcl.name.value] = dcl;
            i += 1;
        }
        FuncBodyParser bodyParser = new(self.reader, self.typeParser, self.globalVarMap, localVarMap, typeDefs);
        BasicBlock?[] basicBlocks = self.getBasicBlocks(bodyParser);
        ErrorEntry?[] errorEntries = self.getErrorEntries(bodyParser);
        return {
            name: { value: name },
            isDeclaration: isDeclaration,
            visibility: visibility,
            localVars: dcls,
            basicBlocks: basicBlocks,
            errorEntries:errorEntries,
            argsCount: argsCount,
            typeValue: sig
        };
    }

    public function parsePackage() returns Package {
        ModuleID pkgId = self.reader.readModuleIDCpRef();
        ImportModule[] importModules = self.parseImportMods();
        TypeDef?[] typeDefs = self.parseTypeDefs();
        GlobalVariableDcl?[] globalVars = self.parseGlobalVars();
        var numFuncs = self.reader.readInt32();
        Function?[] funcs = [];
        int i = 0;
        while (i < numFuncs) {
            funcs[i] = self.parseFunction(typeDefs);
            i += 1;
        }
        
        if (self.dumpBIR) {
           BirEmitter emitter = new({ importModules: importModules, typeDefs: typeDefs, globalVars:globalVars,
                                        functions: funcs, name: {value: pkgId.name}, org: {value: pkgId.org},
                                        versionValue: {value: pkgId.modVersion}});
           emitter.emitPackage();
        }

        return { importModules : importModules, 
                    typeDefs : typeDefs, 
                    globalVars : globalVars, 
                    functions : funcs,
                    name : { value: pkgId.name }, 
                    org : { value: pkgId.org }, 
                    versionValue : { value: pkgId.modVersion } };
    }

    function getBasicBlocks(FuncBodyParser bodyParser) returns BasicBlock?[] {
        BasicBlock?[] basicBlocks = [];
        var numBB = self.reader.readInt32();
        int i = 0;
        while (i < numBB) {
            basicBlocks[i] = bodyParser.parseBB();
            i += 1;
        }

        return basicBlocks;
    }

    function getErrorEntries(FuncBodyParser bodyParser) returns ErrorEntry?[] {
        ErrorEntry?[] errorEntries = [];
        var numEE = self.reader.readInt32();
        int i = 0;
        while (i < numEE) {
            errorEntries[i] = bodyParser.parseEE();
            i += 1;
        }
        return errorEntries;
    }

    function parseImportMods() returns ImportModule[] {
        int numImportMods = self.reader.readInt32();
        ImportModule[] importModules = [];
        foreach var i in 0..<numImportMods {
            string modOrg = self.reader.readStringCpRef();
            string modName = self.reader.readStringCpRef();
            string modVersion = self.reader.readStringCpRef();
            importModules[i] = { modOrg: { value: modOrg }, modName: { value: modName },
                modVersion: { value: modVersion } };
        }
        return importModules;
    }

    function parseTypeDefs() returns TypeDef?[] {
        int numTypeDefs = self.reader.readInt32();
        TypeDef?[] typeDefs = [];
        int i = 0;
        while i < numTypeDefs {
            typeDefs[i] = self.parseTypeDef();
            i = i + 1;
        }

        return typeDefs;
    }

    function parseTypeDef() returns TypeDef {
        string name = self.reader.readStringCpRef();
        Visibility visibility = parseVisibility(self.reader);
        var bType = self.typeParser.parseType();
        Function?[]? attachedFuncs = ();
        if (bType is BObjectType || bType is BRecordType) {
            Function?[] funcs = [];
            var numFuncs = self.reader.readInt32();
            int i = 0;
            while (i < numFuncs) {
                funcs[i] = self.parseFunction([]);
                i += 1;
            }
            attachedFuncs = funcs;
        }

        return { name: { value: name }, visibility: visibility, typeValue: bType, attachedFuncs: attachedFuncs };
    }

    function parseGlobalVars() returns GlobalVariableDcl?[] {       
        GlobalVariableDcl?[] globalVars = []; 
        int numGlobalVars = self.reader.readInt32();        
        int i = 0;
        while i < numGlobalVars {
            var kind = parseVarKind(self.reader);
            string name = self.reader.readStringCpRef();
            Visibility visibility = parseVisibility(self.reader);
            var typeValue = self.typeParser.parseType();
            GlobalVariableDcl dcl = {kind:kind, name:{value:name}, typeValue:typeValue, visibility:visibility};
            globalVars[i] = dcl;
            self.globalVarMap[name] = dcl;
            i = i + 1;
        }
        return globalVars;
    }

    public function parseSig(string sig) returns BInvokableType {
        BType returnType = "int";
        //TODO: add boolean
        if (sig.lastIndexOf("(N)") == (sig.length() - 3)) {
            returnType = "()";
        }
        return {
            retType: returnType
        };
    }

};

public function parseVarKind(BirChannelReader reader) returns VarKind {
    int b = reader.readInt8();
    if (b == 1) {
        LocalVarKind local = "LOCAL";
        return local;
    } else if (b == 2) {
        ArgVarKind arg = "ARG";
        return arg;
    } else if (b == 3) {
        TempVarKind temp = "TEMP";
        return temp;
    } else if (b == 4) {
        ReturnVarKind ret = "RETURN";
        return ret;
    } else if (b == 5) {
        GlobalVarKind ret = "GLOBAL";
        return ret;
    } 
    error err = error("unknown var kind tag " + b);
    panic err;
}

public function parseVarScope(BirChannelReader reader) returns VarScope {
    int b = reader.readInt8();
    if (b == 1) {
        VarScope local = VAR_SCOPE_FUNCTION;
        return local;
    } else if (b == 2) {
        VarScope glob = VAR_SCOPE_GLOBAL;
        return glob;
    }
    error err = error("unknown var scope tag " + b);
    panic err;
}

public function parseVisibility(BirChannelReader reader) returns Visibility {
    int b = reader.readInt8();
    if (b == 0) {
        return "PACKAGE_PRIVATE";
    } else if (b == 1) {
        return "PRIVATE";
    } else if (b == 2) {
        return "PUBLIC";
    }
    error err = error("unknown variable visiblity tag " + b);
        panic err;
}

