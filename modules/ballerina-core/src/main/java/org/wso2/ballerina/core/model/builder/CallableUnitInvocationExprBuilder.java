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
package org.wso2.ballerina.core.model.builder;

import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for building function/action invocation expressions.
 *
 * @since 0.8.0
 */
class CallableUnitInvocationExprBuilder {
    private SymbolName name;
    private List<Expression> expressionList = new ArrayList<>();

    SymbolName getName() {
        return name;
    }

    void setName(SymbolName name) {
        this.name = name;
    }

    List<Expression> getExpressionList() {
        return expressionList;
    }

    void setExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    FunctionInvocationExpr buildFuncInvocExpr() {
        return new FunctionInvocationExpr(name, expressionList.toArray(new Expression[expressionList.size()]));
    }

    ActionInvocationExpr buildActionInvocExpr() {
        return new ActionInvocationExpr(name, expressionList.toArray(new Expression[expressionList.size()]));
    }
}
