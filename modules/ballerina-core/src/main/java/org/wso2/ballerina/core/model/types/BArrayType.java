/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.types;

import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.annotations.BallerinaPrimitive;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code BArrayType} represents array type in Ballerina.
 * <p>
 * Arrays are defined using the array constructor [] as follows:
 * TypeName[]
 * <p>
 * All arrays are unbounded in length and support 0 based indexing.
 *
 * @since 0.8.0
 */
@BallerinaPrimitive(type = "array",
        description = "Represents array type in Ballerina. Arrays are defined using the array constructor [] " +
                "as follows: TypeName[]. All arrays are unbounded in length and supported 0 based indexing.",
        defaultValue = "[]",
        usage = "[data-type][] [variable] = [[value-1], [value-2], ..., [value-n]];")
public class BArrayType extends BType implements BIndexedType {
    private BType elementType;

    /**
     * Creates a type from the given name.
     *
     * @param typeName string name of the type
     * @param pkgPath package path
     * @param symbolScope scope of the symbol
     */
    BArrayType(String typeName, BType elementType, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BArray.class);
        this.elementType = elementType;
    }

    public BType getElementType() {
        return elementType;
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        return (V) new BArray<V>(elementType.getValueClass());
    }

    public boolean equals(Object obj) {
        if (obj instanceof BArrayType) {
            BArrayType other = (BArrayType) obj;
            return this.typeName.equals(other.typeName);
        }

        return false;
    }
}
