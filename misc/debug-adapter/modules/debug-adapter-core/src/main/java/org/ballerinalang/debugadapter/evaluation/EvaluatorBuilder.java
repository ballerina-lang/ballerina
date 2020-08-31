/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation;

import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BracedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
import io.ballerinalang.compiler.syntax.tree.NilLiteralNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeVisitor;
import io.ballerinalang.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerinalang.compiler.syntax.tree.RestArgumentNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.engine.BasicLiteralEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.BinaryExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.FieldAccessExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.FunctionInvocationExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.MethodCallExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.SimpleNameReferenceEvaluator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A {@code NodeVisitor} based implementation used to traverse and capture evaluatable segments of a parsed ballerina
 * expression.
 * <br><br>
 * Supported expression types. (Language specification v2020R1)
 * <ul>
 * <li> Literal expression
 * <li> Multiplicative expression
 * <li> Additive expression
 * <li> Numerical comparison expression
 * <li> Variable reference expression
 * <li> Field access expression
 * <li> Function call expression
 * <li> Function call expression
 * <li> Braced expression
 * </ul>
 * <br>
 * To be Implemented.
 * <ul>
 * <li> String template expression
 * <li> XML template expression
 * <li> New expression
 * <li> Optional field access expression
 * <li> XML attribute access expression
 * <li> Annotation access expression
 * <li> Member access expression
 * <li> Error constructor
 * <li> Anonymous function expression
 * <li> Let expression
 * <li> Type cast expression
 * <li> Typeof expression
 * <li> Unary expression
 * <li> Shift expression
 * <li> Range expression
 * <li> Type test expression
 * <li> Equality expression
 * <li> Binary bitwise expression
 * <li> Logical expression
 * <li> Conditional expression
 * <li> Checking expression
 * <li> Trap expression
 * <li> Query expression
 * <li> XML navigation expression
 * </ul>
 *
 * @since 2.0.0
 */
public class EvaluatorBuilder extends NodeVisitor {

    private final Set<SyntaxKind> supportedSyntax = new HashSet<>();
    private final Set<SyntaxKind> capturedSyntax = new HashSet<>();
    private final List<Node> unsupportedNodes = new ArrayList<>();
    private final SuspendedContext context;
    private Evaluator result = null;
    private EvaluationException builderException = null;

    public EvaluatorBuilder(SuspendedContext context) {
        this.context = context;
        prepareForEvaluation();
    }

