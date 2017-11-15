/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.InvocationNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link org.ballerinalang.model.tree.expressions.InvocationNode}.
 *
 * @since 0.94
 */
public class BLangInvocation extends BLangVariableReference implements InvocationNode, MultiReturnExpr {

    public BLangIdentifier pkgAlias;
    public BLangIdentifier name;
    public List<BLangExpression> argExprs = new ArrayList<>();
    public BLangVariableReference expr;
    public List<BType> types = new ArrayList<>(0);
    public int[] regIndexes;
    public BSymbol symbol;
    public boolean functionPointerInvocation;

    public boolean isMultiReturnExpr() {
        return true;
    }

    @Override
    public IdentifierNode getPackageAlias() {
        return pkgAlias;
    }

    @Override
    public IdentifierNode getName() {
        return name;
    }

    @Override
    public List<? extends ExpressionNode> getArgumentExpressions() {
        return argExprs;
    }

    @Override
    public BLangVariableReference getExpression() {
        return expr;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.INVOCATION;
    }

    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        if (expr != null) {
            // Action invocation or lambda invocation.
            br.append(String.valueOf(expr)).append(".");
        } else if (pkgAlias != null && !pkgAlias.getValue().isEmpty()) {
            br.append(String.valueOf(pkgAlias)).append(":");
        }
        br.append(String.valueOf(name));
        br.append("(");
        if (argExprs.size() > 0) {
            String s = Arrays.toString(argExprs.toArray());
            br.append(s.substring(1, s.length() - 1));
        }
        br.append(")");
        return br.toString();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<BType> getTypes() {
        return types;
    }

    @Override
    public void setTypes(List<BType> types) {
        this.types = types;
    }

    @Override
    public int[] getRegIndexes() {
        return this.regIndexes;
    }


    /**
     * @since 0.94
     */
    public static class BFunctionPointerInvocation extends BLangInvocation {

        public BFunctionPointerInvocation(BLangInvocation parent, BLangVariableReference varRef) {
            this.pos = parent.pos;
            this.name = parent.name;
            this.argExprs = parent.argExprs;
            this.types = parent.types;
            this.regIndexes = parent.regIndexes;
            this.symbol = parent.symbol;
            this.expr = varRef;
            if (types.size() > 0) {
                this.type = types.get(0);
            }
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangFunctionInvocation extends BLangInvocation {

        public BLangFunctionInvocation(DiagnosticPos pos,
                                       List<BLangExpression> argExprs,
                                       BSymbol symbol,
                                       List<BType> types) {
            this.pos = pos;
            this.argExprs = argExprs;
            this.symbol = symbol;
            this.types = types;
            if (types.size() > 0) {
                this.type = types.get(0);
            }
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangActionInvocation extends BLangInvocation {

        public BLangActionInvocation(DiagnosticPos pos,
                                       List<BLangExpression> argExprs,
                                       BSymbol symbol,
                                       List<BType> types) {
            this.pos = pos;
            this.argExprs = argExprs;
            this.symbol = symbol;
            this.types = types;
            if (types.size() > 0) {
                this.type = types.get(0);
            }
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94.2
     */
    public static class BLangTransformerInvocation extends BLangInvocation {

        public BLangTransformerInvocation(DiagnosticPos pos,
                                          List<BLangExpression> argExprs,
                                          BSymbol symbol,
                                          List<BType> types) {
            this.pos = pos;
            this.argExprs = argExprs;
            this.symbol = symbol;
            this.types = types;
            if (types.size() > 0) {
                this.type = types.get(0);
            }
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
