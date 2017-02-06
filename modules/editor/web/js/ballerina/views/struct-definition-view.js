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
define(['lodash', 'log', 'd3', 'alerts', './ballerina-view', 'ballerina/ast/ballerina-ast-factory', './canvas',
        '../utils/dropdown', './../ast/node', './struct-variable-defintion-view'],
    function (_, log, d3, Alerts, BallerinaView, BallerinaASTFactory,
              Canvas, Dropdown, ASTNode, StructVariableDefinitionView) {
        var StructDefinitionView = function (args) {
            Canvas.call(this, args);

            this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
            this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
            //set panel icon for the struct
            this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.struct_icon");
            //set initial height for the struct container svg
            this._totalHeight = 30;
        };

        StructDefinitionView.prototype = Object.create(Canvas.prototype);
        StructDefinitionView.prototype.constructor = Canvas;

        StructDefinitionView.prototype.canVisitStructDefinition = function (structDefinition) {
            return true;
        };

        /**
         * Rendering the view of the Struct definition.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        StructDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);

            // Draws the outlying body of the struct definition.
            this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().getType().toLowerCase(), this.getModel().getStructName());

            // Setting the styles for the canvas icon.
            this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.struct_icon", ""));

            var self = this;

            $(this.getTitle()).text(this.getModel().getStructName())
                .on("change paste keyup", function () {
                    self.getModel().setStructName($(this).text());
                }).on("click", function (event) {
                event.stopPropagation();
            }).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    event.stopPropagation();
                    return false;
                }

                var newServiceName = $(this).val() + String.fromCharCode(enteredKey);

                try {
                    self.getModel().setStructName(newServiceName);
                } catch (error) {
                    Alerts.error(error);
                    event.stopPropagation();
                    return false;
                }
            });

            var structContentWrapper = $("<div/>", {
                id: this.getModel().getID(),
                class: "struct-content-wrapper"
            }).data("model", this.getModel()).appendTo(this.getBodyWrapper());

            //// Creating operational panel

            var structOperationsWrapper = $("<div/>", {
                class: "struct-content-operations-wrapper"
            }).appendTo(structContentWrapper);

            var typeDropdown = new Dropdown({
                class: {mainWrapper: "struct-type-dropdown-wrapper"},
                emptyValue: "Type",
                onDropdownOpen: function() {
                    self.getBodyWrapper().css("height", $(self.getBodyWrapper()).height());
                    self.getBodyWrapper().css("overflow-x", "visible");
                    $(self.getBodyWrapper()).closest(".canvas-container").css("overflow", "visible");
                },
                onDropdownClosed: function() {
                    self.getBodyWrapper().css("height", "");
                    self.getBodyWrapper().css("overflow-x", "");
                    $(self.getBodyWrapper()).closest(".canvas-container").css("overflow", "");
                }
            });
            typeDropdown.getElement().appendTo(structOperationsWrapper);

            // Adding items to the type dropdown.
            var bTypes = this.getDiagramRenderingContext().getEnvironment().getTypes();
            _.forEach(bTypes, function (bType) {
                typeDropdown.addItem({key: bType, value: bType});
            });

            // Creating the identifier text box.
            var identifierTextBox = $("<input/>", {
                type: "text",
                class: "struct-identifier-text-input",
                "placeholder": "Identifier"
            }).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Adding new variable upon enter key.
                if (enteredKey == 13) {
                    addStructVariableButton.click();
                    event.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                // Validation the identifier against grammar.
                if (!ASTNode.isValidIdentifier(newIdentifier)) {
                    var errorString = "Invalid identifier for a variable: " + newIdentifier;
                    Alerts.error(errorString);
                    event.stopPropagation();
                    return false;
                }
            }).appendTo(structOperationsWrapper);

            // Creating cancelling add new constant button.
            var addStructVariableButton = $("<div class='add-struct-variable-button pull-left'/>")
                .appendTo(structOperationsWrapper);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-check fw-stack-1x fw-inverse add-struct-variable-button-square'></i></span>").appendTo(addStructVariableButton);

            $(addStructVariableButton).click(function () {
                try {
                    var bType = typeDropdown.getSelectedValue();
                    var identifier = $(identifierTextBox).val().trim();

                    self.getModel().addVariableDeclaration(bType, identifier);

                    self._renderVariableDeclarations(structVariablesWrapper);

                    $(identifierTextBox).val("");
                } catch (e) {
                    Alerts.error(e);
                }
            });

            // Creating add new constant button.
            var clearFieldsButton = $("<div class='clear-struct-variable-button pull-left'/>").appendTo(structOperationsWrapper);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-cancel fw-stack-1x fw-inverse clear-struct-variable-button-square'></i></span>").appendTo(clearFieldsButton);

            $(clearFieldsButton).click(function(){
                $(identifierTextBox).val("");
            });

            //// End of operational panel.

            //// Creating struct content panel

            var structVariablesWrapper = $("<div/>",{
                class: "struct-content-variables-wrapper"
            }).appendTo(structContentWrapper);

            this._renderVariableDeclarations(structVariablesWrapper);

            $(structVariablesWrapper).click(function(e){
                e.preventDefault();
                return false;
            });

            //// End of struct content panel

            // On window click.
            $(window).click(function (event) {
                self._renderVariableDeclarations(structVariablesWrapper);
            });
        };

        StructDefinitionView.prototype._renderVariableDeclarations = function (wrapper) {
            $(wrapper).empty();
            var self = this;

            _.forEach(this._model.getVariableDeclarations(), function(variableDeclaration) {

                var variableDeclarationView = new StructVariableDefinitionView({
                    parent: self.getModel(),
                    model: variableDeclaration,
                    container: wrapper,
                    toolPalette: self.getToolPalette(),
                    messageManager: self.getMessageManager(),
                    parentView: self
                });

                self.getDiagramRenderingContext().getViewModelMap()[variableDeclaration.id] = variableDeclarationView;

                variableDeclarationView.render(self.getDiagramRenderingContext());

                $(variableDeclarationView.getDeleteButton()).click(function () {
                    self._renderVariableDeclarations(wrapper);
                });

                $(variableDeclarationView.getWrapper()).dblclick({
                    modelID: variableDeclaration.getID()
                }, function (event) {
                    self._renderVariableDeclarations(wrapper);
                    var variableDeclarationView = self.getDiagramRenderingContext()
                        .getViewModelMap()[event.data.modelID];
                    variableDeclarationView.renderEditView();
                });
            });
        };

        return StructDefinitionView;
    });