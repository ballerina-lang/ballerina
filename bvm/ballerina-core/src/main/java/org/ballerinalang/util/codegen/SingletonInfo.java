/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.ballerinalang.model.types.BType;

import java.util.Objects;

/**
 * Represent serializable unit of Singleton value.
 */
public class SingletonInfo extends StructureTypeInfo {
    private BType singletonType;


    public SingletonInfo(int pkgPathCPIndex, String packagePath,
                         int nameCPIndex, String name, int flags) {
        super(pkgPathCPIndex, packagePath, nameCPIndex, name, flags);
    }

    public BType getType() {
        return singletonType;
    }

    public void setType(BType singletonType) {
        this.singletonType = singletonType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgPathCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SingletonInfo && pkgPathCPIndex == (((SingletonInfo) obj).pkgPathCPIndex)
                && nameCPIndex == (((SingletonInfo) obj).nameCPIndex);
    }
}
