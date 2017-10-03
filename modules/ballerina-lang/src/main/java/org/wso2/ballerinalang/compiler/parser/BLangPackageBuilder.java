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
package org.wso2.ballerinalang.compiler.parser;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.WorkerNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeValueNode;
import org.ballerinalang.model.tree.expressions.ConnectorInitNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.model.tree.expressions.XMLLiteralNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransform;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class builds the package AST of a Ballerina source file.
 *
 * @since 0.94
 */
public class BLangPackageBuilder {

    private CompilationUnitNode compUnit;

    private Stack<BLangNameReference> nameReferenceStack = new Stack<>();

    private Stack<TypeNode> typeNodeStack = new Stack<>();

    private Stack<List<TypeNode>> typeNodeListStack = new Stack<>();

    private Stack<BlockNode> blockNodeStack = new Stack<>();

    private Stack<VariableNode> varStack = new Stack<>();

    private Stack<List<VariableNode>> varListStack = new Stack<>();

    private Stack<InvokableNode> invokableNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();

    private Stack<List<ExpressionNode>> exprNodeListStack = new Stack<>();

    private Stack<BLangRecordLiteral> recordLiteralNodes = new Stack<>();

    private Stack<BLangTryCatchFinally> tryCatchFinallyNodesStack = new Stack<>();

    private Stack<StructNode> structStack = new Stack<>();

    private Stack<EnumNode> enumStack = new Stack<>();

    private Stack<IdentifierNode> identifierStack = new Stack<>();

    private Stack<ConnectorNode> connectorNodeStack = new Stack<>();

    private Stack<List<ActionNode>> actionNodeStack = new Stack<>();

    private Stack<AnnotationNode> annotationStack = new Stack<>();

    private Stack<AnnotationAttachmentAttributeValueNode> annotAttribValStack = new Stack<>();

    private Stack<AnnotationAttachmentNode> annotAttachmentStack = new Stack<>();

    private Stack<IfNode> ifElseStatementStack = new Stack<>();

    private Stack<TransactionNode> transactionNodeStack = new Stack<>();

    private Stack<ForkJoinNode> forkJoinNodesStack = new Stack<>();

    private Stack<ServiceNode> serviceNodeStack = new Stack<>();

    private Stack<XMLAttributeNode> xmlAttributeNodeStack = new Stack<>();

    private Stack<ConnectorInitNode> connectorInitNodeStack = new Stack<>();

    private Stack<BLangAnnotationAttachmentPoint> attachmentPointStack = new Stack<>();

    protected int lambdaFunctionCount = 0;

    public BLangPackageBuilder(CompilationUnitNode compUnit) {
        this.compUnit = compUnit;
    }

