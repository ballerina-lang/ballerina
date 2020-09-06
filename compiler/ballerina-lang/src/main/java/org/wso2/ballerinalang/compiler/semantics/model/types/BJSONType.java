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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.LinkedHashSet;

/**
 * @since 0.94
 */
public class BJSONType extends BUnionType implements SelectivelyImmutableReferenceType {

    public BJSONType(BJSONType bjsonType, boolean nullable) {
        super(bjsonType.tsymbol, bjsonType.getMemberTypes(), nullable, false);
        this.tag = TypeTags.JSON;
    }

    public BJSONType(BUnionType type) {
        super(type.tsymbol, new LinkedHashSet<>(type.getMemberTypes()), type.isNullable(), Symbols.isFlagOn(type.flags,
                Flags.READONLY));
        this.immutableType = type.immutableType;
        this.tag = TypeTags.JSON;
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.JSON;
    }
}
