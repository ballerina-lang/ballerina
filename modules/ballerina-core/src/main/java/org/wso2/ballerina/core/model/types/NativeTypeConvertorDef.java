/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
 */
package org.wso2.ballerina.core.model.types;

import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.function.Function;

/**
 * Type convertor represents a native conversion
 */
public class NativeTypeConvertorDef implements BLangSymbol {

    private Function<BValueType, BValueType> typeConvertor;
    private SymbolScope symbolScope;
    private SymbolName symbolName;

    public NativeTypeConvertorDef(Function<BValueType, BValueType> typeConvertor, SymbolScope symbolScope,
                                  SymbolName symbolName) {
        this.typeConvertor = typeConvertor;
        this.symbolScope = symbolScope;
        this.symbolName = symbolName;
    }
    @Override
    public String getName() {
        return symbolName.getName();
    }

    @Override
    public String getPackagePath() {
        return null;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return symbolScope;
    }
}
