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
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.types.BConnectorType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@code ConnectorInfo} contains metadata of a Ballerina connector entry in the program file.
 *
 * @since 0.87
 */
public class ConnectorInfo extends StructureTypeInfo {

    private BConnectorType connectorType;

    private Map<String, ActionInfo> actionInfoMap = new HashMap<>();

    // This variable holds the method table for this type.
    protected Map<Integer, Integer> methodTableIndex = new HashMap<>();
    protected Map<BConnectorType, ConnectorInfo> methodTableType = new HashMap<>();

    public ConnectorInfo(int pkgPathCPIndex, String packagePath, int nameCPIndex, String name, int flags) {
        super(pkgPathCPIndex, packagePath, nameCPIndex, name, flags);
    }

    public Map<Integer, Integer> getMethodTableIndex() {
        return methodTableIndex;
    }

    public void setMethodTableType(Map<BConnectorType, ConnectorInfo> methodTable) {
        this.methodTableType = methodTable;
    }

    public BConnectorType getType() {
        return connectorType;
    }

    public void setType(BConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public void addActionInfo(String actionName, ActionInfo actionInfo) {
        actionInfoMap.put(actionName, actionInfo);
    }

    public ActionInfo getActionInfo(String actionName) {
        return actionInfoMap.get(actionName);
    }

    public ActionInfo[] getActionInfoEntries() {
        return actionInfoMap.values().toArray(new ActionInfo[0]);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgPathCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConnectorInfo && pkgPathCPIndex == (((ConnectorInfo) obj).pkgPathCPIndex)
                && nameCPIndex == (((ConnectorInfo) obj).nameCPIndex);
    }
}
