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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.ParserException;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.DivideExpr;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.KeyValueExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
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
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code BLangModelBuilder} provides an high-level API to create Ballerina language object model.
 *
 * @since 1.0.0
 */
public class BLangModelBuilder {

    private String pkgName;

    private BallerinaFile.BFileBuilder bFileBuilder = new BallerinaFile.BFileBuilder();

    // Builds connectors and services.
    private CallableUnitGroupBuilder currentCUGroupBuilder;

    // Builds functions, actions and resources.
    private CallableUnitBuilder currentCUBuilder;

    private Stack<Annotation.AnnotationBuilder> annotationBuilderStack = new Stack<>();
    private Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();

    private Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();
    private Queue<BType> typeQueue = new LinkedList<>();
    private Stack<String> pkgNameStack = new Stack<>();
    private Stack<SymbolName> symbolNameStack = new Stack<>();
    private Stack<Expression> exprStack = new Stack<>();

    private Stack<KeyValueExpression> keyValueStack = new Stack<>();

    private static final Logger log = LoggerFactory.getLogger(BLangModelBuilder.class);

    // Holds ExpressionLists required for return statements, function/action invocations and connector declarations
    private Stack<List<Expression>> exprListStack = new Stack<>();

    private Stack<List<Annotation>> annotationListStack = new Stack<>();

    private Stack<List<KeyValueExpression>> mapInitKeyValueListStack = new Stack<>();

    public BallerinaFile build() {
        return bFileBuilder.build();
    }

    // Identifiers

    public void createSymbolName(String name) {
        if (pkgNameStack.isEmpty()) {
            symbolNameStack.push(new SymbolName(name));
        } else {
            symbolNameStack.push(new SymbolName(name, pkgNameStack.pop()));
        }
    }

    public void createSymbolName(String connectorName, String actionName) {
        SymbolName symbolName;
        if (pkgNameStack.isEmpty()) {
            symbolName = new SymbolName(actionName);
        } else {
            symbolName = new SymbolName(actionName, pkgNameStack.pop());
        }

        symbolName.setConnectorName(connectorName);
        symbolNameStack.push(symbolName);
    }

    // Packages and import packages

    public void createPackageName(String pkgName) {
        pkgNameStack.push(pkgName);
    }

    public void createPackageDcl() {
        pkgName = getPkgName();
        bFileBuilder.setPkgName(pkgName);
    }

    public void addImportPackage(String pkgName, Position sourceLocation) {
        String pkgPath = getPkgName();
        if (pkgName != null) {
            bFileBuilder.addImportPackage(new ImportPackage(pkgPath, pkgName, sourceLocation));
        } else {
            bFileBuilder.addImportPackage(new ImportPackage(pkgPath, sourceLocation));
        }
    }

    // Annotations

    public void createInstanceCreaterExpr(String typeName, Position sourceLocation) {
        InstanceCreationExpr expression = new InstanceCreationExpr(null);
        BType type = BTypes.getType(typeName);

        if (type == null) {
            throw new ParserException("Unknown type: " + typeName + " in " + sourceLocation.getFileName() + ":" +
                    sourceLocation.getLine());
        }

        expression.setType(type);
        expression.setLocation(sourceLocation);
        exprStack.push(expression);
    }

    public void startAnnotation() {
        annotationBuilderStack.push(new Annotation.AnnotationBuilder());
    }

    public void createAnnotationKeyValue(String key) {
        //        // Assuming the annotation value is a string literal
        //        String value = exprStack.pop().getBValueRef().getString();
        //
        //        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.peek();
        //        annotationBuilder.addKeyValuePair(new Identifier(key), value);

        log.warn("Warning: Key/Value pairs in annotations are not supported");
    }

    public void endAnnotation(String name, boolean valueAvailable, Position sourceLocation) {
        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.pop();
        annotationBuilder.setName(new SymbolName(name));

        if (valueAvailable) {
            Expression expr = exprStack.pop();

            // Assuming the annotation value is a string literal
            if (expr instanceof BasicLiteral && expr.getType() == BTypes.STRING_TYPE) {
                String value = ((BasicLiteral) expr).getBValue().stringValue();
                annotationBuilder.setValue(value);
            } else {
                throw new RuntimeException("Annotations with key/value pars are not support at the moment" + " in " +
                        sourceLocation.getFileName() + ":" + sourceLocation.getLine());
            }
        }

        List<Annotation> annotationList = annotationListStack.peek();
        Annotation annotation = annotationBuilder.build();
        annotation.setLocation(sourceLocation);
        annotationList.add(annotation);
    }

