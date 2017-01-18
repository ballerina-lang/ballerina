/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.CallableUnitInvocationExpr;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.KeyValueExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.util.BValueUtils;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;


/**
 * {@code BLangExecutor} executes a Ballerina application
 *
 * @since 0.8.0
 */
public class BLangExecutor implements NodeExecutor {

    private RuntimeEnvironment runtimeEnv;
    private Context bContext;
    private ControlStack controlStack;

    public BLangExecutor(RuntimeEnvironment runtimeEnv, Context bContext) {
        this.runtimeEnv = runtimeEnv;
        this.bContext = bContext;
        this.controlStack = bContext.getControlStack();
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        //TODO Improve this to support non-blocking behaviour.
        //TODO Possibly a linked set of statements would do.

        Statement[] stmts = blockStmt.getStatements();
        for (Statement stmt : stmts) {
            stmt.execute(this);
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        // TODO WARN: Implementation of this method is inefficient
        // TODO We are in the process of refactoring this method, please bear with us.
        BValue[] rValues;
        Expression rExpr = assignStmt.getRExpr();

        Expression[] lExprs = assignStmt.getLExprs();
        if (lExprs.length > 1) {
            // This statement contains multiple assignments
            rValues = ((CallableUnitInvocationExpr) rExpr).executeMultiReturn(this);
        } else {
            rValues = new BValue[]{rExpr.execute(this)};
        }

        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BValue rValue = rValues[i];

            if (lExpr instanceof VariableRefExpr) {
                assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);

            } else if (lExpr instanceof ArrayMapAccessExpr) {
                assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
            }
        }
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression expr = ifElseStmt.getCondition();
        BBoolean condition = (BBoolean) expr.execute(this);

        if (condition.booleanValue()) {
            ifElseStmt.getThenBody().execute(this);
            return;
        }

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            condition = (BBoolean) elseIfCondition.execute(this);

            if (condition.booleanValue()) {
                elseIfBlock.getElseIfBody().execute(this);
                return;
            }
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.execute(this);
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression expr = whileStmt.getCondition();
        BBoolean condition = (BBoolean) expr.execute(this);

