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
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Operator;

/**
 * {@code NaryExpr} represents an operation with multiple operands.
 * <p>
 * Refer: https://en.wikipedia.org/wiki/Arity#n-ary
 *
 * @see ArrayInitExpr
 * @since 1.0.0
 */
public class NaryExpression extends UnaryExpression {

    private Expression[] argExprs;

    public NaryExpression(Operator op, Expression rExpr, Expression[] argExprs) {
        super(op, rExpr);
        this.argExprs = argExprs;
    }

    public Expression[] getArgExprs() {
        return argExprs;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
