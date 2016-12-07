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

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.utils.TriFunction;

import static org.wso2.ballerina.core.model.Operator.EQUAL;

/**
 * {@code EqualExpression} represents a equal expression(==) in Ballerina
 *
 * @since 1.0.0
 */
public class EqualExpression extends BinaryEqualityExpression {

    public static final TriFunction<Context, Expression, Expression, BValueRef> EQUAL_INT_FUNC =
            (ctx, lExpr, rExpr) -> {
                boolean result = lExpr.evaluate(ctx).getInt() == rExpr.evaluate(ctx).getInt();
                BValue bValue = new BooleanValue(result);
                return new BValueRef(bValue);
            };

    public static final TriFunction<Context, Expression, Expression, BValueRef> EQUAL_LONG_FUNC =
            (ctx, lExpr, rExpr) -> {
                boolean result = lExpr.evaluate(ctx).getLong() == rExpr.evaluate(ctx).getLong();
                BValue bValue = new BooleanValue(result);
                return new BValueRef(bValue);
            };

    public static final TriFunction<Context, Expression, Expression, BValueRef> EQUAL_FLOAT_FUNC =
            (ctx, lExpr, rExpr) -> {
                boolean result = lExpr.evaluate(ctx).getFloat() == rExpr.evaluate(ctx).getFloat();
                BValue bValue = new BooleanValue(result);
                return new BValueRef(bValue);
            };

    public static final TriFunction<Context, Expression, Expression, BValueRef> EQUAL_DOUBLE_FUNC =
            (ctx, lExpr, rExpr) -> {
                boolean result = lExpr.evaluate(ctx).getDouble() == rExpr.evaluate(ctx).getDouble();
                BValue bValue = new BooleanValue(result);
                return new BValueRef(bValue);
            };

    public static final TriFunction<Context, Expression, Expression, BValueRef> EQUAL_STRING_FUNC =
            (ctx, lExpr, rExpr) -> {
                boolean result = lExpr.evaluate(ctx).getString().equals(rExpr.evaluate(ctx).getString());
                BValue bValue = new BooleanValue(result);
                return new BValueRef(bValue);
            };

    public EqualExpression(Expression lExpr, Expression rExpr) {
        super(lExpr, EQUAL, rExpr);
    }
}