        while (condition.booleanValue()) {
            // Interpret the statements in the while body.
            whileStmt.getBody().execute(this);

            // Now evaluate the condition again to decide whether to continue the loop or not.
            condition = (BBoolean) expr.execute(this);
        }
    }

    @Override
    public void visit(FunctionInvocationStmt funcIStmt) {
        funcIStmt.getFunctionInvocationExpr().executeMultiReturn(this);
    }

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        actionIStmt.getActionInvocationExpr().executeMultiReturn(this);
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        Expression[] exprs = returnStmt.getExprs();

        // Check whether the first argument is a multi-return function
        if (exprs.length == 1 && exprs[0] instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) exprs[0];
            if (funcIExpr.getTypes().length > 1) {
                BValue[] returnVals = funcIExpr.executeMultiReturn(this);
                for (int i = 0; i < returnVals.length; i++) {
                    controlStack.setReturnValue(i, returnVals[i]);
                }
                return;
            }
        }

        for (int i = 0; i < exprs.length; i++) {
            Expression expr = exprs[i];
            BValue returnVal = expr.execute(this);
            controlStack.setReturnValue(i, returnVal);
        }
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        // TODO revisit this logic
        Expression expr = replyStmt.getReplyExpr();
        BMessage bMessage = (BMessage) expr.execute(this);
        bContext.getBalCallback().done(bMessage.value());
    }

    @Override
    public BValue[] visit(FunctionInvocationExpr funcIExpr) {

        // Create the Stack frame
        Function function = funcIExpr.getCallableUnit();

        int sizeOfValueArray = function.getStackFrameSize();
        BValue[] localVals = new BValue[sizeOfValueArray];

        // Get values for all the function arguments
        int valueCounter = populateArgumentValues(funcIExpr.getArgExprs(), localVals);

        // Populate values for Connector declarations
        if (function instanceof BallerinaFunction) {
            BallerinaFunction ballerinaFunction = (BallerinaFunction) function;
            valueCounter = populateConnectorDclValues(ballerinaFunction.getConnectorDcls(), localVals, valueCounter);
        }

        // Create default values for all declared local variables
        for (VariableDcl variableDcl : function.getVariableDcls()) {
            localVals[valueCounter] = variableDcl.getType().getDefaultValue();
            valueCounter++;
        }

        for (Parameter returnParam : function.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getDefaultValue();
            valueCounter++;
        }

        // Create an array in the stack frame to hold return values;
        BValue[] returnVals = new BValue[function.getReturnParameters().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
        // return values and function invocation location;
        SymbolName functionSymbolName = funcIExpr.getCallableUnitName();
        CallableUnitInfo functionInfo = new CallableUnitInfo(functionSymbolName.getName(),
                functionSymbolName.getPkgName(), funcIExpr.getLocation());

        StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
        controlStack.pushFrame(stackFrame);

        // Check whether we are invoking a native function or not.
        if (function instanceof BallerinaFunction) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.getCallableUnitBody().execute(this);
        } else {
            AbstractNativeFunction nativeFunction = (AbstractNativeFunction) function;
            nativeFunction.executeNative(bContext);
        }

        controlStack.popFrame();

        // Setting return values to function invocation expression
        return returnVals;
    }

    @Override
    public BValue[] visit(ActionInvocationExpr actionIExpr) {
        // Create the Stack frame
        Action action = actionIExpr.getCallableUnit();

        BValue[] localVals = new BValue[action.getStackFrameSize()];

        // Create default values for all declared local variables
        int valueCounter = populateArgumentValues(actionIExpr.getArgExprs(), localVals);

        // Populate values for Connector declarations
        if (action instanceof BallerinaAction) {
            BallerinaAction ballerinaAction = (BallerinaAction) action;
            valueCounter = populateConnectorDclValues(ballerinaAction.getConnectorDcls(), localVals, valueCounter);
        }

        // Create default values for all declared local variables
        for (VariableDcl variableDcl : action.getVariableDcls()) {
            localVals[valueCounter] = variableDcl.getType().getDefaultValue();
            valueCounter++;
        }

        for (Parameter returnParam : action.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getDefaultValue();
            valueCounter++;
        }

        // Create an array in the stack frame to hold return values;
        BValue[] returnVals = new BValue[action.getReturnParameters().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression values and
        // return values;
        SymbolName actionSymbolName = actionIExpr.getCallableUnitName();
        CallableUnitInfo actionInfo = new CallableUnitInfo(actionSymbolName.getName(), actionSymbolName.getPkgName(),
                actionIExpr.getLocation());
        StackFrame stackFrame = new StackFrame(localVals, returnVals, actionInfo);
        controlStack.pushFrame(stackFrame);

        // Check whether we are invoking a native action or not.
        if (action instanceof BallerinaAction) {
            BallerinaAction bAction = (BallerinaAction) action;
            bAction.getCallableUnitBody().execute(this);
        } else {
            AbstractNativeAction nativeAction = (AbstractNativeAction) action;
            nativeAction.execute(bContext);
        }

        controlStack.popFrame();

        // Setting return values to function invocation expression
        return returnVals;
    }

    // TODO Check the possibility of removing this from the executor since this is not part of the executor.
    @Override
    public BValue[] visit(ResourceInvocationExpr resourceIExpr) {

        Resource resource = resourceIExpr.getResource();

        ControlStack controlStack = bContext.getControlStack();
        BValue[] valueParams = new BValue[resource.getStackFrameSize()];

        BMessage messageValue = new BMessage(bContext.getCarbonMessage());

        valueParams[0] = messageValue;

        int valueCounter = 1;

        // Populate values for Connector declarations
        valueCounter = populateConnectorDclValues(resource.getConnectorDcls(), valueParams, valueCounter);

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = resource.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            valueParams[valueCounter] = variableDcl.getType().getDefaultValue();
            valueCounter++;
        }

        BValue[] ret = new BValue[1];

        SymbolName resourceSymbolName = resource.getSymbolName();
        CallableUnitInfo resourceInfo = new CallableUnitInfo(resourceSymbolName.getName(),
                resourceSymbolName.getPkgName(), resource.getLocation());

        StackFrame stackFrame = new StackFrame(valueParams, ret, resourceInfo);
        controlStack.pushFrame(stackFrame);

        resource.getResourceBody().execute(this);

        return ret;
    }

    @Override
    public BValue visit(InstanceCreationExpr instanceCreationExpr) {
        return instanceCreationExpr.getType().getDefaultValue();
    }

    @Override
    public BValue visit(UnaryExpression unaryExpr) {
        Expression rExpr = unaryExpr.getRExpr();
        BValueType rValue = (BValueType) rExpr.execute(this);
        //ToDO this has to be improved property in UnaryExpression class since Unary does not need BiFunction
        return unaryExpr.getEvalFunc().apply(null, rValue);
    }

    @Override
    public BValue visit(BinaryExpression binaryExpr) {
        Expression rExpr = binaryExpr.getRExpr();
        BValueType rValue = (BValueType) rExpr.execute(this);

        Expression lExpr = binaryExpr.getLExpr();
        BValueType lValue = (BValueType) lExpr.execute(this);

        return binaryExpr.getEvalFunc().apply(lValue, rValue);
    }

    @Override
    public BValue visit(ArrayMapAccessExpr arrayMapAccessExpr) {

        Expression arrayVarRefExpr = arrayMapAccessExpr.getRExpr();
        BValue collectionValue = arrayVarRefExpr.execute(this);

        Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
        BValue indexValue = indexExpr.execute(this);


        // Check whether this collection access expression is in the left hand of an assignment expression
        // If yes skip setting the value;
        if (!arrayMapAccessExpr.isLHSExpr()) {

            if (arrayMapAccessExpr.getType() != BTypes.MAP_TYPE) {
                // Get the value stored in the index
                if (collectionValue instanceof BArray) {
                    return ((BArray) collectionValue).get(((BInteger) indexValue).intValue());
                } else {
                    return collectionValue;
                }
            } else {
                // Get the value stored in the index
                if (indexValue instanceof BString) {
                    return ((BMap) collectionValue).get(indexValue);
                } else {
                    throw new IllegalStateException("Index of a map should be string type");
                }
            }
        } else {
            throw new IllegalStateException("This branch shouldn't be executed. ");
        }
    }

    @Override
    public BValue visit(ArrayInitExpr arrayInitExpr) {
        Expression[] argExprs = arrayInitExpr.getArgExprs();

        // Creating a new array
        BArray bArray = arrayInitExpr.getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            Expression expr = argExprs[i];
            BValue value = expr.execute(this);
            bArray.add(i, value);
        }

        return bArray;
    }

    @Override
    public BValue visit(MapInitExpr mapInitExpr) {
        Expression[] argExprs = mapInitExpr.getArgExprs();
        // Creating a new map
        BMap<BString, BValue> bMap = new BMap<>();

        for (int i = 0; i < argExprs.length; i++) {
            KeyValueExpression expr = (KeyValueExpression) argExprs[i];
            BString key = new BString(expr.getKey());
            Expression expression = expr.getValueExpression();
            BValue value = expression.execute(this);
            bMap.put(key, value);
        }
        return bMap;
    }

    @Override
    public BValue visit(BacktickExpr backtickExpr) {
        // Evaluate the variable references before creating objects
        String evaluatedString = evaluteBacktickString(backtickExpr);
        if (backtickExpr.getType() == BTypes.JSON_TYPE) {
            return new BJSON(evaluatedString);

        } else {
            return new BXML(evaluatedString);
        }
    }

    @Override
    public BValue visit(VariableRefExpr variableRefExpr) {
        MemoryLocation memoryLocation = variableRefExpr.getMemoryLocation();
        return memoryLocation.execute(this);
    }

    @Override
    public BValue visit(BasicLiteral basicLiteral) {
        return basicLiteral.getBValue();
    }

    @Override
    public BValue visit(LocalVarLocation localVarLocation) {
        int offset = localVarLocation.getStackFrameOffset();
        return controlStack.getValue(offset);
    }

    @Override
    public BValue visit(ConstantLocation constantLocation) {
        int offset = constantLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue visit(ServiceVarLocation serviceVarLocation) {
        int offset = serviceVarLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue visit(ConnectorVarLocation connectorVarLocation) {
        // Fist the get the BConnector object. In an action invocation first argument is always the connector
        BConnector bConnector = (BConnector) controlStack.getValue(0);
        if (bConnector == null) {
            throw new BallerinaException("Connector argument value is null");
        }

        // Now get the connector variable value from the memory block allocated to the BConnector instance.
        return bConnector.getValue(connectorVarLocation.getConnectorMemAddrOffset());
    }


    // Private methods

    private int populateArgumentValues(Expression[] expressions, BValue[] localVals) {
        int i = 0;
        for (Expression arg : expressions) {
            // Evaluate the argument expression
            BValue argValue = arg.execute(this);
            BType argType = arg.getType();

            // Here we need to handle value types differently from reference types
            // Value types need to be cloned before passing ot the function : pass by value.
            // TODO Implement copy-on-write mechanism to improve performance
            if (BTypes.isValueType(argType)) {
                argValue = BValueUtils.clone(argType, argValue);
            }

            // Setting argument value in the stack frame
            localVals[i] = argValue;

            i++;
        }
        return i;
    }

    private int populateConnectorDclValues(ConnectorDcl[] connectorDcls, BValue[] valueParams, int valueCounter) {
        for (ConnectorDcl connectorDcl : connectorDcls) {

            BValue[] connectorMemBlock;
            Connector connector = connectorDcl.getConnector();
            if (connector instanceof AbstractNativeConnector) {

                //TODO Fix Issue#320
                AbstractNativeConnector nativeConnector = ((AbstractNativeConnector) connector).getInstance();
                Expression[] argExpressions = connectorDcl.getArgExprs();
                connectorMemBlock = new BValue[argExpressions.length];

                for (int j = 0; j < argExpressions.length; j++) {
                    connectorMemBlock[j] = argExpressions[j].execute(this);
                }

                nativeConnector.init(connectorMemBlock);
                connector = nativeConnector;

            } else {
                BallerinaConnector ballerinaConnector = (BallerinaConnector) connector;

                int offset = 0;
                connectorMemBlock = new BValue[ballerinaConnector.getSizeOfConnectorMem()];
                for (Expression expr : connectorDcl.getArgExprs()) {
                    connectorMemBlock[offset] = expr.execute(this);
                    offset++;
                }

                // Populate all connector declarations
                offset = populateConnectorDclValues(ballerinaConnector.getConnectorDcls(), connectorMemBlock, offset);

                for (VariableDcl variableDcl : ballerinaConnector.getVariableDcls()) {
                    connectorMemBlock[offset] = variableDcl.getType().getDefaultValue();
                    offset++;
                }
            }

            BConnector connectorValue = new BConnector(connector, connectorMemBlock);
            valueParams[valueCounter] = connectorValue;
            valueCounter++;
        }

        return valueCounter;
    }

    private String evaluteBacktickString(BacktickExpr backtickExpr) {
        String varString = "";
        for (Expression expression : backtickExpr.getExpressionList()) {
            varString = varString + expression.execute(this).stringValue();
        }
        return varString;
    }

    private void assignValueToArrayMapAccessExpr(BValue rValue, ArrayMapAccessExpr lExpr) {
        ArrayMapAccessExpr accessExpr = lExpr;
        if (!(accessExpr.getType() == BTypes.MAP_TYPE)) {
            BArray arrayVal = (BArray) accessExpr.getRExpr().execute(this);
            BInteger indexVal = (BInteger) accessExpr.getIndexExpr().execute(this);
            arrayVal.add(indexVal.intValue(), rValue);

        } else {
            BMap<BString, BValue> mapVal = (BMap<BString, BValue>) accessExpr.getRExpr().execute(this);
            BString indexVal = (BString) accessExpr.getIndexExpr().execute(this);
            mapVal.put(indexVal, rValue);
            // set the type of this expression here
            // accessExpr.setType(rExpr.getType());
        }
    }

    private void assignValueToVarRefExpr(BValue rValue, VariableRefExpr lExpr) {
        VariableRefExpr variableRefExpr = lExpr;
        MemoryLocation memoryLocation = variableRefExpr.getMemoryLocation();
        if (memoryLocation instanceof LocalVarLocation) {
            int stackFrameOffset = ((LocalVarLocation) memoryLocation).getStackFrameOffset();
            controlStack.setValue(stackFrameOffset, rValue);

        } else if (memoryLocation instanceof ServiceVarLocation) {
            int staticMemOffset = ((ServiceVarLocation) memoryLocation).getStaticMemAddrOffset();
            runtimeEnv.getStaticMemory().setValue(staticMemOffset, rValue);

        } else if (memoryLocation instanceof ConnectorVarLocation) {
            // Fist the get the BConnector object. In an action invocation first argument is always the connector
            BConnector bConnector = (BConnector) controlStack.getValue(0);
            if (bConnector == null) {
                throw new BallerinaException("Connector argument value is null");
            }

            int connectorMemOffset = ((ConnectorVarLocation) memoryLocation).getConnectorMemAddrOffset();
            bConnector.setValue(connectorMemOffset, rValue);
        }
    }
}
