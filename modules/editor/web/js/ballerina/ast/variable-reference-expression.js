/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash', './expression'], function (_, Expression) {

    /**
     * Constructor for VariableReferenceExpression
     * @param {Object} args - Arguments to create the VariableReferenceExpression
     * @constructor
     */
    var VariableReferenceExpression = function (args) {
        this._symbolName = _.get(args, 'symbolName');
        Expression.call(this, 'VariableReferenceExpression');
    };

    VariableReferenceExpression.prototype = Object.create(Expression.prototype);
    VariableReferenceExpression.prototype.constructor = VariableReferenceExpression;

    /**
     * Setter for Symbol name
     * @param symbolName
     */
    VariableReferenceExpression.prototype.setSymbolName = function (symbolName) {
        this._symbolName = symbolName;
    }

    /**
     * Getter for Symbol name
     * @returns {*}
     */
    VariableReferenceExpression.prototype.getSymbolName = function () {
        return this._symbolName;
    }

    /**
     * initialize VariableReferenceExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.symbolName] - Symbol name of the VariableReferenceExpression
     */
    VariableReferenceExpression.prototype.initFromJson = function (jsonNode) {
        this.setSymbolName(jsonNode.variable_reference_name);
    };

    return VariableReferenceExpression;
});