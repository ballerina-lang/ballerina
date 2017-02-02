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
define(['lodash', 'log', './node'], function (_, log, ASTNode) {
    var StructDefinition = function (args) {
        ASTNode.call(this, 'StructDefinition');
        this._structName = _.get(args, 'structName', 'newStruct');
        this.BallerinaASTFactory = this.getFactory();
    };

    StructDefinition.prototype = Object.create(ASTNode.prototype);
    StructDefinition.prototype.constructor = StructDefinition;

    /**
     * setter for struct name
     * @param structName - name of the struct
     */
    StructDefinition.prototype.setStructName = function (structName) {
        this._structName = structName;
    };

    /**
     * getter for struct name
     * @returns {string} struct name
     */
    StructDefinition.prototype.getStructName = function () {
        return this._structName;
    };

    /**
     * Gets all the variables declarations residing in the struct.
     * @return {VariableDeclaration[]} - The variable declarations.
     */
    StructDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    /**
     * Adds new variable declaration.
     * @param {string} bType - The ballerina type of the variable.
     * @param {string} identifier - The identifier of the variable
     */
    StructDefinition.prototype.addVariableDeclaration = function (bType, identifier) {
        // Check if already variable declaration exists with same identifier.
        var identifierAlreadyExists = _.findIndex(this.getVariableDeclarations(), function (variableDeclaration) {
                return variableDeclaration.getIdentifier() === identifier;
            }) !== -1;

        // If variable declaration with the same identifier exists, then throw an error. Else create the new variable
        // declaration.
        if (identifierAlreadyExists) {
            var errorString = "A variable with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new variable declaration.
            var newVariableDeclaration = this.getFactory().createVariableDeclaration();

            newVariableDeclaration.setType(bType);
            newVariableDeclaration.setIdentifier(identifier);

            var self = this;

            // Get the index of the last declaration.
            var index = _.findLastIndex(this.getChildren(), function (child) {
                return self.getFactory().isVariableDeclaration(child);
            });

            this.addChild(newVariableDeclaration, index + 1);
        }
    };

    /**
     * Removes new variable declaration.
     * @param {string} modelID - The model ID of the variable.
     */
    StructDefinition.prototype.removeVariableDeclaration = function (modelID) {
        var self = this;
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child)
                && child.getID() === modelID;
        });
        this.removeChild(variableDeclarationChild)
    };

    /**
     * Initialize StructDefinition from json object
     * @param {Object} jsonNode - JSON object for initialization.
     * @param {string} jsonNode.struct_name - Name of the struct definition.
     * @param {VariableDeclaration[]} jsonNode.children - Variables of the struct definition.
     */
    StructDefinition.prototype.initFromJson = function (jsonNode) {
        var self = this;
        this._structName = jsonNode.struct_name;

        _.each(jsonNode.children, function (childNode) {
            var child = self.BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return StructDefinition;
});