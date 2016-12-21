/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.utils;

import org.testng.Assert;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.util.LangModelUtils;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Utility functions for Function Invocations.
 */
public class FunctionUtils {

    private FunctionUtils() {
    }

    /**
     * Add Native Function instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param function Function instance.
     * @return Given SymScope instance.
     */
    public static void addNativeFunction(SymScope symScope, AbstractNativeFunction function) {
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(function.getPackageName() + ":" +
                function.getClass().getAnnotation(BallerinaFunction.class).functionName(), function.getParameters());
        Symbol symbol = new Symbol(function,
                LangModelUtils.getTypesOfParams(function.getParameters()), function.getReturnTypesC());
        symScope.insert(symbolName, symbol);
    }

    /**
     * Generate FunctionInvocationExpr.
     *
     * @param bFile        BallerinaFile instance where callee function is defined.
     * @param functionName Callee function name.
     * @param arguments    Input arguments for callee function.
     * @return FunctionInvocationExpr instance.
     */
    public static FunctionInvocationExpr createInvocationExpr(BallerinaFile bFile, String functionName,
                                                              BValue[] arguments) {
        Assert.assertNotNull(functionName, "FunctionName can't be null.");
        Assert.assertNotNull(arguments, "Arguments can't be null. But it can be empty.");

        Expression[] exprs = new Expression[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            BValueRef valueRefA = new BValueRef(arguments[i]);
            BasicLiteral basicLiteralA = new BasicLiteral(valueRefA);
            exprs[i] = basicLiteralA;
        }

        FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(new SymbolName(functionName), exprs);
        funcIExpr.setOffset(0);
        funcIExpr.setFunction(bFile.getFunctions().get(functionName));

        return funcIExpr;
    }


    /**
     * Create Ballerina Context for function invocation.
     *
     * @param sizeOfReturnValues size of the return values.
     * @return Context instance for interpret function.
     */
    public static Context createInvocationContext(int sizeOfReturnValues) {
        Assert.assertTrue(sizeOfReturnValues >= 0);
        BValueRef[] results = new BValueRef[sizeOfReturnValues];
        StackFrame stackFrame = new StackFrame(results, null);

        Context bContext = new Context();
        bContext.getControlStack().pushFrame(stackFrame);
        return bContext;
    }

    /**
     * Get Value of the given position from the Context.
     *
     * @param context  Ballerina Context instance.
     * @param position position of the value.
     * @return BValueRef.
     */
    public static BValueRef getValue(Context context, int position) {
        StackFrame currentFrame = context.getControlStack().getCurrentFrame();
        return currentFrame.values[position];
    }

    /**
     * Get First Value from the Context.
     *
     * @param context  Ballerina Context instance.
     * @return BValueRef.
     */
    public static BValueRef getValue(Context context) {
        StackFrame currentFrame = context.getControlStack().getCurrentFrame();
        return currentFrame.values[0];
    }
}
