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
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * All the expressions should extend {@code AbstractExpression}
 * <p>
 * Provides the common behaviour of expressions.
 *
 * @since 1.0.0
 */
public abstract class AbstractExpression implements Expression {

    protected BType type;
    protected int offset;
    protected Position expressionLocation;

    public BType getType() {
        return type;
    }

    public void setType(BType type) {
        this.type = type;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public BValue execute(NodeExecutor executor) {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public Position getLocation() {
        return expressionLocation;
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(Position location) {
        this.expressionLocation = location;
    }
}
