/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl;

import org.wso2.ballerina.core.model.NativeUnit;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;

import java.util.function.Supplier;

/**
 * Proxy class to hold Native units.
 *
 * @since 0.8.0
 */
public class NativeUnitProxy implements BLangSymbol {
    private Supplier<NativeUnit> nativeFunctionSupplier;
    private NativeUnit nativeUnit;

    public NativeUnitProxy(Supplier<NativeUnit> nativeFunctionSupplier) {
        this.nativeFunctionSupplier = nativeFunctionSupplier;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getPackagePath() {
        return null;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public SymbolName getSymbolName() {
        return null;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return null;
    }
    
    public NativeUnit load() {
        if (nativeUnit == null) {
            nativeUnit = this.nativeFunctionSupplier.get();
        }
        return nativeUnit;
    }
}
