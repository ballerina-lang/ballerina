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

import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.TypedescValue;

import java.util.HashMap;

/**
 * Native implementation of lang.internal:setNarrowType(typedesc, (any|error)[]).
 *
 * @since 1.2.0
 */
public class SetNarrowType {

    public static MapValue setNarrowType(TypedescValue td, MapValue value) {
        BRecordType recordType = (BRecordType) value.getType();
        BRecordType newRecordType = new BRecordType("narrowType", recordType.getPackage(), recordType.flags,
                recordType.sealed, recordType.typeFlags);
        newRecordType.setFields(new HashMap<>() {{
            put("value", new BField(td.getDescribingType(), "value", Flags.PUBLIC + Flags.REQUIRED));
        }});

        MapValueImpl<BString, Object> newRecord = new MapValueImpl<>(newRecordType);
        newRecord.put(BStringUtils.fromString("value"), value.get(BStringUtils.fromString("value")));
        return newRecord;
    }
}
