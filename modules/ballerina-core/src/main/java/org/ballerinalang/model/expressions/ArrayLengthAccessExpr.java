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

package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BValue;

/**
 * <p>
 * {@code ArrayLengthAccessExpr} represents array length access operation.
 * </p>
 * eg:
 * <p>
 * Sample usage <b>arrayRef.length</b> returns occupied slots in array referenced by arrayRef.
 * </p>
 * @since 1.0.0
 */
public class ArrayLengthAccessExpr extends UnaryExpression implements ReferenceExpr {

    private ReferenceExpr varRefExpr;

    public ArrayLengthAccessExpr(NodeLocation location, ReferenceExpr arraylengthExpr) {
        super(location, null, arraylengthExpr);
        this.varRefExpr = arraylengthExpr;
    }

    @Override
    public String getVarName() {
        return null;
    }


    @Override
    public SymbolName getSymbolName() {
        return null;
    }


    public ReferenceExpr getVarRef() {
        return varRefExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    @Override
    public BType getType() {
        return BTypes.typeInt;
    }

}
