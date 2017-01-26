/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.StructDcl;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code StructInitExpr} represents a struct initializer expression
 * 
 * TODO: fix this
 * Extends {@code NaryExpression} because can be considered as an operation with multiple arguments.
 *
 * @since 1.0.0
 */
public class StructInitExpr extends AbstractExpression {

    private StructDcl structDcl;

    public StructInitExpr(StructDcl structDcl) {
        this.structDcl = structDcl;
    }

    public StructDcl getStructDcl() {
        return structDcl;
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    /**
     * {@code StructInitExprBuilder} represents a struct initializer expression builder
     *
     * @since 1.0.0
     */
    public static class StructInitExprBuilder {
        StructDcl structDcl;

        public StructInitExprBuilder() {
        }
        
        public void setStructDcl(StructDcl structDcl) {
            this.structDcl = structDcl;
        }
        
        public StructInitExpr build() {
            return new StructInitExpr(structDcl);
        }
    }
}
