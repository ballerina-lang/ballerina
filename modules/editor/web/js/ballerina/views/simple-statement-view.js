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

define(
    ['lodash', 'log', './ballerina-statement-view', 'd3utils', 'd3'],
    function (_, log, BallerinaStatementView, D3Utils, d3) {

        /**
         * Simple statement.
         * @param args {*} arguments for the creating view
         * @constructor
         */
        var SimpleStatementView = function (args) {
            BallerinaStatementView.call(this, args);

            var viewOptions = this.getViewOptions();
            viewOptions.height = _.get(args, "viewOptions.height", 30);
            viewOptions.width = _.get(args, "viewOptions.width", 120); // starting width
            viewOptions.minWidth = _.get(args, "viewOptions.minWidth", 120); // minimum width
            viewOptions.maxWidth = _.get(args, "viewOptions.maxWidth", 300); // maximum width
            viewOptions.textPadding = _.get(args, "viewOptions.textPadding", {left: 5, right: 5, top: 0, bottom: 0});

            this.getBoundingBox().fromTopCenter(this.getTopCenter(), viewOptions.width, viewOptions.height);
        };

        SimpleStatementView.prototype = Object.create(BallerinaStatementView.prototype);

        SimpleStatementView.prototype.constructor = SimpleStatementView;

        SimpleStatementView.prototype.render = function (renderingContext) {
            this.setDiagramRenderingContext(renderingContext);
            var bBox = this.getBoundingBox();

            // Creating statement group.
            var statementGroup = D3Utils.group(d3.select(this._container));
            // "id" is prepend with a "_" to be compatible with HTML4
            statementGroup.attr("id", "_" + this.getModel().id);
            var svgRect = D3Utils.rect(bBox.getLeft(), bBox.getTop(), bBox.w(), bBox.h(), 0, 0, statementGroup)
                                 .classed('statement-rect', true);
            var svgText = D3Utils.textElement(bBox.getCenterX(), bBox.getCenterY(), "", statementGroup)
                                 .classed('statement-text', true);
            statementGroup.outerRectElement = svgRect;
            statementGroup.displayTextElement = svgText;
            this.setStatementGroup(statementGroup);

            // Registering event listeners.
            bBox.on('top-edge-moved', function (dy) {
                svgRect.attr('y', parseFloat(svgRect.attr('y')) + dy);
                svgText.attr('y', parseFloat(svgText.attr('y')) + dy);
            });
            bBox.on('width-changed', function (dw) {
                svgRect.attr('width', parseFloat(svgRect.attr('width')) + dw);
            });
            bBox.on('left-edge-moved', function (dx) {
                svgRect.attr('x', parseFloat(svgRect.attr('x')) + dx);
            });
        };

        /**
         * Renders the display text of this simple statement.
         * @param displayText {string} text to be rendered
         */
        SimpleStatementView.prototype.renderDisplayText = function (displayText) {
            var boundingBox = this.getBoundingBox();
            var viewOptions = this.getViewOptions();
            var minWidth = viewOptions.minWidth, maxWidth = viewOptions.maxWidth;
            var leftTextPadding = viewOptions.textPadding.left, rightTextPadding = viewOptions.textPadding.right;
            var textElement = this.getStatementGroup().displayTextElement.node();
            textElement.textContent = displayText;

            /*
             +------------------------------------------------------------+
             |<--leftTextPadding--><--displayText--><--rightTextPadding-->|
             +------------------------------------------------------------+
             */
            var displayTextWidth = leftTextPadding + textElement.getComputedTextLength() + rightTextPadding;
            if (displayTextWidth < minWidth) {
                // Text hasn't exceeded the minimum width of the bounding box. Hence no need to increase width.
                boundingBox.zoomWidth(minWidth);
            } else {
                if (displayTextWidth < maxWidth) {
                    // We can safely expand width as the text length is still less than maximum width of the box.
                    boundingBox.zoomWidth(displayTextWidth);
                } else {
                    // We need to truncate displayText and show an ellipses at the end.
                    var ellipses = "...";
                    var possibleCharactersCount = 0;
                    for (var i = (displayText.length - 1); 1 < i; i--) {
                        if ((leftTextPadding + textElement.getSubStringLength(0, i) + rightTextPadding) < maxWidth) {
                            possibleCharactersCount = i;
                            break;
                        }
                    }
                    // We need room for the ellipses as well, hence removing 'ellipses.length' no. of characters.
                    textElement.textContent = displayText.substring(0, (possibleCharactersCount - ellipses.length))
                                              + ellipses; // Appending ellipses.
                    boundingBox.zoomWidth(maxWidth);
                }
            }
        };

        SimpleStatementView.prototype.setModel = function (model) {
            if (_.isNil(model)) {
                var message = "Model of a simple statement cannot be null.";
                log.error(message);
                throw new Error(message);
            } else {
                this._model = model;
            }
        };

        SimpleStatementView.prototype.getModel = function () {
            return this._model;
        };

        SimpleStatementView.prototype.setContainer = function (container) {
            if (_.isNil(container)) {
                var message = "Container of a simple statement cannot be null.";
                log.error(message);
                throw new Error(message);
            } else {
                this._container = container;
            }
        };

        SimpleStatementView.prototype.getContainer = function () {
            return this._container;
        };

        SimpleStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        SimpleStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        return SimpleStatementView;
    });
