/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.jvm.scheduling.CallerEnv;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BValueCreator;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_RUNTIME_PKG_ID;

/**
 * Extern function ballerina/runtime:getCurrentModule.
 *
 * @since 2.0.0
 */
public class GetCurrentModule {

    public static MapValue getCurrentModule(CallerEnv callerEnv) {
        Map<String, Object> moduleInfo = new HashMap<>();
        moduleInfo.put("orgName", callerEnv.getOrgName());
        moduleInfo.put("name", callerEnv.getName());
        moduleInfo.put("'version", callerEnv.getVersion());
        return (MapValue) BValueCreator.createRecordValue(BALLERINA_RUNTIME_PKG_ID, "Module", moduleInfo);
    }
}
