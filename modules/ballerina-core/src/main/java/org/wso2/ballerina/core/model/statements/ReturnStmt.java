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
package org.wso2.ballerina.core.model.statements;

import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.expressions.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code ReturnStmt} represents a return statement
 *
 * @since 1.0.0
 */
public class ReturnStmt implements Statement {
    private Expression expr;

    private Expression[] exprs;

    public ReturnStmt(Expression expr) {
        this.expr = expr;
    }

    private ReturnStmt(Expression[] exprs) {
        this.exprs = exprs;
    }

    public Expression[] getExprs() {
        return exprs;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Builds a {@code ReturnStmt} statement
     *
     * @since 1.0.0
     */
    public static class ReturnStmtBuilder {
        List<Expression> expressionList = new ArrayList<>();

        public ReturnStmtBuilder() {
        }

        public void setExpressionList(List<Expression> expressionList) {
            this.expressionList = expressionList;
        }

        public void addExpression(Expression expr) {
            expressionList.add(expr);
        }

        public ReturnStmt build() {
            return new ReturnStmt(expressionList.toArray(new Expression[expressionList.size()]));
        }
    }
}