    /**
     * Parses a given ballerina expression and transforms into a tree of executable {@link Evaluator} instances.
     *
     * @param expression Ballerina expression(user input).
     * @throws EvaluationException If validation/parsing is failed.
     */
    public Evaluator build(String expression) throws EvaluationException {
        // Validates and converts the expression into a parsed syntax-tree node.
        ExpressionNode parsedExpr = DebugExpressionParser.validateAndParse(expression);
        // transforms the parsed ballerina expression into a java expression using a node transformer implementation.
        parsedExpr.accept(this);
        if (unsupportedSyntaxDetected()) {
            final StringJoiner errors = new StringJoiner(System.lineSeparator());
            unsupportedNodes.forEach(node -> errors.add(String.format("%s - %s", node.toString(), node.kind())));
            throw new EvaluationException(String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION.getString(),
                    errors));
        }
        if (result == null) {
            throw builderException;
        }
        return result;
    }

    @Override
    public void visit(BracedExpressionNode bracedExpressionNode) {
        visitSyntaxNode(bracedExpressionNode);
        bracedExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(BinaryExpressionNode binaryExpressionNode) {
        visitSyntaxNode(binaryExpressionNode);
        binaryExpressionNode.lhsExpr().accept(this);
        Evaluator lhsEvaluator = result;
        binaryExpressionNode.rhsExpr().accept(this);
        Evaluator rhsEvaluator = result;
        result = new BinaryExpressionEvaluator(context, binaryExpressionNode, lhsEvaluator, rhsEvaluator);
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        visitSyntaxNode(functionCallExpressionNode);
        // Evaluates arguments.
        List<Evaluator> argEvaluators = new ArrayList<>();
        SeparatedNodeList<FunctionArgumentNode> args = functionCallExpressionNode.arguments();
        // Removes argument separator nodes from the args list.
        for (int index = args.size() - 2; index > 0; index -= 2) {
            args.remove(index);
        }
        for (int idx = 0; idx < args.size(); idx++) {
            final FunctionArgumentNode argExprNode = args.get(idx);
            argExprNode.accept(this);
            if (result == null) {
                builderException = new EvaluationException(String.format(EvaluationExceptionKind.INVALID_ARGUMENT
                        .getString(), argExprNode.toString()));
                return;
            }
            // Todo - should we disable GC like intellij expression evaluator does?
            argEvaluators.add(result);
        }
        result = new FunctionInvocationExpressionEvaluator(context, functionCallExpressionNode, argEvaluators);
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        visitSyntaxNode(methodCallExpressionNode);
        // visits object expression.
        methodCallExpressionNode.expression().accept(this);
        Evaluator expression = result;
        // visits object method arguments.
        List<Evaluator> argEvaluators = new ArrayList<>();
        SeparatedNodeList<FunctionArgumentNode> args = methodCallExpressionNode.arguments();
        // Removes argument separator nodes from the args list.
        for (int index = args.size() - 2; index > 0; index -= 2) {
            args.remove(index);
        }
        for (int idx = 0; idx < args.size(); idx++) {
            final FunctionArgumentNode argExprNode = args.get(idx);
            argExprNode.accept(this);
            if (result == null) {
                builderException = new EvaluationException(String.format(EvaluationExceptionKind.INVALID_ARGUMENT
                        .getString(), argExprNode.toString()));
                return;
            }
            // Todo - should we disable GC like intellij expression evaluator does?
            argEvaluators.add(result);
        }
        result = new MethodCallExpressionEvaluator(context, expression, methodCallExpressionNode, argEvaluators);
    }

    @Override
    public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {
        visitSyntaxNode(fieldAccessExpressionNode);
        // visits object expression.
        fieldAccessExpressionNode.expression().accept(this);
        Evaluator expression = result;
        result = new FieldAccessExpressionEvaluator(context, expression, fieldAccessExpressionNode);
    }

    @Override
    public void visit(PositionalArgumentNode positionalArgumentNode) {
        visitSyntaxNode(positionalArgumentNode);
        positionalArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(NamedArgumentNode namedArgumentNode) {
        visitSyntaxNode(namedArgumentNode);
        namedArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(RestArgumentNode restArgumentNode) {
        visitSyntaxNode(restArgumentNode);
        restArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        visitSyntaxNode(simpleNameReferenceNode);
        result = new SimpleNameReferenceEvaluator(context, simpleNameReferenceNode);
    }

    @Override
    public void visit(BasicLiteralNode basicLiteralNode) {
        visitSyntaxNode(basicLiteralNode);
        result = new BasicLiteralEvaluator(context, basicLiteralNode);
    }

    @Override
    public void visit(NilLiteralNode nilLiteralNode) {
        visitSyntaxNode(nilLiteralNode);
        result = new BasicLiteralEvaluator(context, nilLiteralNode);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        capturedSyntax.add(node.kind());
        if (!supportedSyntax.contains(node.kind())) {
            unsupportedNodes.add(node);
        }
    }

    @Override
    public void visit(Token token) {
    }

    private boolean unsupportedSyntaxDetected() {
        return !unsupportedNodes.isEmpty();
    }

    private void prepareForEvaluation() {
        addLiteralExpressionSyntax();
        addStringTemplateExpressionSyntax();
        addXmlTemplateExpressionSyntax();
        addNewExpressionSyntax();
        addVariableReferenceExpressionSyntax();
        addFieldAccessExpressionSyntax();
        addOptionalFieldAccessExpressionSyntax();
        addXmlAttributeAccessExpressionSyntax();
        addAnnotationAccessExpressionSyntax();
        addMemberAccessExpressionSyntax();
        addFunctionCallExpressionSyntax();
        addMethodCallExpressionSyntax();
        addErrorConstructorExpressionSyntax();
        addAnonymousFunctionExpressionSyntax();
        addLetExpressionSyntax();
        addTypeCastExpressionSyntax();
        addTypeOfExpressionSyntax();
        addUnaryExpressionSyntax();
        addMultiplicativeExpressionSyntax();
        addAdditiveExpressionSyntax();
        addShiftExpressionSyntax();
        addRangeExpressionSyntax();
        addNumericalComparisonExpressionSyntax();
        addTypeTestExpressionSyntax();
        addEqualityExpressionSyntax();
        addBinaryBitwiseExpressionSyntax();
        addLogicalExpressionSyntax();
        addConditionalExpressionSyntax();
        addCheckingExpressionSyntax();
        addTrapExpressionSyntax();
        addQueryExpressionSyntax();
        addXmlNavigationExpressionSyntax();
        addMiscellaneousSyntax();
    }

    private void addLiteralExpressionSyntax() {
        // nil
        supportedSyntax.add(SyntaxKind.NIL_LITERAL);
        // boolean
        supportedSyntax.add(SyntaxKind.BOOLEAN_LITERAL);
        supportedSyntax.add(SyntaxKind.TRUE_KEYWORD);
        supportedSyntax.add(SyntaxKind.FALSE_KEYWORD);
        // numeric literal
        supportedSyntax.add(SyntaxKind.NUMERIC_LITERAL);
        supportedSyntax.add(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN);
        supportedSyntax.add(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
        // string
        supportedSyntax.add(SyntaxKind.STRING_LITERAL);
        // Todo - Add support for hex literals
        // Todo - Add support for byte array literal
    }

    private void addStringTemplateExpressionSyntax() {
        // Todo
    }

    private void addXmlTemplateExpressionSyntax() {
        // Todo
    }

    private void addNewExpressionSyntax() {
        // Todo
    }

    private void addVariableReferenceExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.SIMPLE_NAME_REFERENCE);
        // Todo - Add qualified identifier support
        // Todo - Xml qualified name
    }

    private void addFieldAccessExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.FIELD_ACCESS);
    }

    private void addOptionalFieldAccessExpressionSyntax() {
        // Todo
    }

    private void addXmlAttributeAccessExpressionSyntax() {
        // Todo
    }

    private void addAnnotationAccessExpressionSyntax() {
        // Todo
    }

    private void addMemberAccessExpressionSyntax() {
        // Todo
    }

    private void addFunctionCallExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.FUNCTION_CALL);
        supportedSyntax.add(SyntaxKind.POSITIONAL_ARG);
        supportedSyntax.add(SyntaxKind.OPEN_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.CLOSE_PAREN_TOKEN);
        // Todo: Add named args and rest args
    }

    private void addMethodCallExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.METHOD_CALL);
        supportedSyntax.add(SyntaxKind.POSITIONAL_ARG);
        supportedSyntax.add(SyntaxKind.OPEN_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.CLOSE_PAREN_TOKEN);
        // Todo: Add named args and rest args
    }

    private void addErrorConstructorExpressionSyntax() {
        // Todo
    }

    private void addAnonymousFunctionExpressionSyntax() {
        // Todo
    }

    private void addLetExpressionSyntax() {
        // Todo
    }

    private void addTypeCastExpressionSyntax() {
        // Todo
    }

    private void addTypeOfExpressionSyntax() {
        // Todo
    }

    private void addUnaryExpressionSyntax() {
        // Todo
    }

    private void addMultiplicativeExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.BINARY_EXPRESSION);
        supportedSyntax.add(SyntaxKind.ASTERISK_TOKEN);
        supportedSyntax.add(SyntaxKind.SLASH_TOKEN);
        supportedSyntax.add(SyntaxKind.PERCENT_TOKEN);
    }

    private void addAdditiveExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.BINARY_EXPRESSION);
        supportedSyntax.add(SyntaxKind.PLUS_TOKEN);
        supportedSyntax.add(SyntaxKind.MINUS_TOKEN);
    }

    private void addShiftExpressionSyntax() {
        // Todo
    }

    private void addRangeExpressionSyntax() {
        // Todo
    }

    private void addNumericalComparisonExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.BINARY_EXPRESSION);
        supportedSyntax.add(SyntaxKind.LT_TOKEN);
        supportedSyntax.add(SyntaxKind.LT_EQUAL_TOKEN);
        supportedSyntax.add(SyntaxKind.GT_TOKEN);
        supportedSyntax.add(SyntaxKind.GT_EQUAL_TOKEN);
    }

    private void addTypeTestExpressionSyntax() {
        // Todo
    }

    private void addEqualityExpressionSyntax() {
        // Todo
    }

    private void addBinaryBitwiseExpressionSyntax() {
        // Todo
    }

    private void addLogicalExpressionSyntax() {
        // Todo
    }

    private void addConditionalExpressionSyntax() {
        // Todo
    }

    private void addCheckingExpressionSyntax() {
        // Todo
    }

    private void addTrapExpressionSyntax() {
        // Todo
    }

    private void addQueryExpressionSyntax() {
        // Todo
    }

    private void addXmlNavigationExpressionSyntax() {
        // Todo
    }

    private void addMiscellaneousSyntax() {
        // braced expression
        supportedSyntax.add(SyntaxKind.BRACED_EXPRESSION);
        supportedSyntax.add(SyntaxKind.OPEN_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.CLOSE_PAREN_TOKEN);
        // misc
        supportedSyntax.add(SyntaxKind.IDENTIFIER_TOKEN);
        supportedSyntax.add(SyntaxKind.NONE);
        supportedSyntax.add(SyntaxKind.EOF_TOKEN);
    }
}