    // Function parameters and types

    /**
     * Create a function parameter and a corresponding variable reference expression
     * <p/>
     * Set the even function to get the value from the function arguments with the correct index.
     * Store the reference in the symbol table.
     *
     * @param paramName name of the function parameter
     */
    public void createParam(String paramName, Position sourceLocation) {
        SymbolName paramNameId = new SymbolName(paramName);
        BType paramType = typeQueue.remove();
        Parameter param = new Parameter(paramType, paramNameId, sourceLocation);

        if (currentCUBuilder != null) {
            // Add the parameter to callableUnitBuilder.
            currentCUBuilder.addParameter(param);
        } else {
            currentCUGroupBuilder.addParameter(param);
        }
    }

    public void createType(String typeName, Position sourceLocation) {
        BType type = BTypes.getType(typeName);
        if (type == null) {
            throw new ParserException(sourceLocation.getFileName() + ":" + sourceLocation.getLine() +
                    ": unsupported type '" + typeName + "'");
        }
        typeQueue.add(type);
    }

    public void createArrayType(String typeName, Position sourceLocation) {
        BType type = BTypes.getArrayType(typeName);
        /*if (type == null) {
            throw new ParserException("Unsupported type: " + typeName + " in " + 
                    sourceLocation.getFileName() + ":" + sourceLocation.getLine());
        }*/
        typeQueue.add(type);
    }

    public void registerConnectorType(String typeName) {
        //TODO: We might have to do this through a symbol table in the future
        BTypes.addConnectorType(typeName);
    }

    public void createReturnTypes(Position sourceLocation) {
        while (!typeQueue.isEmpty()) {
            BType paramType = typeQueue.remove();
            Parameter param = new Parameter(paramType, null, sourceLocation);
            currentCUBuilder.addReturnParameter(param);
        }
    }

    public void createNamedReturnParams(String paramName, Position sourceLocation) {
        SymbolName paramNameId = new SymbolName(paramName);
        BType paramType = typeQueue.remove();

        Parameter param = new Parameter(paramType, paramNameId, sourceLocation);
        currentCUBuilder.addReturnParameter(param);
    }

    // Variable declarations, reference expressions

    public void createConstant(String constName, Position sourceLocation) {
        SymbolName symbolName = new SymbolName(constName);
        BType type = typeQueue.remove();

        Const.ConstBuilder builder = new Const.ConstBuilder();
        builder.setType(type);
        builder.setSymbolName(symbolName);
        builder.setValueExpr(exprStack.pop());

        Const constant = builder.build();
        constant.setLocation(sourceLocation);
        bFileBuilder.addConst(constant);
    }

    public void createVariableDcl(String varName, Position sourceLocation) {
        // Create a variable declaration
        SymbolName localVarId = new SymbolName(varName);
        BType localVarType = typeQueue.remove();
        VariableDcl variableDcl = new VariableDcl(localVarType, localVarId);
        variableDcl.setLocation(sourceLocation);

        // Add this variable declaration to the current callable unit or callable unit group
        if (currentCUBuilder != null) {
            // This connector declaration should added to the relevant function/action or resource
            currentCUBuilder.addVariableDcl(variableDcl);
        } else {
            currentCUGroupBuilder.addVariableDcl(variableDcl);
        }

    }

    public void createConnectorDcl(String varName, Position sourceLocation) {
        // Here we build the object model for the following line

        // Here we need to pop the symbolName stack twice as the connector name appears twice in the declaration.
        if (symbolNameStack.size() < 2) {
            IllegalStateException ex = new IllegalStateException("symbol stack size should be " +
                    "greater than or equal to two");
            throw new ParserException("Failed to parse connector declaration" + varName + " in " +
                    sourceLocation.getFileName() + ":" + sourceLocation.getLine(), ex);
        }

        symbolNameStack.pop();
        SymbolName cSymName = symbolNameStack.pop();
        List<Expression> exprList = exprListStack.pop();

        ConnectorDcl.ConnectorDclBuilder builder = new ConnectorDcl.ConnectorDclBuilder();
        builder.setConnectorName(cSymName);
        builder.setVarName(new SymbolName(varName));
        builder.setExprList(exprList);

        ConnectorDcl connectorDcl = builder.build();
        connectorDcl.setLocation(sourceLocation);
        if (currentCUBuilder != null) {
            // This connector declaration should added to the relevant function/action or resource
            currentCUBuilder.addConnectorDcl(connectorDcl);
        } else {
            currentCUGroupBuilder.addConnectorDcl(connectorDcl);
        }
    }

