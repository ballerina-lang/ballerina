/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.types;

import org.wso2.ballerina.core.model.values.BValue;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code BType} represents a type in Ballerina
 * <p>
 * Ballerina has variables of various types. The type system includes built-in primitive or value types,
 * a collection of built-in structured types, and array, record and iterator type constructors.
 * All variables of primitive types are allocated on the stack while all non-primitive types are
 * allocated on a heap using new.
 *
 * @since 1.0.0
 */
public abstract class BType {

    protected String typeName;

    //Using a HashMap here, because there won't be any concurrent access
    // TODO Improve this to support modularity of Ballerina
    private static final Map<String, BType> TYPE_MAP = new HashMap<>(20);

    /**
     * Create a type from the given name
     *
     * @param typeName string name of the type
     */
    protected BType(String typeName) {
        this.typeName = typeName;
        TYPE_MAP.put(typeName, this);
    }

    public abstract <V extends BValue> V[] createArray();

    public abstract <V extends BValue> V getDefaultValue();

    public String toString() {
        return typeName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BType) {
            BType other = (BType) obj;
            return this.typeName.equals(other.typeName);
        }
        return false;
    }

    public int hashCode() {
        return typeName.length();
    }

    @SuppressWarnings("unchecked")
    static <T extends BType> T getType(String typeName) {
        return (T) TYPE_MAP.get(typeName);
    }
}
