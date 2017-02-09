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
define(['lodash', './node'], function (_, ASTNode) {

    var TypeMapperDefinition = function (args) {
        ASTNode.call(this, 'TypeMapperDefinition');
        this._typeMapperName = _.get(args, 'typeMapperName', 'newTypeMapper');
        this._returnStatementExpression = _.get(args, 'returnStatementExpression', undefined);
        this._selectedTypeStructNameForSource = _.get(args, 'selectedTypeStructNameForSource', 'default');
        this._selectedTypeStructNameForTarget = _.get(args, 'selectedTypeStructNameForTarget', 'default');
    };

    TypeMapperDefinition.prototype = Object.create(ASTNode.prototype);
    TypeMapperDefinition.prototype.constructor = TypeMapperDefinition;

    /**
     * Set the type mapper name
     * @param typeMapperName
     */
    TypeMapperDefinition.prototype.setTypeMapperName = function (typeMapperName, options) {
        if (!_.isNil(typeMapperName)) {
            this.setAttribute('_typeMapperName', typeMapperName, options);
        } else {
            log.error('Invalid Type Mapper name [' + typeMapperName + '] Provided');
            throw 'Invalid Type Mapper name [' + typeMapperName + '] Provided';
        }
    };

    /**
     * returns the type mapper name
     * @returns {string} type mapper name
     */
    TypeMapperDefinition.prototype.getTypeMapperName = function () {
        return this._typeMapperName;
    };

    /**
     * return variable declarations
     * @returns {Array} variable declarations array
     */
    TypeMapperDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    /**
     * Gets the return SourceAndTaergetObjects
     * @return {object} - Return type.
     */
    TypeMapperDefinition.prototype.getSourceAndTaergetObjects = function (targetExpression) {
        var sourceTargetContainer = {};
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isTypeStructDefinition(child)) {
                if(child.getTypeStructName() == targetExpression){
                    sourceTargetContainer["target"] = child;
                }else{
                    sourceTargetContainer["source"] = child;
                }
            }
        });
        return sourceTargetContainer;
    };



    /**
     * Add variable declaration
     * @param newVariableDeclaration
     */
    TypeMapperDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
        // Get the index of the last variable declaration.
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        var index = _.findLastIndex(this.getChildren(), function (child) {
            return ballerinaASTFactory.isVariableDeclaration(child);
        });

        this.addChild(newVariableDeclaration, index + 1);
    };

    /**
     * Remove variable declaration
     * @param variableDeclarationIdentifier
     */
    TypeMapperDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
        var self = this;
        var ballerinaASTFactory = this.getFactory();
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    };

    /**
     * Gets the return type
     * @return {string} - Return type.
     */
    TypeMapperDefinition.prototype.getReturnType = function () {
        var returnType = "";
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isTypeStructDefinition(child)) {
                if(child.getTypeStructName() == self.getReturnStatementExpression()){
                    returnType = child.getTypeStructName();
                }
            }
        });
        return returnType;
    };

    /**
     * returns the argument
     * @returns {String} argument
     */
    TypeMapperDefinition.prototype.getSourceAndIdentifier = function () {
        var sourceAndIdentifier = "";
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isTypeStructDefinition(child)) {
                if(child.getTypeStructName() != self.getReturnStatementExpression()){
                    sourceAndIdentifier = child.getTypeStructName() + " " + child.getIdentifier();
                }
            }
        });
        return sourceAndIdentifier;
    };

    /**
     * Set the already selected type struct name for source
     * @param selectedStructNameForSource
     */
    TypeMapperDefinition.prototype.setSelectedStructNameForSource = function (selectedStructNameForSource, options) {
        if (!_.isNil(selectedStructNameForSource)) {
            this.setAttribute('_selectedTypeStructNameForSource', selectedStructNameForSource, options);
        } else {
            log.error('Invalid TypeStructName [' + selectedStructNameForSource + '] Provided');
            throw 'Invalid TypeStructName [' + selectedStructNameForSource + '] Provided';
        }
    };

    /**
     * Returns the selected type struct name for source
     * @returns {string} type struct name for source
     */
    TypeMapperDefinition.prototype.getSelectedStructNameForSource = function () {
        return this._selectedTypeStructNameForSource;
    };

    /**
     * Set the already selected type struct name for target
     * @param selectedStructNameForTarget
     */
    TypeMapperDefinition.prototype.setSelectedStructNameForTarget = function (selectedStructNameForTarget, options) {
        if (!_.isNil(selectedStructNameForTarget)) {
            this.setAttribute('_selectedTypeStructNameForTarget', selectedStructNameForTarget, options);
        } else {
            log.error('Invalid TypeStructName [' + selectedStructNameForTarget + '] Provided');
            throw 'Invalid TypeStructName [' + selectedStructNameForTarget + '] Provided';
        }
    };

    /**
     * Returns the selected type struct name for target
     * @returns {string} type struct name for target
     */
    TypeMapperDefinition.prototype.getSelectedStructNameForTarget = function () {
        return this._selectedTypeStructNameForTarget;
    };

    /**
     * removes the already selected child before adding a new child
     * @param type
     */
    TypeMapperDefinition.prototype.removeTypeStructDefinition = function (type) {
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        var selectedTypeDef = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isTypeStructDefinition(child)
                && child.getCategory() === type;
        });

        if (!_.isUndefined(selectedTypeDef)) {
            this.removeChild(selectedTypeDef);
        }
    };

    /**
     * removes the already selected child before adding a new child
     * @param sourceProperty
     * @param targetProperty
     */
    TypeMapperDefinition.prototype.removeAssignmentDefinition = function (sourceProperty, targetProperty) {
        //TODO: Get rid of hardcoded x and y
        var self = this;
        var ballerinaASTFactory = this.getFactory();
        var assignmentStatement = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isAssignmentStatement(child) &&
                (('x.' + targetProperty + ' = ' + 'y.' + sourceProperty) === child.getStatementString());
        });
        if (!_.isUndefined(assignmentStatement)) {
            this.removeChild(assignmentStatement);
        }
    };

    /**
     * returns the index of the selected struct
     * @param structArray
     * @param selectedStructName
     */
    TypeMapperDefinition.prototype.getSelectedStructIndex = function (structArray, selectedStructName) {
        return _.findIndex(structArray, function (child) {
            return child._structName === selectedStructName;
        });
    };

    /**
     * Adds new type struct definition.
     * @param {string} typeStructName
     * @param {string} identifier
     * @param {object} onConnectFunctionReference
     * @param {object} onDisConnectFunctionReference
     */
    TypeMapperDefinition.prototype.addTypeStructDefinitionChild = function (typeStructName, identifier,onConnectFunctionReference,onDisConnectFunctionReference) {

        // Creating new type struct definition.
        var newTypeStructDef = this.getFactory().createTypeStructDefinition();
        newTypeStructDef.setTypeStructName(typeStructName);
        newTypeStructDef.setIdentifier(identifier);
        newTypeStructDef.setOnConnectInstance(onConnectFunctionReference);
        newTypeStructDef.setOnDisconnectInstance(onDisConnectFunctionReference);
        this.addChild(newTypeStructDef);
    };

    /**
     * Adds new resource parameter.
     * @param {string} typeStructName
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addResourceParameterChild = function (typeStructName, identifier) {

        // Creating a new ResourceParameter.
        var newResourceParameter = this.getFactory().createResourceParameter();
        //todo check setting annotations
        var newStructType =this.getFactory().createStructType();
        newStructType.setTypeName(typeStructName);
        var newSymbolName =this.getFactory().createSymbolName();
        newSymbolName.setName(identifier);
        newResourceParameter.addChild(newStructType);
        newResourceParameter.addChild(newSymbolName);
        this.addChild(newResourceParameter);
    };

    /**
     * Adds new return type.
     * @param {string} typeStructName
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addReturnTypeChild = function (typeStructName, identifier) {

        // Creating a new ResourceParameter.
        var newReturnType = this.getFactory().createReturnType();
        //todo check setting annotations
        var newStructType =this.getFactory().createStructType();
        newStructType.setTypeName(typeStructName);
        var newSymbolName =this.getFactory().createSymbolName();
        newSymbolName.setName(identifier);
        newReturnType.addChild(newStructType);
        newReturnType.addChild(newSymbolName);
        this.addChild(newReturnType);
    };

    /**
     * Adds new variable definition statement.
     * @param {string} typeStructName
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addVariableDefinitionStatement = function (typeStructName, identifier) {

        // Creating a new ResourceParameter.
        var newReturnType = this.getFactory().createReturnType();
        //todo check setting annotations
        var newStructType =this.getFactory().createStructType();
        newStructType.setTypeName(typeStructName);
        var newSymbolName =this.getFactory().createSymbolName();
        newSymbolName.setName(identifier);
        newReturnType.addChild(newStructType);
        newReturnType.addChild(newSymbolName);
        this.addChild(newReturnType);
    };

    /**
     * Adds new statement.
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addStatement = function (identifier) {

        // Creating a new Statement.
        var newStatement = this.getFactory().createStatement();
        //todo check setting annotations
        var newReturnStatement =this.getFactory().createReturnStatement();
        newReturnStatement.setReturnExpression(identifier);
        newStatement.addChild(newReturnStatement);
        this.addChild(newStatement);
    };

    /**
     * Constructs new assignment statement.
     * @param {string} sourceIdentifier
     * @param {string} targetIdentifier
     * @param {string} sourceValue
     * @param {string} targetValue
     * @returns {AssignmentStatement}
     */
    TypeMapperDefinition.prototype.returnConstuctedAssignmentStatement = function (sourceIdentifier,targetIdentifier,sourceValue,targetValue) {

        // Creating a new Assignment Statement.
        var newAssignmentStatement = this.getFactory().createAssignmentStatement();
        var newExpression = this.getFactory().createExpression();

        var sourceStructFieldAccessExpression = this.getFactory().createStructFieldAccessExpression();
        var sourceVariableReferenceExpression = this.getFactory().createVariableReferenceExpression();
        sourceVariableReferenceExpression.setVariableReferenceName(sourceIdentifier);
        var sourceFieldExpression = this.getFactory().createFieldExpression();
        sourceFieldExpression.setFieldName(sourceValue);
        sourceStructFieldAccessExpression.addChild(sourceVariableReferenceExpression);
        sourceStructFieldAccessExpression.addChild(sourceFieldExpression);

        newExpression.addChild(sourceStructFieldAccessExpression);

        var targetStructFieldAccessExpression = this.getFactory().createStructFieldAccessExpression();
        var targetVariableReferenceExpression = this.getFactory().createVariableReferenceExpression();
        targetVariableReferenceExpression.setVariableReferenceName(targetIdentifier);
        var targetFieldExpression = this.getFactory().createFieldExpression();
        targetFieldExpression.setFieldName(targetValue);

        newAssignmentStatement.addChild(newExpression);
        newAssignmentStatement.addChild(targetStructFieldAccessExpression);

        return newAssignmentStatement;
    };

    /**
     * Adds assignmentStatement to statement
     * @param assignmentStatement
     */
    TypeMapperDefinition.prototype.addAssignmentStatement = function (assignmentStatement) {

        // returns child Statement.
        var ballerinaASTFactory = this.getFactory();
        var statement = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isStatement(child);
        });

        if(!_.isUndefined(statement)){
            statement.addChild(assignmentStatement);
        }
    };

    return TypeMapperDefinition;
});