    /**
     * Create variable reference expression
     * <p/>
     * There are three types of variables references as per the grammar file.
     * 1) Simple variable references. a, b, index etc
     * 2) Map or array access a[1], m["key"]
     * 3) Struct field access  Person.name
     */
    public void createVarRefExpr(String varName, Position sourceLocation) {
        SymbolName symName = new SymbolName(varName);

        VariableRefExpr variableRefExpr = new VariableRefExpr(symName);
        variableRefExpr.setLocation(sourceLocation);
        exprStack.push(variableRefExpr);
    }

    public void createMapArrayVarRefExpr(String varName, Position sourceLocation) {
        SymbolName symName = new SymbolName(varName);
        Expression indexExpr = exprStack.pop();
        VariableRefExpr arrayVarRefExpr = new VariableRefExpr(symName);
        arrayVarRefExpr.setLocation(sourceLocation);

        ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder = new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();
        builder.setVarName(symName);
        builder.setIndexExpr(indexExpr);
        builder.setArrayMapVarRefExpr(arrayVarRefExpr);

        ArrayMapAccessExpr accessExpr = builder.build();
        accessExpr.setLocation(sourceLocation);
        exprStack.push(accessExpr);
    }

    // Expressions

    public void createBinaryExpr(String opStr, Position sourceLocation) {
        Expression rExpr = exprStack.pop();
        Expression lExpr = exprStack.pop();

        BinaryExpression expr;
        switch (opStr) {
            case "+":
                expr = new AddExpression(lExpr, rExpr, sourceLocation);
                break;

            case "-":
                expr = new SubtractExpression(lExpr, rExpr, sourceLocation);
                break;

            case "*":
                expr = new MultExpression(lExpr, rExpr, sourceLocation);
                break;

            case "/":
                expr = new DivideExpr(lExpr, rExpr, sourceLocation);
                break;

            case "&&":
                expr = new AndExpression(lExpr, rExpr, sourceLocation);
                break;

            case "||":
                expr = new OrExpression(lExpr, rExpr, sourceLocation);
                break;

            case "==":
                expr = new EqualExpression(lExpr, rExpr, sourceLocation);
                break;

            case "!=":
                expr = new NotEqualExpression(lExpr, rExpr, sourceLocation);
                break;

            case ">=":
                expr = new GreaterEqualExpression(lExpr, rExpr, sourceLocation);
                break;

            case ">":
                expr = new GreaterThanExpression(lExpr, rExpr, sourceLocation);
                break;

            case "<":
                expr = new LessThanExpression(lExpr, rExpr, sourceLocation);
                break;

            case "<=":
                expr = new LessEqualExpression(lExpr, rExpr, sourceLocation);
                break;

            default:
                throw new ParserException("Unsupported operator '" + opStr + "' in " +
                        sourceLocation.getFileName() + ":" + sourceLocation.getLine());
        }

        exprStack.push(expr);
    }

    public void createUnaryExpr(String op, Position sourceLocation) {
        Expression rExpr = exprStack.pop();

        UnaryExpression expr;
        switch (op) {
            case "+":
                expr = new UnaryExpression(Operator.ADD, rExpr, sourceLocation);
                break;

            case "-":
                expr = new UnaryExpression(Operator.SUB, rExpr, sourceLocation);
                break;

            case "!":
                expr = new UnaryExpression(Operator.NOT, rExpr, sourceLocation);
                break;

            default:
                throw new ParserException("Unsupported operator '" + op + "' in " +
                        sourceLocation.getFileName() + ":" + sourceLocation.getLine());
        }

        exprStack.push(expr);
    }

    public void createBacktickExpr(String stringContent, Position sourceLocation) {
        String templateStr = getValueWithinBackquote(stringContent);
        BacktickExpr.BacktickExprBuilder builder = new BacktickExpr.BacktickExprBuilder();
        builder.setTemplateStr(templateStr);
        BacktickExpr expr = builder.build();
        expr.setLocation(sourceLocation);
        exprStack.push(expr);
    }

    public void startExprList() {
        exprListStack.push(new ArrayList<>());
    }

