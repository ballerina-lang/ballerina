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

import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BacktickExpr} represents an xml or a json string wrapped in between backticks/backquotes
 *
 * @since 0.8.0
 */
public class BacktickExpr extends AbstractExpression {

    private String templateStr;

    List<Expression> expressionList = new ArrayList<>();

    private BacktickExpr(String templateStr) {
        this.templateStr = templateStr;
    }

    public String getTemplateStr() {
        return templateStr;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public void addExpression(Expression expression) {
        expressionList.add(expression);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    /**
     *
     */
    public static class BacktickExprBuilder {
        private String templateStr;

        public BacktickExprBuilder() {
        }

        public void setTemplateStr(String templateStr) {
            this.templateStr = templateStr;
        }

        public BacktickExpr build() {
            return new BacktickExpr(this.templateStr);
        }
    }
}
