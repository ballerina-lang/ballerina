/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal.configurable.providers;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;

import java.util.Map;

/**
 *  This interface represents the providers that will be used to retrieve the configuration from user.
 *
 *  @since 2.0.0
 */
public interface ConfigProvider {

    void initialize(Map<Module, VariableKey[]> configVarMap);

    boolean hasConfigs();

    ConfigValue getAsIntAndMark(Module module, VariableKey key);

    ConfigValue getAsByteAndMark(Module module, VariableKey key);

    ConfigValue getAsBooleanAndMark(Module module, VariableKey key);

    ConfigValue getAsFloatAndMark(Module module, VariableKey key);

    ConfigValue getAsDecimalAndMark(Module module, VariableKey key);

    ConfigValue getAsStringAndMark(Module module, VariableKey key);

    ConfigValue getAsArrayAndMark(Module module, VariableKey key);

    ConfigValue getAsRecordAndMark(Module module, VariableKey key);

    ConfigValue getAsTableAndMark(Module module, VariableKey key);

}
