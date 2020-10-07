/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.internal;

import org.ballerinalang.jvm.api.BValueCreator;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.TypedescValue;

/**
 * Native implementation of lang.internal:getElementType(typedesc).
 *
 * @since 1.2.0
 */
public class GetElementType {

    public static TypedescValue getElementType(Object td) {
        TypedescValue typedescValue = (TypedescValue) td;
        BType type = typedescValue.getDescribingType();
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            return (TypedescValue) BValueCreator.createTypedescValue(((BArrayType) type).getElementType());
        } else if (type.getTag() == TypeTags.STREAM_TAG) {
            return (TypedescValue) BValueCreator.createTypedescValue(((BStreamType) type).getConstrainedType());
        } else if (type.getTag() == TypeTags.TABLE_TAG) {
            return (TypedescValue) BValueCreator.createTypedescValue(((BTableType) type).getConstrainedType());
        }

        return (TypedescValue) BValueCreator.createTypedescValue(BTypes.typeNull);
    }
}