    public void endExprList(int exprCount) {
        List<Expression> exprList = exprListStack.peek();
        addExprToList(exprList, exprCount);
    }

    public void createFunctionInvocationExpr(Position sourceLocation) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setExpressionList(exprListStack.pop());
        cIExprBuilder.setName(symbolNameStack.pop());

        FunctionInvocationExpr invocationExpr = cIExprBuilder.buildFuncInvocExpr();
        invocationExpr.setLocation(sourceLocation);
        exprStack.push(invocationExpr);
    }

    public void createActionInvocationExpr(Position sourceLocation) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setExpressionList(exprListStack.pop());
        cIExprBuilder.setName(symbolNameStack.pop());

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();
        invocationExpr.setLocation(sourceLocation);
        exprStack.push(invocationExpr);
    }

    public void createArrayInitExpr(Position sourceLocation) {
        ArrayInitExpr.ArrayInitExprBuilder builder = new ArrayInitExpr.ArrayInitExprBuilder();

        if (!exprListStack.isEmpty()) {
            List<Expression> argList = exprListStack.pop();
            builder.setArgList(argList);
        }

        ArrayInitExpr arrayInitExpr = builder.build();
        arrayInitExpr.setLocation(sourceLocation);
        exprStack.push(arrayInitExpr);
    }

    public void createMapInitExpr(Position sourceLocation) {
        MapInitExpr.MapInitExprBuilder builder = new MapInitExpr.MapInitExprBuilder();

        if (!mapInitKeyValueListStack.isEmpty()) {
            List<KeyValueExpression> argList = mapInitKeyValueListStack.pop();
            builder.setArgList(argList);
        }

        MapInitExpr mapInitExpr = builder.build();
        mapInitExpr.setLocation(sourceLocation);
        exprStack.push(mapInitExpr);
    }

    public void startMapInitKeyValue() {
        mapInitKeyValueListStack.push(new ArrayList<>());
    }

    public void endMapInitKeyValue(int exprCount) {
        List<KeyValueExpression> keyValueList = mapInitKeyValueListStack.peek();
        addKeyValueToList(keyValueList, exprCount);
    }

    public void createMapInitKeyValue(String key, Position sourceLocation) {
        if (!exprStack.isEmpty()) {
            Expression currentExpression = exprStack.pop();
            keyValueStack.push(new KeyValueExpression(key, currentExpression, sourceLocation));
        } else {
            keyValueStack.push(new KeyValueExpression(key, null, sourceLocation));
        }


    }

    // Functions, Actions and Resources

    public void startCallableUnitBody() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endCallableUnitBody() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();

        currentCUBuilder.setBody(blockStmt);
    }

    public void startCallableUnit() {
        currentCUBuilder = new CallableUnitBuilder();
        annotationListStack.push(new ArrayList<>());
    }

    public void createFunction(String name, boolean isPublic, Position sourceLocation, int position) {
        currentCUBuilder.setName(new SymbolName(name, pkgName));
        currentCUBuilder.setPublic(isPublic);
        currentCUBuilder.setPosition(sourceLocation);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaFunction function = currentCUBuilder.buildFunction();
        function.setRelativePosition(position);
        bFileBuilder.addFunction(function);

        currentCUBuilder = null;
    }

    public void createResource(String name, Position sourceLocation) {
        currentCUBuilder.setName(new SymbolName(name, pkgName));
        currentCUBuilder.setPosition(sourceLocation);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUBuilder::addAnnotation);

        Resource resource = currentCUBuilder.buildResource();
        currentCUGroupBuilder.addResource(resource);

        currentCUBuilder = null;
    }

    public void createAction(String name, Position sourceLocation) {
        currentCUBuilder.setName(new SymbolName(name, pkgName));
        currentCUBuilder.setPosition(sourceLocation);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaAction action = currentCUBuilder.buildAction();
        currentCUGroupBuilder.addAction(action);

        currentCUBuilder = null;
    }

    // Services and Connectors

    public void startCallableUnitGroup() {
        currentCUGroupBuilder = new CallableUnitGroupBuilder();
        annotationListStack.push(new ArrayList<>());
    }

    public void createService(String name, Position sourceLocation, int position) {
        currentCUGroupBuilder.setName(new SymbolName(name, pkgName));
        currentCUGroupBuilder.setLocation(sourceLocation);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        Service service = currentCUGroupBuilder.buildService();
        service.setRelativePosition(position);
        bFileBuilder.addService(service);

        currentCUGroupBuilder = null;
    }

    public void createConnector(String name, Position sourceLocation, int position) {
        currentCUGroupBuilder.setName(new SymbolName(name, pkgName));
        currentCUGroupBuilder.setLocation(sourceLocation);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        BallerinaConnector connector = currentCUGroupBuilder.buildConnector();
        connector.setRelativePosition(position);
        bFileBuilder.addConnector(connector);

        currentCUGroupBuilder = null;
    }

    // Statements

    public void createAssignmentExpr(Position sourceLocation) {
        Expression rExpr = exprStack.pop();
        Expression lExpr = exprStack.pop();

        AssignStmt assignStmt = new AssignStmt(lExpr, rExpr);
        assignStmt.setLocation(sourceLocation);
        addToBlockStmt(assignStmt);
    }

    public void createReturnStmt(Position sourceLocation) {
        ReturnStmt.ReturnStmtBuilder returnStmtBuilder = new ReturnStmt.ReturnStmtBuilder();

        // Get the expression list from the expression list stack
        if (!exprListStack.isEmpty()) {
            // Return statement with empty expression list.
            // Just a return statement
            returnStmtBuilder.setExpressionList(exprListStack.pop());
        }

        ReturnStmt returnStmt = returnStmtBuilder.build();
        returnStmt.setLocation(sourceLocation);
        addToBlockStmt(returnStmt);
    }

    public void createReplyStmt(Position sourceLocation) {
        ReplyStmt.ReplyStmtBuilder replyStmtBuilder = new ReplyStmt.ReplyStmtBuilder();
        replyStmtBuilder.setExpression(exprStack.pop());
        ReplyStmt replyStmt = replyStmtBuilder.build();
        replyStmt.setLocation(sourceLocation);
        addToBlockStmt(replyStmt);
    }

    public void startWhileStmt() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endWhileStmt(Position sourceLocation) {
        // Create a while statement builder
        WhileStmt.WhileStmtBuilder whileStmtBuilder = new WhileStmt.WhileStmtBuilder();

        // Get the expression at the top of the expression stack and set it as the while condition
        whileStmtBuilder.setCondition(exprStack.pop());

        // Get the statement block at the top of the block statement stack and set as the while body.
        whileStmtBuilder.setWhileBody(blockStmtBuilderStack.pop().build());

        // Add the while statement to the statement block which is at the top of the stack.
        WhileStmt whileStmt = whileStmtBuilder.build();
        whileStmt.setLocation(sourceLocation);
        blockStmtBuilderStack.peek().addStmt(whileStmt);
    }

    public void startIfElseStmt() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
        ifElseStmtBuilderStack.push(new IfElseStmt.IfElseStmtBuilder());
    }

    public void startElseIfClause() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endElseIfClause(Position sourceLocation) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt elseIfStmt = blockStmtBuilder.build();
        elseIfStmt.setLocation(sourceLocation);
        ifElseStmtBuilder.addElseIfBlock(exprStack.pop(), elseIfStmt);
    }

    public void startElseClause() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endElseClause(Position sourceLocation) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt elseStmt = blockStmtBuilder.build();
        elseStmt.setLocation(sourceLocation);
        ifElseStmtBuilder.setElseBody(elseStmt);
    }

    public void endIfElseStmt(Position sourceLocation) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.pop();
        ifElseStmtBuilder.setIfCondition(exprStack.pop());

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        ifElseStmtBuilder.setThenBody(blockStmtBuilder.build());

        IfElseStmt ifElseStmt = ifElseStmtBuilder.build();
        ifElseStmt.setLocation(sourceLocation);
        addToBlockStmt(ifElseStmt);
    }

    public void createFunctionInvocationStmt(Position sourceLocation) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setExpressionList(exprListStack.pop());
        cIExprBuilder.setName(symbolNameStack.pop());

        FunctionInvocationExpr invocationExpr = cIExprBuilder.buildFuncInvocExpr();
        invocationExpr.setLocation(sourceLocation);

        FunctionInvocationStmt.FunctionInvokeStmtBuilder stmtBuilder =
                new FunctionInvocationStmt.FunctionInvokeStmtBuilder();
        stmtBuilder.setFunctionInvocationExpr(invocationExpr);
        FunctionInvocationStmt functionInvocationStmt = stmtBuilder.build();

        blockStmtBuilderStack.peek().addStmt(functionInvocationStmt);
    }

    public void createActionInvocationStmt(Position sourceLocation) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setExpressionList(exprListStack.pop());
        cIExprBuilder.setName(symbolNameStack.pop());

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();
        invocationExpr.setLocation(sourceLocation);

        ActionInvocationStmt.ActionInvocationStmtBuilder stmtBuilder =
                new ActionInvocationStmt.ActionInvocationStmtBuilder();
        stmtBuilder.setFunctionInvocationExpr(invocationExpr);
        ActionInvocationStmt actionInvocationStmt = stmtBuilder.build();

        blockStmtBuilderStack.peek().addStmt(actionInvocationStmt);
    }

    // Literal Values

    public void createIntegerLiteral(String value, Position sourceLocation) {
        BValueType bValue = new BInteger(Integer.parseInt(value));
        createLiteral(bValue, BTypes.INT_TYPE, sourceLocation);
    }

    public void createLongLiteral(String value, Position sourceLocation) {
        BValueType bValue = new BLong(Long.parseLong(value));
        createLiteral(bValue, BTypes.LONG_TYPE, sourceLocation);
    }

    public void createFloatLiteral(String value, Position sourceLocation) {
        BValueType bValue = new BFloat(Float.parseFloat(value));
        createLiteral(bValue, BTypes.FLOAT_TYPE, sourceLocation);
    }

    public void createDoubleLiteral(String value, Position sourceLocation) {
        BValueType bValue = new BDouble(Double.parseDouble(value));
        createLiteral(bValue, BTypes.DOUBLE_TYPE, sourceLocation);
    }

    public void createStringLiteral(String value, Position sourceLocation) {
        BValueType bValue = new BString(value);
        createLiteral(bValue, BTypes.STRING_TYPE, sourceLocation);
    }

    public void createBooleanLiteral(String value, Position sourceLocation) {
        BValueType bValue = new BBoolean(Boolean.parseBoolean(value));
        createLiteral(bValue, BTypes.BOOLEAN_TYPE, sourceLocation);
    }

    public void createNullLiteral(String value, Position sourceLocation) {
        throw new RuntimeException("Null values are not yet supported in Ballerina in " + sourceLocation.getFileName()
                + ":" + sourceLocation.getLine());
    }

    // Private methods

    private void addToBlockStmt(Statement stmt) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.peek();
        blockStmtBuilder.addStmt(stmt);
    }

    private void createLiteral(BValueType bValueType, BType type, Position sourceLocation) {
        BasicLiteral basicLiteral = new BasicLiteral(bValueType);
        basicLiteral.setType(type);
        basicLiteral.setLocation(sourceLocation);
        exprStack.push(basicLiteral);
    }

    /**
     * @param exprList List<Expression>
     * @param n        number of expression to be added the given list
     */
    private void addExprToList(List<Expression> exprList, int n) {

        if (exprStack.isEmpty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
        }

        if (n == 1) {
            Expression expr = exprStack.pop();
            exprList.add(expr);
        } else {
            Expression expr = exprStack.pop();
            addExprToList(exprList, n - 1);
            exprList.add(expr);
        }
    }

    /**
     * @param keyValueDataHolderList List<KeyValueDataHolder>
     * @param n                      number of expression to be added the given list
     */
    private void addKeyValueToList(List<KeyValueExpression> keyValueDataHolderList, int n) {

        if (keyValueStack.isEmpty()) {
            throw new IllegalStateException("KeyValue stack cannot be empty in processing a KeyValueList");
        }

        if (n == 1) {
            KeyValueExpression keyValue = keyValueStack.pop();
            keyValueDataHolderList.add(keyValue);
        } else {
            KeyValueExpression keyValue = keyValueStack.pop();
            addKeyValueToList(keyValueDataHolderList, n - 1);
            keyValueDataHolderList.add(keyValue);
        }
    }

    private String getPkgName() {
        if (pkgNameStack.isEmpty()) {
            throw new IllegalStateException("Package name stack is empty");
        }

        return pkgNameStack.pop();
    }

    /**
     * return value within double quotes
     *
     * @param inputString string with double quotes
     * @return value
     */
    private static String getValueWithinBackquote(String inputString) {
        Pattern p = Pattern.compile("`([^`]*)`");
        Matcher m = p.matcher(inputString);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

}