    public void addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint attachPoint,
                               String pkgPath) {
        BLangAnnotationAttachmentPoint attachmentPoint =
                new BLangAnnotationAttachmentPoint(attachPoint, pkgPath);
        attachmentPointStack.push(attachmentPoint);
    }

    public void addValueType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.addWS(ws);
        typeNode.pos = pos;
        typeNode.typeKind = (TreeUtils.stringToTypeKind(typeName));

        addType(typeNode);
    }

    public void addArrayType(DiagnosticPos pos, Set<Whitespace> ws, int dimensions) {
        BLangType eType = (BLangType) this.typeNodeStack.pop();
        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.addWS(ws);
        arrayTypeNode.pos = pos;
        arrayTypeNode.elemtype = eType;
        arrayTypeNode.dimensions = dimensions;

        addType(arrayTypeNode);
    }

    public void addUserDefineType(Set<Whitespace> ws) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.pos = nameReference.pos;
        userDefinedType.addWS(ws);
        userDefinedType.addWS(nameReference.ws);
        userDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        userDefinedType.typeName = (BLangIdentifier) nameReference.name;

        addType(userDefinedType);
    }

    public void addBuiltInReferenceType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;
        refType.addWS(ws);
        addType(refType);
    }

    public void addConstraintType(DiagnosticPos pos, String typeName) {
        // TODO : Fix map<int> format.
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType constraintType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        constraintType.pos = pos;
        constraintType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        constraintType.typeName = (BLangIdentifier) nameReference.name;

        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = refType;
        constrainedType.constraint = constraintType;
        constrainedType.pos = pos;

        addType(constrainedType);
    }

    public void addFunctionType(DiagnosticPos pos, boolean paramsAvail, boolean paramsTypeOnly,
                                boolean retParamsAvail, boolean retParamTypeOnly, boolean returnsKeywordExists) {
        // TODO : Fix function main ()(boolean , function(string x)(float, int)){} issue
        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) TreeBuilder.createFunctionTypeNode();
        functionTypeNode.pos = pos;
        functionTypeNode.returnsKeywordExists = returnsKeywordExists;

        if (retParamsAvail) {
            if (retParamTypeOnly) {
                functionTypeNode.returnParamTypeNodes.addAll(this.typeNodeListStack.pop());
            } else {
                this.varListStack.pop().forEach(v -> functionTypeNode.returnParamTypeNodes.add(v.getTypeNode()));
            }
        }
        if (paramsAvail) {
            if (paramsTypeOnly) {
                functionTypeNode.paramTypeNodes.addAll(this.typeNodeListStack.pop());
            } else {
                this.varListStack.pop().forEach(v -> functionTypeNode.paramTypeNodes.add(v.getTypeNode()));
            }
        }

        addType(functionTypeNode);
    }

    private void addType(TypeNode typeNode) {
        this.typeNodeStack.push(typeNode);
    }

    public void addNameReference(DiagnosticPos currentPos, Set<Whitespace> ws, String pkgName, String name) {
        IdentifierNode pkgNameNode = createIdentifier(pkgName);
        IdentifierNode nameNode = createIdentifier(name);
        nameReferenceStack.push(new BLangNameReference(currentPos, ws, pkgNameNode, nameNode));
    }

    public void startVarList() {
        this.varListStack.push(new ArrayList<>());
    }

    public void startFunctionDef() {
        FunctionNode functionNode = TreeBuilder.createFunctionNode();
        attachAnnotations(functionNode);
        this.invokableNodeStack.push(functionNode);
    }

    public void startBlock() {
        this.blockNodeStack.push(TreeBuilder.createBlockNode());
    }

    private IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    public void addVarToStruct(DiagnosticPos pos,
                               Set<Whitespace> ws,
                               String identifier,
                               boolean exprAvailable,
                               int annotCount) {

        Set<Whitespace> wsForSemiColon = removeLast(ws);
        BLangStruct structNode = (BLangStruct) this.structStack.peek();
        structNode.addWS(wsForSemiColon);
        addVar(pos, ws, identifier, exprAvailable, annotCount);
    }

    public void addVarToAnnotation(DiagnosticPos pos,
                                   Set<Whitespace> ws,
                                   String identifier,
                                   boolean exprAvailable,
                                   int annotCount) {

        Set<Whitespace> wsForSemiColon = removeLast(ws);
        AnnotationNode annotNode = this.annotationStack.peek();
        annotNode.addWS(wsForSemiColon);
        addVar(pos, ws, identifier, exprAvailable, annotCount);
    }


    public void addVar(DiagnosticPos pos,
                       Set<Whitespace> ws,
                       String identifier,
                       boolean exprAvailable,
                       int annotCount) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        attachAnnotations(var, annotCount);
        var.pos = pos;
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }
    }

    public void endCallableUnitSignature(Set<Whitespace> ws, String identifier, boolean paramsAvail,
                                         boolean retParamsAvail, boolean retParamTypeOnly) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        invNode.setName(this.createIdentifier(identifier));
        invNode.addWS(ws);
        if (retParamsAvail) {
            if (retParamTypeOnly) {
                this.typeNodeListStack.pop().forEach(e -> {
                    VariableNode var = TreeBuilder.createVariableNode();
                    var.setTypeNode(e);

                    // Create an empty name node
                    IdentifierNode nameNode = TreeBuilder.createIdentifierNode();
                    nameNode.setValue("");
                    var.setName(nameNode);
                    invNode.addReturnParameter(var);
                });
            } else {
                this.varListStack.pop().forEach(invNode::addReturnParameter);
            }
        }
        if (paramsAvail) {
            this.varListStack.pop().forEach(invNode::addParameter);
        }
    }

    public void startLambdaFunctionDef() {
        startFunctionDef();
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.setName(createIdentifier("$lambda$" + lambdaFunctionCount++));
        lambdaFunction.addFlag(Flag.LAMBDA);
    }

    public void addLambdaFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean paramsAvail, boolean retParamsAvail,
                                     boolean retParamTypeOnly) {
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.pos = pos;
        endCallableUnitSignature(ws, lambdaFunction.getName().value, paramsAvail, retParamsAvail, retParamTypeOnly);
        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = lambdaFunction;
        lambdaExpr.pos = pos;
        addExpressionNode(lambdaExpr);
        // TODO: is null correct here
        endFunctionDef(pos, null, false, false, true, false);
    }

    public void addVariableDefStatement(DiagnosticPos pos,
                                        Set<Whitespace> ws,
                                        String identifier,
                                        boolean exprAvailable) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        Set<Whitespace> wsOfSemiColon = removeLast(ws);
        var.pos = pos;
        var.addWS(ws);
        var.setName(this.createIdentifier(identifier));
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }

        BLangVariableDef varDefNode = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        varDefNode.addWS(wsOfSemiColon);
        addStmtToCurrentBlock(varDefNode);
    }

    public void addConnectorInitExpression(DiagnosticPos pos, Set<Whitespace> ws, boolean exprAvailable) {
        BLangConnectorInit connectorInitNode = (BLangConnectorInit) TreeBuilder.createConnectorInitNode();
        connectorInitNode.pos = pos;
        connectorInitNode.addWS(ws);
        connectorInitNode.connectorType = (BLangUserDefinedType) typeNodeStack.pop();
        if (exprAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> connectorInitNode.argsExpr.add((BLangExpression) exprNode));
        }
        ConnectorInitNode previous = null;
        while (!connectorInitNodeStack.empty()) {
            connectorInitNode.filterConnectors.add(0, connectorInitNodeStack.pop());
        }
        this.addExpressionNode(connectorInitNode);
    }

    public void addFilterConnectorInitExpression(DiagnosticPos pos, Set<Whitespace> ws, boolean exprAvailable) {
        BLangConnectorInit connectorInitNode = (BLangConnectorInit) TreeBuilder.createConnectorInitNode();
        connectorInitNode.pos = pos;
        connectorInitNode.addWS(ws);
        connectorInitNode.connectorType = (BLangUserDefinedType) typeNodeStack.pop();
        if (exprAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> connectorInitNode.argsExpr.add((BLangExpression) exprNode));
        }
        this.connectorInitNodeStack.push(connectorInitNode);
    }

    private void addStmtToCurrentBlock(StatementNode statement) {
        this.blockNodeStack.peek().addStatement(statement);
    }

    public void startTryCatchFinallyStmt() {
        this.tryCatchFinallyNodesStack.push((BLangTryCatchFinally) TreeBuilder.createTryCatchFinallyNode());
        startBlock();
    }

    public void addTryClause(DiagnosticPos pos) {
        BLangBlockStmt tryBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        tryBlock.pos = pos;
        tryCatchFinallyNodesStack.peek().tryBody = tryBlock;
    }

    public void startCatchClause() {
        startBlock();
    }

    public void addCatchClause(DiagnosticPos poc, Set<Whitespace> ws, String paramName) {
        BLangVariable variableNode = (BLangVariable) TreeBuilder.createVariableNode();
        variableNode.typeNode = (BLangType) this.typeNodeStack.pop();
        variableNode.name = (BLangIdentifier) createIdentifier(paramName);
        variableNode.pos = variableNode.typeNode.pos;

        BLangCatch catchNode = (BLangCatch) TreeBuilder.createCatchNode();
        catchNode.pos = poc;
        catchNode.addWS(ws);
        catchNode.body = (BLangBlockStmt) this.blockNodeStack.pop();
        catchNode.param = variableNode;
        tryCatchFinallyNodesStack.peek().catchBlocks.add(catchNode);
    }

    public void startFinallyBlock() {
        startBlock();
    }

    public void addFinallyBlock(DiagnosticPos poc, Set<Whitespace> ws) {
        BLangBlockStmt blockNode = (BLangBlockStmt) this.blockNodeStack.pop();
        tryCatchFinallyNodesStack.peek().finallyBody = blockNode;
        blockNode.pos = poc;
        blockNode.addWS(ws);
    }

    public void addTryCatchFinallyStmt(DiagnosticPos poc, Set<Whitespace> ws) {
        BLangTryCatchFinally stmtNode = tryCatchFinallyNodesStack.pop();
        stmtNode.pos = poc;
        stmtNode.addWS(ws);
        addStmtToCurrentBlock(stmtNode);
    }

    public void addThrowStmt(DiagnosticPos poc, Set<Whitespace> ws) {
        ExpressionNode throwExpr = this.exprNodeStack.pop();
        BLangThrow throwNode = (BLangThrow) TreeBuilder.createThrowNode();
        throwNode.pos = poc;
        throwNode.addWS(ws);
        throwNode.expr = (BLangExpression) throwExpr;
        addStmtToCurrentBlock(throwNode);
    }

    private void addExpressionNode(ExpressionNode expressionNode) {
        this.exprNodeStack.push(expressionNode);
    }

    public void addLiteralValue(DiagnosticPos pos, Set<Whitespace> ws, int typeTag, Object value) {
        BLangLiteral litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        litExpr.addWS(ws);
        litExpr.pos = pos;
        litExpr.typeTag = typeTag;
        litExpr.value = value;
        addExpressionNode(litExpr);
    }

    public void addArrayInitExpr(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable) {
        List<ExpressionNode> argExprList;
        if (argsAvailable) {
            argExprList = exprNodeListStack.pop();
        } else {
            argExprList = new ArrayList<>(0);
        }
        BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        arrayLiteral.exprs = argExprList.stream().map(expr -> (BLangExpression) expr).collect(Collectors.toList());
        arrayLiteral.pos = pos;
        arrayLiteral.addWS(ws);
        addExpressionNode(arrayLiteral);
    }

    public void addKeyValueRecord(Set<Whitespace> ws) {
        BLangRecordKeyValue keyValue = (BLangRecordKeyValue) TreeBuilder.createRecordKeyValue();
        keyValue.addWS(ws);
        keyValue.valueExpr = (BLangExpression) exprNodeStack.pop();
        keyValue.key = new BLangRecordKey((BLangExpression) exprNodeStack.pop());
        recordLiteralNodes.peek().keyValuePairs.add(keyValue);
    }

    public void addMapStructLiteral(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRecordLiteral recordTypeLiteralNode = recordLiteralNodes.pop();
        recordTypeLiteralNode.pos = pos;
        recordTypeLiteralNode.addWS(ws);
        addExpressionNode(recordTypeLiteralNode);
    }

    public void startMapStructLiteral() {
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        recordLiteralNodes.push(literalNode);
    }

    public void startExprNodeList() {
        this.exprNodeListStack.push(new ArrayList<>());
    }

    public void endExprNodeList(int exprCount) {
        List<ExpressionNode> exprList = exprNodeListStack.peek();
        addExprToExprNodeList(exprList, exprCount);
    }

    private void addExprToExprNodeList(List<ExpressionNode> exprList, int n) {
        if (exprNodeStack.isEmpty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
        }
        ExpressionNode expr = exprNodeStack.pop();
        if (n > 1) {
            addExprToExprNodeList(exprList, n - 1);
        }
        exprList.add(expr);
    }


    public void createSimpleVariableReference(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder
                .createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.addWS(ws);
        varRef.addWS(nameReference.ws);
        varRef.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        varRef.variableName = (BLangIdentifier) nameReference.name;
        this.exprNodeStack.push(varRef);
    }

    public void createFunctionInvocation(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argExprs.add((BLangExpression) exprNode));
        }

        BLangNameReference nameReference = nameReferenceStack.pop();
        invocationNode.name = (BLangIdentifier) nameReference.name;
        invocationNode.addWS(nameReference.ws);
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        addExpressionNode(invocationNode);
    }

    public void createInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, String invocation, boolean argsAvailable) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argExprs.add((BLangExpression) exprNode));
        }

        invocationNode.expr = (BLangVariableReference) exprNodeStack.pop();
        invocationNode.name = (BLangIdentifier) createIdentifier(invocation);
        invocationNode.pkgAlias = (BLangIdentifier) createIdentifier(null);
        addExpressionNode(invocationNode);
    }

    public void createFieldBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws, String fieldName) {
        BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldBasedAccess.pos = pos;
        fieldBasedAccess.addWS(ws);
        fieldBasedAccess.field = (BLangIdentifier) createIdentifier(fieldName);
        fieldBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        addExpressionNode(fieldBasedAccess);
    }

    public void createIndexBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexBasedAccess.pos = pos;
        indexBasedAccess.addWS(ws);
        indexBasedAccess.indexExpr = (BLangExpression) exprNodeStack.pop();
        indexBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        addExpressionNode(indexBasedAccess);
    }

    public void createBinaryExpr(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.pos = pos;
        binaryExpressionNode.addWS(ws);
        binaryExpressionNode.rhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.lhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.opKind = OperatorKind.valueFrom(operator);
        addExpressionNode(binaryExpressionNode);
    }

    public void createTypeCastExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTypeCastExpr typeCastNode = (BLangTypeCastExpr) TreeBuilder.createTypeCastNode();
        typeCastNode.pos = pos;
        typeCastNode.addWS(ws);
        typeCastNode.expr = (BLangExpression) exprNodeStack.pop();
        typeCastNode.typeNode = (BLangType) typeNodeStack.pop();
        addExpressionNode(typeCastNode);
    }

    public void createTypeConversionExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTypeConversionExpr typeConversionNode = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        typeConversionNode.pos = pos;
        typeConversionNode.addWS(ws);
        typeConversionNode.expr = (BLangExpression) exprNodeStack.pop();
        typeConversionNode.typeNode = (BLangType) typeNodeStack.pop();
        addExpressionNode(typeConversionNode);
    }

    public void createUnaryExpr(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangUnaryExpr unaryExpressionNode = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        unaryExpressionNode.pos = pos;
        unaryExpressionNode.addWS(ws);
        unaryExpressionNode.expr = (BLangExpression) exprNodeStack.pop();
        unaryExpressionNode.operator = OperatorKind.valueFrom(operator);
        addExpressionNode(unaryExpressionNode);
    }

    public void createTernaryExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) TreeBuilder.createTernaryExpressionNode();
        ternaryExpr.pos = pos;
        ternaryExpr.addWS(ws);
        ternaryExpr.elseExpr = (BLangExpression) exprNodeStack.pop();
        ternaryExpr.thenExpr = (BLangExpression) exprNodeStack.pop();
        ternaryExpr.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(ternaryExpr);
    }


    public void endFunctionDef(DiagnosticPos pos,
                               Set<Whitespace> ws,
                               boolean publicFunc,
                               boolean nativeFunc,
                               boolean bodyExists,
                               boolean isReceiverAttached) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        function.pos = pos;
        function.addWS(ws);

        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
        }

        if (nativeFunc) {
            function.flagSet.add(Flag.NATIVE);
        }

        if (!bodyExists) {
            function.body = null;
        }

        if (isReceiverAttached) {
            function.receiver = (BLangVariable) this.varStack.pop();
        }

        this.compUnit.addTopLevelNode(function);
    }

    public void startWorker() {
        WorkerNode workerNode = TreeBuilder.createWorkerNode();
        this.invokableNodeStack.push(workerNode);
        startBlock();
    }

    public void addWorker(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        BLangWorker worker = (BLangWorker) this.invokableNodeStack.pop();
        worker.setName(createIdentifier(workerName));
        worker.pos = pos;
        worker.addWS(ws);
        worker.setBody(this.blockNodeStack.pop());
        if (this.forkJoinNodesStack.empty()) {
            InvokableNode invokableNode = this.invokableNodeStack.peek();
            invokableNode.getParameters().forEach(worker::addParameter);
            invokableNode.getReturnParameters().forEach(worker::addReturnParameter);
            invokableNode.addWorker(worker);
            invokableNode.addFlag(Flag.PARALLEL);
        } else {
            ((BLangForkJoin) this.forkJoinNodesStack.peek()).workers.add(worker);
        }
    }

    public void startForkJoinStmt() {
        this.forkJoinNodesStack.push(TreeBuilder.createForkJoinNode());
    }

    public void addForkJoinStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.pop();
        forkJoin.pos = pos;
        forkJoin.addWS(ws);
        this.addStmtToCurrentBlock(forkJoin);
    }

    public void startJoinCause() {
        startBlock();
    }

    public void addJoinCause(String identifier, Set<Whitespace> ws) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedBody = (BLangBlockStmt) this.blockNodeStack.pop();
        BLangVariable resultVar = (BLangVariable) this.generateBasicVarNode(
                (DiagnosticPos) this.typeNodeStack.peek().getPosition(), ws, identifier, false);
        forkJoin.joinResultVar = resultVar;
    }

    public void addJoinCondition(String joinType, List<String> workerNames, int joinCount) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedWorkerCount = joinCount;
        forkJoin.joinType = ForkJoinNode.JoinType.valueOf(joinType);
        workerNames.forEach(s -> forkJoin.joinedWorkers.add((BLangIdentifier) createIdentifier(s)));
    }

    public void startTimeoutCause() {
        startBlock();
    }

    public void addTimeoutCause(String identifier, Set<Whitespace> ws) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.timeoutBody = (BLangBlockStmt) this.blockNodeStack.pop();
        forkJoin.timeoutExpression = (BLangExpression) this.exprNodeStack.pop();
        BLangVariable resultVar = (BLangVariable) this.generateBasicVarNode(
                (DiagnosticPos) this.typeNodeStack.peek().getPosition(), ws, identifier, false);
        forkJoin.timeoutVariable = resultVar;
    }

    public void endCallableUnitBody(Set<Whitespace> ws) {
        BlockNode block = this.blockNodeStack.pop();
        InvokableNode invokableNode = this.invokableNodeStack.peek();
        invokableNode.addWS(ws);
        invokableNode.setBody(block);
    }

    public void setPackageDeclaration(DiagnosticPos pos, Set<Whitespace> ws, List<String> nameComps, String version) {
        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        nameComps.forEach(e -> pkgNameComps.add((BLangIdentifier) this.createIdentifier(e)));
        BLangIdentifier versionNode = (BLangIdentifier) this.createIdentifier(version);

        BLangPackageDeclaration pkgDcl = (BLangPackageDeclaration) TreeBuilder.createPackageDeclarationNode();
        pkgDcl.pos = pos;
        pkgDcl.addWS(ws);
        pkgDcl.pkgNameComps = pkgNameComps;
        pkgDcl.version = versionNode;
        this.compUnit.addTopLevelNode(pkgDcl);
    }

    public void addImportPackageDeclaration(DiagnosticPos pos,
                                            Set<Whitespace> ws,
                                            List<String> nameComps,
                                            String version,
                                            String alias) {

        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        nameComps.forEach(e -> pkgNameComps.add((BLangIdentifier) this.createIdentifier(e)));
        BLangIdentifier versionNode = (BLangIdentifier) this.createIdentifier(version);
        BLangIdentifier aliasNode = (alias != null && !alias.isEmpty()) ?
                (BLangIdentifier) this.createIdentifier(alias) :
                pkgNameComps.get(pkgNameComps.size() - 1);

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = pos;
        importDcl.addWS(ws);
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.version = versionNode;
        importDcl.alias = aliasNode;
        this.compUnit.addTopLevelNode(importDcl);
    }

    private VariableNode generateBasicVarNode(DiagnosticPos pos,
                                              Set<Whitespace> ws,
                                              String identifier,
                                              boolean exprAvailable) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifier);
        var.setName(name);
        var.addWS(ws);
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    public void addGlobalVariable(DiagnosticPos pos,
                                  Set<Whitespace> ws,
                                  String identifier,
                                  boolean exprAvailable,
                                  boolean publicVar) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        if (publicVar) {
            var.flagSet.add(Flag.PUBLIC);
        }

        this.compUnit.addTopLevelNode(var);
    }

    public void addConstVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicVar) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, true);
        var.flagSet.add(Flag.CONST);
        if (publicVar) {
            var.flagSet.add(Flag.PUBLIC);
        }

        this.compUnit.addTopLevelNode(var);
    }

    public void startStructDef() {
        StructNode structNode = TreeBuilder.createStructNode();
        attachAnnotations(structNode);
        this.structStack.add(structNode);
    }

    public void endStructDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicStruct) {
        BLangStruct structNode = (BLangStruct) this.structStack.pop();
        structNode.pos = pos;
        structNode.addWS(ws);
        structNode.setName(this.createIdentifier(identifier));
        if (publicStruct) {
            structNode.flagSet.add(Flag.PUBLIC);
        }

        this.varListStack.pop().forEach(structNode::addField);
        this.compUnit.addTopLevelNode(structNode);
    }

    public void startEnumDef(DiagnosticPos pos) {
        BLangEnum bLangEnum = (BLangEnum) TreeBuilder.createEnumNode();
        bLangEnum.pos = pos;
        this.enumStack.add(bLangEnum);
    }

    public void endEnumDef(String identifier, boolean publicEnum) {
        BLangEnum enumNode = (BLangEnum) this.enumStack.pop();
        enumNode.name = (BLangIdentifier) this.createIdentifier(identifier);
        if (publicEnum) {
            enumNode.flags.add(Flag.PUBLIC);
        }

        while (!this.identifierStack.empty()) {
            enumNode.addEnumField(this.identifierStack.pop());
        }
        this.compUnit.addTopLevelNode(enumNode);
    }

    public void addEnumFieldList(List<String> enumFieldList) {
        enumFieldList.forEach(identifier -> this.identifierStack.push(this.createIdentifier(identifier)));
    }

    public void startConnectorDef() {
        ConnectorNode connectorNode = TreeBuilder.createConnectorNode();
        attachAnnotations(connectorNode);
        this.connectorNodeStack.push(connectorNode);
    }

    public void startConnectorBody() {
        /* end of connector definition header, so let's populate 
         * the connector information before processing the body */
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        if (!this.varStack.empty()) {
            connectorNode.setFilteredParamter(this.varStack.pop());
        }
        if (!this.varListStack.empty()) {
            this.varListStack.pop().forEach(connectorNode::addParameter);
        }
        /* add a temporary block node to contain connector variable definitions */
        this.blockNodeStack.add(TreeBuilder.createBlockNode());
        /* action node list to contain the actions of the connector */
        this.actionNodeStack.add(new ArrayList<>());
    }

    public void endConnectorDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicCon) {
        BLangConnector connectorNode = (BLangConnector) this.connectorNodeStack.pop();
        connectorNode.pos = pos;
        connectorNode.addWS(ws);
        connectorNode.setName(this.createIdentifier(identifier));
        if (publicCon) {
            connectorNode.flagSet.add(Flag.PUBLIC);
        }

        this.compUnit.addTopLevelNode(connectorNode);
    }

    public void endConnectorBody(Set<Whitespace> ws) {
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        connectorNode.addWS(ws);
        this.blockNodeStack.pop().getStatements().forEach(
                e -> connectorNode.addVariableDef((VariableDefinitionNode) e));
        this.actionNodeStack.pop().forEach(connectorNode::addAction);
    }

    public void startActionDef() {
        ActionNode actionNode = TreeBuilder.createActionNode();
        this.invokableNodeStack.push(actionNode);
    }

    public void endActionDef(DiagnosticPos pos,
                             Set<Whitespace> ws, int annotCount, boolean nativeAction, boolean bodyExists) {
        BLangAction actionNode = (BLangAction) this.invokableNodeStack.pop();
        actionNode.pos = pos;
        actionNode.addWS(ws);
        if (nativeAction) {
            actionNode.flagSet.add(Flag.NATIVE);
        }

        if (!bodyExists) {
            actionNode.body = null;
        }

        attachAnnotations(actionNode, annotCount);
        this.connectorNodeStack.peek().addAction(actionNode);
    }

    public void startProcessingTypeNodeList() {
        this.typeNodeListStack.push(new ArrayList<>());
    }

    public void endProcessingTypeNodeList(int size) {
        for (int i = 0; i < size; i++) {
            this.typeNodeListStack.peek().add(0, typeNodeStack.pop());
        }
    }

    public void startAnnotationDef(DiagnosticPos pos) {
        BLangAnnotation annotNode = (BLangAnnotation) TreeBuilder.createAnnotationNode();
        annotNode.pos = pos;
        attachAnnotations(annotNode);
        this.annotationStack.add(annotNode);
    }

    public void endAnnotationDef(Set<Whitespace> ws, String identifier, boolean publicAnnotation) {
        BLangAnnotation annotationNode = (BLangAnnotation) this.annotationStack.pop();
        annotationNode.addWS(ws);
        annotationNode.setName(this.createIdentifier(identifier));
        this.varListStack.pop().forEach(var -> {
            BLangVariable variable = (BLangVariable) var;
            BLangAnnotAttribute annAttrNode = (BLangAnnotAttribute) TreeBuilder.createAnnotAttributeNode();
            var.getFlags().forEach(annAttrNode::addFlag);
            var.getAnnotationAttachments().forEach(annAttrNode::addAnnotationAttachment);
            annAttrNode.typeNode = variable.typeNode;
            annAttrNode.expr = variable.expr;
            annAttrNode.name = variable.name;
            annAttrNode.addWS(variable.getWS());
            annAttrNode.pos = variable.pos;

            // add the attribute to the annotation definition
            annotationNode.addAttribute(annAttrNode);
        });

        if (publicAnnotation) {
            annotationNode.flagSet.add(Flag.PUBLIC);
        }
        while (!attachmentPointStack.empty()) {
            ((BLangAnnotation) annotationNode).attachmentPoints.add(attachmentPointStack.pop());
        }
        this.compUnit.addTopLevelNode(annotationNode);
    }

    public void startAnnotationAttachment(DiagnosticPos currentPos) {
        BLangAnnotationAttachment annotAttachmentNode =
                (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        annotAttachmentNode.pos = currentPos;
        annotAttachmentStack.push(annotAttachmentNode);
    }

    public void setAnnotationAttachmentName() {
        BLangNameReference nameReference = nameReferenceStack.pop();
        annotAttachmentStack.peek().setAnnotationName(createIdentifier(nameReference.name.getValue()));
        annotAttachmentStack.peek().setPackageAlias(createIdentifier(nameReference.pkgAlias.getValue()));
    }

    public void createLiteralTypeAttributeValue(DiagnosticPos currentPos, Set<Whitespace> ws) {
        createAnnotAttribValueFromExpr(currentPos, ws);
    }

    public void createVarRefTypeAttributeValue(DiagnosticPos currentPos, Set<Whitespace> ws) {
        createAnnotAttribValueFromSimpleVarRefExpr(currentPos, ws);
    }

    public void createAnnotationTypeAttributeValue(DiagnosticPos currentPos, Set<Whitespace> ws) {
        BLangAnnotAttachmentAttributeValue annotAttrVal =
                (BLangAnnotAttachmentAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.addWS(ws);
        annotAttrVal.setValue(annotAttachmentStack.pop());
        annotAttribValStack.push(annotAttrVal);
    }

    public void createArrayTypeAttributeValue(DiagnosticPos currentPos, Set<Whitespace> ws) {
        BLangAnnotAttachmentAttributeValue annotAttrVal =
                (BLangAnnotAttachmentAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.addWS(ws);
        while (!annotAttribValStack.isEmpty()) {
            annotAttrVal.addValue(annotAttribValStack.pop());
        }
        annotAttribValStack.push(annotAttrVal);
    }

    public void createAnnotAttachmentAttribute(DiagnosticPos pos, Set<Whitespace> ws, String attrName) {
        AnnotationAttachmentAttributeValueNode attributeValueNode = annotAttribValStack.pop();
        attributeValueNode.addWS(ws);
        annotAttachmentStack.peek().addAttribute(attrName, attributeValueNode);
    }

    private void createAnnotAttribValueFromExpr(DiagnosticPos currentPos, Set<Whitespace> ws) {
        BLangAnnotAttachmentAttributeValue annotAttrVal =
                (BLangAnnotAttachmentAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.addWS(ws);
        annotAttrVal.setValue(exprNodeStack.pop());
        annotAttribValStack.push(annotAttrVal);
    }

    private void createAnnotAttribValueFromSimpleVarRefExpr(DiagnosticPos currentPos, Set<Whitespace> ws) {
        BLangAnnotAttachmentAttributeValue annotAttrVal =
                (BLangAnnotAttachmentAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.addWS(ws);
        createSimpleVariableReference(currentPos, ws);
        annotAttrVal.setValue(exprNodeStack.pop());
        annotAttribValStack.push(annotAttrVal);
    }

    private void attachAnnotations(AnnotatableNode annotatableNode) {
        annotAttachmentStack.forEach(annot -> annotatableNode.addAnnotationAttachment(annot));
        annotAttachmentStack.clear();
    }

    private void attachAnnotations(AnnotatableNode annotatableNode, int count) {
        if (count == 0 || annotAttachmentStack.empty()) {
            return;
        }

        List<AnnotationAttachmentNode> tempAnnotAttachments = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (annotAttachmentStack.empty()) {
                break;
            }
            tempAnnotAttachments.add(annotAttachmentStack.pop());
        }
        // reversing the collected annotations to preserve the original order
        Collections.reverse(tempAnnotAttachments);
        tempAnnotAttachments.forEach(annot -> annotatableNode.addAnnotationAttachment(annot));
    }

    public void addAssignmentStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isVarDeclaration) {
        ExpressionNode rExprNode = exprNodeStack.pop();
        List<ExpressionNode> lExprList = exprNodeListStack.pop();
        BLangAssignment assignmentNode = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentNode.setExpression(rExprNode);
        assignmentNode.setDeclaredWithVar(isVarDeclaration);
        assignmentNode.pos = pos;
        assignmentNode.addWS(ws);
        lExprList.forEach(expressionNode -> assignmentNode.addVariable((BLangVariableReference) expressionNode));
        addStmtToCurrentBlock(assignmentNode);
    }

    public void startWhileStmt() {
        startBlock();
    }

    public void addCommentStmt(DiagnosticPos pos, Set<Whitespace> ws, String commentText) {
        BLangComment comment = (BLangComment) TreeBuilder.createCommentNode();
        comment.pos = pos;
        comment.addWS(ws);
        comment.value = commentText;
        addStmtToCurrentBlock(comment);
    }

    public void addWhileStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
        whileNode.setCondition(exprNodeStack.pop());
        whileNode.pos = pos;
        whileNode.addWS(ws);
        BLangBlockStmt whileBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        whileBlock.pos = pos;
        whileNode.setBody(whileBlock);
        addStmtToCurrentBlock(whileNode);
    }

    public void addContinueStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangContinue continueNode = (BLangContinue) TreeBuilder.createContinueNode();
        continueNode.pos = pos;
        continueNode.addWS(ws);
        addStmtToCurrentBlock(continueNode);
    }

    public void addBreakStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangBreak breakNode = (BLangBreak) TreeBuilder.createBreakNode();
        breakNode.pos = pos;
        breakNode.addWS(ws);
        addStmtToCurrentBlock(breakNode);
    }

    public void addReturnStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean exprAvailable) {
        BLangReturn retStmt = (BLangReturn) TreeBuilder.createReturnNode();
        retStmt.pos = pos;
        retStmt.addWS(ws);
        if (exprAvailable) {
            for (ExpressionNode expr : this.exprNodeListStack.pop()) {
                retStmt.exprs.add((BLangExpression) expr);
            }
        }
        addStmtToCurrentBlock(retStmt);
    }

    public void startTransactionStmt() {
        transactionNodeStack.push(TreeBuilder.createTransactionNode());
        startBlock();
    }

    public void addTransactionBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt transactionBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        transactionBlock.pos = pos;
        transactionBlock.addWS(ws);
        transactionNode.setTransactionBody(transactionBlock);
    }

    public void startFailedBlock() {
        startBlock();
    }

    public void addFailedBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt failedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        failedBlock.pos = pos;
        failedBlock.addWS(ws);
        transactionNode.setFailedBody(failedBlock);
    }

    public void startCommittedBlock() {
        startBlock();
    }

    public void addCommittedBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt committedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        committedBlock.pos = pos;
        committedBlock.addWS(ws);
        transactionNode.setCommittedBody(committedBlock);
    }

    public void startAbortedBlock() {
        startBlock();
    }

    public void addAbortedBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt abortedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        abortedBlock.pos = pos;
        abortedBlock.addWS(ws);
        transactionNode.setAbortedBody(abortedBlock);
    }

    public void endTransactionStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.pop();
        transaction.pos = pos;
        transaction.addWS(ws);
        addStmtToCurrentBlock(transaction);
    }

    public void addAbortStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangAbort abortNode = (BLangAbort) TreeBuilder.createAbortNode();
        abortNode.pos = pos;
        abortNode.addWS(ws);
        addStmtToCurrentBlock(abortNode);
    }

    public void startIfElseNode(DiagnosticPos pos) {
        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        ifElseStatementStack.push(ifNode);
        startBlock();
    }

    public void addRetrytmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRetry retryNode = (BLangRetry) TreeBuilder.createRetryNode();
        retryNode.pos = pos;
        retryNode.addWS(ws);
        addStmtToCurrentBlock(retryNode);
        TransactionNode transactionNode = transactionNodeStack.peek();
        transactionNode.setRetryCount(exprNodeStack.pop());
    }

    public void addIfBlock(Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.peek();
        ifNode.addWS(ws);
        ifNode.setCondition(exprNodeStack.pop());
        ifNode.setBody(blockNodeStack.pop());
    }

    public void addElseIfBlock(Set<Whitespace> ws) {
        IfNode elseIfNode = ifElseStatementStack.pop();
        elseIfNode.setCondition(exprNodeStack.pop());
        elseIfNode.setBody(blockNodeStack.pop());
        Set<Whitespace> elseWS = removeFirst(ws);
        elseIfNode.addWS(ws);

        IfNode parentIfNode = ifElseStatementStack.peek();
        while (parentIfNode.getElseStatement() != null) {
            parentIfNode = (IfNode) parentIfNode.getElseStatement();
        }
        parentIfNode.addWS(elseWS);
        parentIfNode.setElseStatement(elseIfNode);
    }

    public void addElseBlock(Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.peek();
        while (ifNode.getElseStatement() != null) {
            ifNode = (IfNode) ifNode.getElseStatement();
        }
        BlockNode elseBlock = blockNodeStack.pop();
        elseBlock.addWS(ws);
        ifNode.setElseStatement(elseBlock);
    }

    public void endIfElseNode(Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.pop();
        ifNode.addWS(ws);
        addStmtToCurrentBlock(ifNode);
    }

    public void addWorkerSendStmt(DiagnosticPos pos, Set<Whitespace> ws, String workerName, boolean isForkJoinSend) {
        BLangWorkerSend workerSendNode = (BLangWorkerSend) TreeBuilder.createWorkerSendNode();
        workerSendNode.setWorkerName(this.createIdentifier(workerName));
        exprNodeListStack.pop().forEach(expr -> workerSendNode.exprs.add((BLangExpression) expr));
        workerSendNode.isForkJoinSend = isForkJoinSend;
        workerSendNode.pos = pos;
        workerSendNode.addWS(ws);
        addStmtToCurrentBlock(workerSendNode);
    }

    public void addWorkerReceiveStmt(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        BLangWorkerReceive workerReceiveNode = (BLangWorkerReceive) TreeBuilder.createWorkerReceiveNode();
        workerReceiveNode.setWorkerName(this.createIdentifier(workerName));
        exprNodeListStack.pop().forEach(expr -> workerReceiveNode.exprs.add((BLangExpression) expr));
        workerReceiveNode.pos = pos;
        workerReceiveNode.addWS(ws);
        addStmtToCurrentBlock(workerReceiveNode);
    }

    public void addExpressionStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        exprStmt.addWS(ws);
        exprStmt.expr = (BLangExpression) exprNodeStack.pop();
        addStmtToCurrentBlock(exprStmt);
    }

    public void startServiceDef(DiagnosticPos pos) {
        BLangService serviceNode = (BLangService) TreeBuilder.createServiceNode();
        serviceNode.pos = pos;
        attachAnnotations(serviceNode);
        serviceNodeStack.push(serviceNode);
    }

    public void addServiceBody(Set<Whitespace> ws) {
        ServiceNode serviceNode = serviceNodeStack.peek();
        serviceNode.addWS(ws);
        blockNodeStack.pop().getStatements()
                .forEach(varDef -> serviceNode.addVariable((VariableDefinitionNode) varDef));
    }

    public void endServiceDef(Set<Whitespace> ws, String protocolPkg, String serviceName) {
        ServiceNode serviceNode = serviceNodeStack.pop();
        serviceNode.setName(createIdentifier(serviceName));
        serviceNode.setProtocolPackageIdentifier(createIdentifier(protocolPkg));
        serviceNode.addWS(ws);
        this.compUnit.addTopLevelNode(serviceNode);
    }

    public void startResourceDef() {
        ResourceNode resourceNode = TreeBuilder.createResourceNode();
        invokableNodeStack.push(resourceNode);
    }

    public void endResourceDef(String resourceName, int annotCount) {
        ResourceNode resourceNode = (ResourceNode) invokableNodeStack.pop();
        resourceNode.setName(createIdentifier(resourceName));
        attachAnnotations(resourceNode, annotCount);
        varListStack.pop().forEach(resourceNode::addParameter);
        serviceNodeStack.peek().addResource(resourceNode);
    }

    public void createXMLQName(DiagnosticPos pos, String localname, String prefix) {
        BLangXMLQName qname = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        qname.localname = (BLangIdentifier) createIdentifier(localname);
        qname.prefix = (BLangIdentifier) createIdentifier(prefix);
        qname.pos = pos;
        addExpressionNode(qname);
    }

    public void createXMLAttribute(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangXMLAttribute xmlAttribute = (BLangXMLAttribute) TreeBuilder.createXMLAttributeNode();
        xmlAttribute.value = (BLangXMLQuotedString) exprNodeStack.pop();
        xmlAttribute.name = (BLangExpression) exprNodeStack.pop();
        xmlAttribute.pos = pos;
        xmlAttribute.addWS(ws);
        xmlAttributeNodeStack.push(xmlAttribute);
    }

    public void startXMLElement(DiagnosticPos pos, Set<Whitespace> ws, boolean isRoot) {
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) TreeBuilder.createXMLElementLiteralNode();
        xmlElement.startTagName = (BLangExpression) exprNodeStack.pop();
        xmlElement.pos = pos;
        xmlElement.isRoot = isRoot;
        xmlElement.addWS(ws);
        xmlAttributeNodeStack.forEach(attribute -> xmlElement.addAttribute(attribute));
        xmlAttributeNodeStack.clear();
        addExpressionNode(xmlElement);
    }

    public void endXMLElement(Set<Whitespace> ws) {
        BLangExpression endTag = (BLangExpression) exprNodeStack.pop();
        endTag.addWS(ws);
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        xmlElement.endTagName = endTag;
    }

    public void createXMLQuotedLiteral(DiagnosticPos pos,
                                       Set<Whitespace> ws,
                                       Stack<String> precedingTextFragments,
                                       String endingText,
                                       QuoteType quoteType) {
        List<BLangExpression> templateExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.LITERAL);
        BLangXMLQuotedString quotedString = (BLangXMLQuotedString) TreeBuilder.createXMLQuotedStringNode();
        quotedString.pos = pos;
        quotedString.quoteType = quoteType;
        quotedString.textFragments = templateExprs;
        addExpressionNode(quotedString);
    }

    public void addChildToXMLElement(Set<Whitespace> ws) {
        XMLLiteralNode child = (XMLLiteralNode) exprNodeStack.pop();
        child.addWS(ws);
        BLangXMLElementLiteral parentXMLExpr = (BLangXMLElementLiteral) exprNodeStack.peek();
        parentXMLExpr.addChild(child);
    }

    public void createXMLTextLiteral(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     Stack<String> precedingTextFragments,
                                     String endingText) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.XML_TEXT_LITERAL);
        xmlTextLiteral.pos = pos;
        addExpressionNode(xmlTextLiteral);
    }

    public void addXMLTextToElement(DiagnosticPos pos,
                                    Set<Whitespace> ws,
                                    Stack<String> precedingTextFragments,
                                    String endingText) {

        List<BLangExpression> templateExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.XML_TEXT_LITERAL);
        BLangXMLElementLiteral parentElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        templateExprs.forEach(expr -> parentElement.addChild(expr));
    }

    public void createXMLCommentLiteral(DiagnosticPos pos,
                                        Set<Whitespace> ws,
                                        Stack<String> precedingTextFragments,
                                        String endingText) {

        BLangXMLCommentLiteral xmlCommentLiteral = (BLangXMLCommentLiteral) TreeBuilder.createXMLCommentLiteralNode();
        xmlCommentLiteral.textFragments =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.LITERAL);
        xmlCommentLiteral.pos = pos;
        addExpressionNode(xmlCommentLiteral);
    }

    public void createXMLPILiteral(DiagnosticPos pos,
                                   Set<Whitespace> ws,
                                   String targetQName,
                                   Stack<String> precedingTextFragments,
                                   String endingText) {
        List<BLangExpression> dataExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.LITERAL);
        addLiteralValue(pos, ws, TypeTags.STRING, targetQName);

        BLangXMLProcInsLiteral xmlProcInsLiteral =
                (BLangXMLProcInsLiteral) TreeBuilder.createXMLProcessingIntsructionLiteralNode();
        xmlProcInsLiteral.pos = pos;
        xmlProcInsLiteral.dataFragments = dataExprs;
        xmlProcInsLiteral.target = (BLangLiteral) exprNodeStack.pop();;
        addExpressionNode(xmlProcInsLiteral);
    }

    public void addXMLNSDeclaration(DiagnosticPos pos,
                                    Set<Whitespace> ws,
                                    String namespaceUri,
                                    String prefix,
                                    boolean isTopLevel) {
        BLangXMLNS xmlns = (BLangXMLNS) TreeBuilder.createXMLNSNode();
        BLangIdentifier prefixIdentifer = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        prefixIdentifer.pos = pos;
        prefixIdentifer.value = prefix;

        addLiteralValue(pos, ws, TypeTags.STRING, namespaceUri);
        xmlns.namespaceURI = (BLangLiteral) exprNodeStack.pop();
        xmlns.prefix = prefixIdentifer;
        xmlns.pos = pos;

        if (isTopLevel) {
            this.compUnit.addTopLevelNode(xmlns);
            return;
        }

        BLangXMLNSStatement xmlnsStmt = (BLangXMLNSStatement) TreeBuilder.createXMLNSDeclrStatementNode();
        xmlnsStmt.xmlnsDecl = xmlns;
        xmlnsStmt.pos = pos;
        addStmtToCurrentBlock(xmlnsStmt);
    }

    public void createStringTemplateLiteral(DiagnosticPos pos, Set<Whitespace> ws, Stack<String> precedingTextFragments,
                                            String endingText) {
        BLangStringTemplateLiteral stringTemplateLiteral =
                (BLangStringTemplateLiteral) TreeBuilder.createStringTemplateLiteralNode();
        stringTemplateLiteral.exprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.LITERAL);
        stringTemplateLiteral.pos = pos;
        addExpressionNode(stringTemplateLiteral);
    }

    public void startTransformStmt() {
        startBlock();
    }

    public void createTransformStatement(DiagnosticPos pos) {
        BLangTransform transformNode = (BLangTransform) TreeBuilder.createTransformNode();
        transformNode.pos = pos;
        BLangBlockStmt transformBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        transformBlock.pos = pos;
        transformNode.setBody(transformBlock);
        addStmtToCurrentBlock(transformNode);
    }

    public void createXmlAttributesRefExpr(DiagnosticPos pos, boolean singleAttribute) {
        BLangExpression indexExpr = null;
        BLangXMLAttributeAccess xmlAttributeAccess =
                (BLangXMLAttributeAccess) TreeBuilder.createXMLAttributeAccessNode();
        xmlAttributeAccess.pos = pos;
        if (singleAttribute) {
            xmlAttributeAccess.indexExpr = (BLangExpression) exprNodeStack.pop();
        }
        xmlAttributeAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        addExpressionNode(xmlAttributeAccess);
    }

    // Private methods

    private List<BLangExpression> getExpressionsInTemplate(DiagnosticPos pos,
                                                           Set<Whitespace> ws,
                                                           Stack<String> precedingTextFragments,
                                                           String endingText,
                                                           NodeKind targetStrExprKind) {
        List<BLangExpression> expressions = new ArrayList<>();

        endingText = endingText == null ? "" : StringEscapeUtils.unescapeJava(endingText);
        addLiteralValue(pos, ws, TypeTags.STRING, endingText);
        expressions.add((BLangExpression) exprNodeStack.pop());

        while (!precedingTextFragments.isEmpty()) {
            expressions.add((BLangExpression) exprNodeStack.pop());
            String textFragment = precedingTextFragments.pop();
            textFragment = textFragment == null ? "" : StringEscapeUtils.unescapeJava(textFragment);
            addLiteralValue(pos, ws, TypeTags.STRING, textFragment);
            expressions.add((BLangExpression) exprNodeStack.pop());
        }

        Collections.reverse(expressions);
        return expressions;
    }

    public void endParameterList(Set<Whitespace> ws) {
        this.invokableNodeStack.peek().addWS(ws);
    }

    private Set<Whitespace> removeFirst(Set<Whitespace> ws) {
        if (ws == null) {
            return null;
        }
        Iterator<Whitespace> iterator = ws.iterator();
        TreeSet<Whitespace> singletonSet = new TreeSet<>();
        if (iterator.hasNext()) {
            Whitespace token = iterator.next();
            iterator.remove();
            singletonSet.add(token);
        }
        return singletonSet;
    }

    private Set<Whitespace> removeLast(Set<Whitespace> ws) {
        if (ws == null) {
            return null;
        }
        Iterator<Whitespace> iterator = ws.iterator();
        Whitespace last = null;
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        TreeSet<Whitespace> singletonSet = new TreeSet<>();
        if (last != null) {
            iterator.remove();
            singletonSet.add(last);
        }
        return singletonSet;
    }
}
