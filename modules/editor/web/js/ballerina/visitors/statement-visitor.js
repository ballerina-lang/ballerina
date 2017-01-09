/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['lodash', 'log', './ast-visitor', '../ast/module'], function (_, log, ASTVisitor, AST) {

    var StatementVisitor = function (args) {
        ASTVisitor.call(this, args);
    };

    StatementVisitor.prototype = Object.create(ASTVisitor.prototype);
    StatementVisitor.prototype.constructor = StatementVisitor;

    StatementVisitor.prototype.canVisitIfStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitIfStatement = function (statement) {
    };
    StatementVisitor.prototype.visitIfStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitIfStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitElseStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitElseStatement = function (statement) {
    };
    StatementVisitor.prototype.visitElseStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitElseStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitElseIfStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitElseIfStatement = function (statement) {
    };
    StatementVisitor.prototype.visitElseIfStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitElseIfStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitTryStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitTryStatement = function (statement) {
    };
    StatementVisitor.prototype.visitTryStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitTryStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitCatchStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitCatchStatement = function (statement) {
    };
    StatementVisitor.prototype.visitCatchStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitCatchStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitStatement = function (statement) {
    };
    StatementVisitor.prototype.visitStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitIfElseStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitIfElseStatement = function (statement) {
    };
    StatementVisitor.prototype.visitIfElseStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitIfElseStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitReplyStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitReplyStatement = function (statement) {
    };
    StatementVisitor.prototype.visitReplyStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitReplyStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitReturnStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitReturnStatement = function (statement) {
    };
    StatementVisitor.prototype.visitReturnStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitReturnStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitActionInvocationStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitActionInvocationStatement = function (statement) {
    };
    StatementVisitor.prototype.visitActionInvocationStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitActionInvocationStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitExpression = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitExpression = function (statement) {
    };
    StatementVisitor.prototype.visitExpression = function (statement) {
    };
    StatementVisitor.prototype.endVisitExpression = function (statement) {
    };

    StatementVisitor.prototype.canVisitFuncInvocationStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitFuncInvocationStatement = function (statement) {
    };
    StatementVisitor.prototype.visitFuncInvocationStatement= function (statement) {
    };
    StatementVisitor.prototype.endVisitFuncInvocationStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitTryCatchStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitTryCatchStatement = function (statement) {
    };
    StatementVisitor.prototype.visitTryCatchStatement= function (statement) {
    };
    StatementVisitor.prototype.endVisitTryCatchStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitAssignmentStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitAssignmentStatement = function (statement) {
    };
    StatementVisitor.prototype.visitAssignmentStatement= function (statement) {
    };
    StatementVisitor.prototype.endVisitAssignmentStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitWhileStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitWhileStatement = function (statement) {
    };
    StatementVisitor.prototype.visitWhileStatement= function (statement) {
    };
    StatementVisitor.prototype.endVisitWhileStatement = function (statement) {
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.canVisitStatement = function (node) {
        if (node instanceof AST.WhileStatement) {
            return this.canVisitWhileStatement(node);
        } else if (node instanceof AST.IfElseStatement) {
            return this.canVisitIfElseStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.canVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.canVisitElseStatement(node);
        }  else if (node instanceof AST.ElseIfStatement) {
            return this.canVisitElseIfStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.canVisitTryCatchStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.canVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.canVisitCatchStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.canVisitAssignmentStatement(node);
        } else if (node instanceof AST.ActionInvocationStatement) {
            return this.canVisitActionInvocationStatement(node);
        } else if (node instanceof AST.Expression) {
            return this.canVisitExpression(node);
        } else if (node instanceof AST.ReplyStatement) {
            return this.canVisitReplyStatement(node);
        } else if (node instanceof AST.ReturnStatement) {
            return this.canVisitReturnStatement(node);
        }  else if (node instanceof AST.FunctionInvocation) {
            return this.canVisitFuncInvocationStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.beginVisitStatement = function (node) {
        if (node instanceof AST.WhileStatement) {
            this.beginVisitWhileStatement(node);
        } else if (node instanceof AST.IfElseStatement) {
            return this.beginVisitIfElseStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.beginVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.beginVisitElseStatement(node);
        }  else if (node instanceof AST.ElseIfStatement) {
            return this.beginVisitElseIfStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.beginVisitTryCatchStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.beginVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.beginVisitCatchStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.beginVisitAssignmentStatement(node);
        } else if (node instanceof AST.ActionInvocationStatement) {
            return this.beginVisitActionInvocationStatement(node);
        } else if (node instanceof AST.Expression) {
            return this.beginVisitExpression(node);
        } else if (node instanceof AST.ReplyStatement) {
            return this.beginVisitReplyStatement(node);
        } else if (node instanceof AST.ReturnStatement) {
            return this.beginVisitReturnStatement(node);
        }  else if (node instanceof AST.FunctionInvocation) {
            return this.beginVisitFuncInvocationStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.endVisitStatement = function (node) {
        if (node instanceof AST.WhileStatement) {
            return this.endVisitWhileStatement(node);
        } else if (node instanceof AST.IfElseStatement) {
            return this.endVisitIfElseStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.endVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.endVisitElseStatement(node);
        }  else if (node instanceof AST.ElseIfStatement) {
            return this.endVisitElseIfStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.endVisitTryCatchStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.endVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.endVisitCatchStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.endVisitAssignmentStatement(node);
        } else if (node instanceof AST.ActionInvocationStatement) {
            return this.endVisitActionInvocationStatement(node);
        } else if (node instanceof AST.Expression) {
            return this.endVisitExpression(node);
        } else if (node instanceof AST.ReplyStatement) {
            return this.endVisitReplyStatement(node);
        }  else if (node instanceof AST.ReturnStatement) {
            return this.endVisitReturnStatement(node);
        }  else if (node instanceof AST.FunctionInvocation) {
            return this.endVisitFuncInvocationStatement(node);
        }
    };

    return StatementVisitor;
});
