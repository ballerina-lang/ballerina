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

import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code BLongType} represents a long which is a 64-bit signed number
 *
 * @since 1.0.0
 */
class BLongType extends BType {

    /**
     * Create a {@code BLongType} which represents the boolean type
     *
     * @param typeName string name of the type
     */
    BLongType(String typeName) {
        super(typeName);
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V[] createArray() {
        return (V[]) BTypes.createArray(BLong[].class);
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        return (V) new BLong(0);
    }
}
