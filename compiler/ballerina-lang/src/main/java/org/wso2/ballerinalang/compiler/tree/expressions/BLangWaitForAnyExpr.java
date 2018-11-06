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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.WaitForAnyExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents the wait for any expression implementation.
 *
 * @since 0.985
 */
public class BLangWaitForAnyExpr extends BLangExpression implements WaitForAnyExpressionNode {

    private static final String WAIT_KEYWORD = "wait";

    public List<BLangExpression> exprList = new ArrayList<>();

    public List<BLangExpression> getExpressionList() {
        return exprList;
    }

    @Override
    public String toString() {
        return WAIT_KEYWORD + " " + String.join("|", exprList.stream().map(Object::toString)
                                                             .collect(Collectors.toList()));
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.WAIT_EXPR;
    }

}
