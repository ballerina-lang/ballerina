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
define(['lodash', 'log','./statement', './else-statement', './else-if-statement', './if-statement'], function (_, log, Statement, ElseStatement, ElseIfStatement, IfStatement) {

    /**
     * Class for if conditions in ballerina.
     * @constructor
     */

    var IfElseStatement = function (args) {
        Statement.call(this);
        var ifStatement = new IfStatement(args);
        this.addChild(ifStatement);
        this._ifStatement = ifStatement;
        this._elseStatement = undefined;
        this._elseIfStatements = [];
        this.type = "IfElseStatement";
    };

    IfElseStatement.prototype = Object.create(Statement.prototype);
    IfElseStatement.prototype.constructor = IfElseStatement;

    IfElseStatement.prototype.getIfStatement = function () {
        return this._ifStatement;
    };

    IfElseStatement.prototype.getElseStatement = function () {
        return this._elseStatement;
    };

    IfElseStatement.prototype.getElseIfStatement = function () {
        return this._elseIfStatements;
    };

    IfElseStatement.prototype.setIfStatement = function (ifStatement) {
        this._ifStatement = ifStatement;
    };

    /**
     * creates Else Statement
     * @param args
     */
    IfElseStatement.prototype.createElseStatement = function (args) {
        var newElseStatement = new ElseStatement(args);
        this._elseStatement = newElseStatement;
        this.addChild(newElseStatement);
        return newElseStatement;
    };

    /**
     * creates Else If Statement
     * @param args
     */
    IfElseStatement.prototype.createElseIfStatement = function (args) {
        var newElseIfStatement = new ElseIfStatement(args);
        this._elseIfStatements.push(newElseIfStatement);
        this.addChild(newElseIfStatement);
        return newElseIfStatement;
    };

    /**
     * initialize IfElseStatement from json object
     * @param {Object} jsonNode to initialize from
     * @param {Object} [jsonNode.if_statement] - If statement block
     * @param {Object} [jsonNode.else_statement] - Else statement block
     */
    IfElseStatement.prototype.initFromJson = function (jsonNode) {

        var self = this;

        _.each(jsonNode.if_statement, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            if (self.getFactory().isExpression(child)) {
                child.initFromJson(childNode);
                this._condition = child.getExpression();
            } else {
                self._ifStatement.addChild(child);
                child.initFromJson(childNode);
            }
        });

        _.each(jsonNode.else_statement, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self._elseStatement.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return IfElseStatement;
});