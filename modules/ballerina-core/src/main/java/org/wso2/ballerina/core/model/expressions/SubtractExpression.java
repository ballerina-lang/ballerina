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
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.function.BiFunction;

import static org.wso2.ballerina.core.model.Operator.SUB;

/**
 * {@code SubstractExpression} represents a binary substract expression.
 *
 * @since 0.8.0
 */
public class SubtractExpression extends BinaryArithmeticExpression {

    public static final BiFunction<BValueType, BValueType, BValueType> SUB_INT_FUNC =
            (lVal, rVal) -> new BInteger(lVal.intValue() - rVal.intValue());

    public static final BiFunction<BValueType, BValueType, BValueType> SUB_LONG_FUNC =
            (lVal, rVal) -> new BLong(lVal.longValue() - rVal.longValue());

    public static final BiFunction<BValueType, BValueType, BValueType> SUB_FLOAT_FUNC =
            (lVal, rVal) -> new BFloat(lVal.floatValue() - rVal.floatValue());

    public static final BiFunction<BValueType, BValueType, BValueType> SUB_DOUBLE_FUNC =
            (lVal, rVal) -> new BDouble(lVal.doubleValue() - rVal.doubleValue());

    public SubtractExpression(Expression lExpr, Expression rExpr, Position location) {
        super(lExpr, SUB, rExpr, location);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
