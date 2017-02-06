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

/**
 * Module for BallerinaASTRoot
 */
define(['lodash', 'log', './node', './import-declaration'], function (_, log, ASTNode, ImportDeclaration) {

    /**
     * Constructs BallerinaASTRoot
     * @class BallerinaASTRoot
     * @augments ASTNode
     * @param args
     * @constructor
     */
    var BallerinaASTRoot = function (args) {
        this.packageDefinition = _.get(args, 'packageDefinition');
        this.serviceDefinitions = _.get(args, 'serviceDefinitions', []);
        this.functionDefinitions = _.get(args, 'functionDefinitions', []);
        this.connectorDefinitions = _.get(args, 'connectorDefinitions', []);
        this.structDefinitions = _.get(args, 'structDefinitions', []);
        this.typeDefinitions = _.get(args, 'typeDefinitions', []);
        this.typeMapperDefinitions = _.get(args, 'typeMapperDefinitions', []);
        ASTNode.call(this, "BallerinaASTRoot");
    };

    BallerinaASTRoot.prototype = Object.create(ASTNode.prototype);
    BallerinaASTRoot.prototype.constructor = BallerinaASTRoot;

    /**
     * Setter function for PackageDefinition
     * @param packageDefinition
     */
    BallerinaASTRoot.prototype.setPackageDefinition = function (packageDefinition, options){
        if(!_.isNil(packageDefinition)){
            packageDefinition.setParent(this, options);
            this.setAttribute('packageDefinition', packageDefinition, options);
        }
    };

    /**
     * Setter for ImportDeclarations
     * @param importDeclarations
     */
    BallerinaASTRoot.prototype.setImportDeclarations = function (importDeclarations, options) {
        if(!_.isNil(importDeclarations)){
            // TODO : Need to be implemented.
            throw "Set import declaration is not implemented";
        }
    };

    /**
     * Setter function for ServiceDefinition
     * @param serviceDefinitions
     */
    BallerinaASTRoot.prototype.setServiceDefinitions = function (serviceDefinitions, options) {
        if (!_.isNil(serviceDefinitions)) {
            this.setAttribute('serviceDefinitions', serviceDefinitions, options);
            var self = this;
            _.forEach(serviceDefinitions, function (serviceDefinition) {
                serviceDefinition.setParent(self, options);
            });
        }
    };

    /**
     * Setter function for ConnectorDefinition
     * @param connectorDefinitions
     */
    BallerinaASTRoot.prototype.setConnectorDefinitions = function (connectorDefinitions, options) {
        if (!_.isNil(connectorDefinitions)) {
            this.setAttribute('connectorDefinitions', connectorDefinitions, options);
            var self = this;
            _.forEach(connectorDefinitions, function (connectorDefinition) {
                connectorDefinition.setParent(self, options);
            });
        }
    };

    /**
     * Setter function for FunctionDefinition
     * @param functionDefinitions
     */
    BallerinaASTRoot.prototype.setFunctionDefinitions = function (functionDefinitions, options) {
        if (!_.isNil(functionDefinitions)) {
            this.setAttribute('functionDefinitions', functionDefinitions, options);
            var self = this;
            _.forEach(functionDefinitions, function (functionDefinition) {
                functionDefinition.setParent(self, options);
            });
        }
    };

    /**
     * Getter function for PackageDefinition
     * @return {*}
     */
    BallerinaASTRoot.prototype.getPackageDefinition = function () {
        return this.packageDefinition;
    };

    /**
     * Getter function for import declarations.
     * @return {*}
     */
    BallerinaASTRoot.prototype.getImportDeclarations = function () {
        var importDeclarations = [];
        var ballerinaASTFactory = this.getFactory();
        _.forEach(this.getChildren(), function(child){
            if (ballerinaASTFactory.isImportDeclaration(child)) {
                importDeclarations.push(child);
            }
        });
        return importDeclarations;
    };

    /**
     * Getter function for ServiceDefinition
     * @returns {Array}
     */
    BallerinaASTRoot.prototype.getServiceDefinitions = function () {
        return this.serviceDefinitions;
    };

    /**
     * Getter function for ServiceDefinition
     * @returns {Array}
     */
    BallerinaASTRoot.prototype.getConnectorDefinitions = function () {
        return this.connectorDefinitions;
    };

    /**
     * Getter function for FunctionDefinition
     * @returns {Array}
     */
    BallerinaASTRoot.prototype.getFunctionDefinitions = function () {
        return this.functionDefinitions;
    };

    /**
     * This is the root element of ballerina ast - so cannot be a child of anyone.
     * @param node
     * @return {boolean}
     */
    BallerinaASTRoot.prototype.canBeAChildOf = function (node) {
        return false;
    };

    /**
     * Deletes an import with given package name.
     */
    BallerinaASTRoot.prototype.deleteImport = function (packageName) {
        var ballerinaASTFactory = this.getFactory();
        var importDeclaration = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isImportDeclaration(child) && child.getPackageName() == packageName;
        });

        var modifiedEvent = {
            origin: this,
            type: 'child-removed',
            title: 'remove import',
            data: {
                child: importDeclaration,
                index: this.getIndexOfChild(importDeclaration)
            }
        };

        _.remove(this.getChildren(), function (child) {
            return ballerinaASTFactory.isImportDeclaration(child) && child.getPackageName() == packageName;
        });

        /**
         * @event ASTNode#tree-modified
         */
        this.trigger('tree-modified', modifiedEvent);
    };

    /**
     * Adds new import declaration.
     * @param {ImportDeclaration} importDeclaration - New import declaration.
     */
    BallerinaASTRoot.prototype.addImport = function (importDeclaration) {
        var ballerinaASTFactory = this.getFactory();
        var index = _.findLastIndex(this.getChildren(), function(child){
            return ballerinaASTFactory.isImportDeclaration(child);
        });

        // If there are no imports index is -1. Then we need to add the first import after the package
        // definition which is the first child of the ast root
        if (index === -1) {
            index = 0;
        }
        this.getChildren().splice(index + 1, 0, importDeclaration);

        var modifiedEvent = {
            origin: this,
            type: 'child-added',
            title: 'add import',
            data: {
                child: importDeclaration,
                index: index + 1
            }
        };

        /**
         * @event ASTNode#tree-modified
         */
        this.trigger('tree-modified', modifiedEvent);
    };

    //// Start of constant definition functions

    /**
     * Adds a new constance definition.
     * @param {string} bType - The ballerina type.
     * @param {string} identifier - The identifier.
     * @param {string} value - The value of the constant.
     */
    BallerinaASTRoot.prototype.addConstantDefinition = function(bType, identifier, value) {

        // Check if already constant declaration exists with same identifier.
        var identifierAlreadyExists = _.findIndex(this.getConstantDefinitions(), function (constantDefinition) {
                return constantDefinition.getIdentifier() === identifier;
            }) !== -1;

        // If constant declaration with the same identifier exists, then throw an error. Else create the new constant
        // declaration.
        if (identifierAlreadyExists) {
            var errorString = "A constant with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new constant definition.
            var newConstantDefinition = this.getFactory().createConstantDefinition();
            newConstantDefinition.setType(bType);
            newConstantDefinition.setIdentifier(identifier);
            newConstantDefinition.setValue(value);

            var self = this;

            // Get the index of the last constant declaration.
            var index = _.findLastIndex(this.getChildren(), function (child) {
                return self.getFactory().isConstantDefinition(child);
            });

            if (index == -1) {
                index = _.findLastIndex(this.getChildren(), function (child) {
                    return self.getFactory().isImportDeclaration(child);
                })
            }

            this.addChild(newConstantDefinition, index + 1);
        }
    };

    /**
     * Removes a constant definition.
     * @param {string} modelID - The ID of the constant definition.
     */
    BallerinaASTRoot.prototype.removeConstantDefinition = function(modelID) {
        var self = this;
        // Deleting the variable from the children.
        var resourceParameter = _.find(this.getChildren(), function (child) {
            return self.getFactory().isConstantDefinition(child) && child.id === modelID;
        });

        this.removeChild(resourceParameter);
    };

    /**
     * Gets all the constant definitions.
     * @return {ConstantDefinition[]} - The constant definitions.
     */
    BallerinaASTRoot.prototype.getConstantDefinitions = function () {
        var constantDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConstantDefinition(child)) {
                constantDeclarations.push(child);
            }
        });
        return constantDeclarations;
    };

    //// End of constant definition functions

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    BallerinaASTRoot.prototype.canBeParentOf = function (node) {
        var BallerinaASTFactory = this.getFactory();
        return BallerinaASTFactory.isServiceDefinition(node)
            || BallerinaASTFactory.isFunctionDefinition(node)
            || BallerinaASTFactory.isConnectorDefinition(node)
            || BallerinaASTFactory.isTypeDefinition(node)
            || BallerinaASTFactory.isConnectorDefinition(node)
            || BallerinaASTFactory.isStructDefinition(node)
            || BallerinaASTFactory.isTypeMapperDefinition(node)
            || BallerinaASTFactory.isTypeStructDefinition(node);
    };


    return BallerinaASTRoot;
});
