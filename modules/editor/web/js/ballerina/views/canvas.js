
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
define(['log', 'lodash', 'jquery', 'd3', 'd3utils', './../visitors/ast-visitor', './ballerina-view', './message-manager', 'mcustom_scroller'],
    function(log, _, $, d3, D3Utils, AstVisitor, BallerinaView, MessageManager, mcustomScroller){

    var Canvas = function(args) {
        var mMArgs = {'canvas': this};
        args.messageManager = new MessageManager(mMArgs);
        BallerinaView.call(this, args);
        this.init();
    };

    Canvas.prototype = Object.create(BallerinaView.prototype);
    Canvas.prototype.constructor = Canvas;

    Canvas.prototype.init = function () {
        this.on('remove-view', this.removeViewCallback, this);
        this._model.on('child-removed', this.childRemovedCallback, this);
    };

    Canvas.prototype.getSVG = function () {
        return this._svg;
    };

    Canvas.prototype.getMainWrapper = function () {
        return this._mainSVGGroup;
    };

    Canvas.prototype.getOperationsPane = function () {
        return this._canvasOperationsWrapper;
    };

    /**
     * Since canvas by default init a drop area within content area. Hence, all the subclasses need a way to override
     * allowed type of node to drop there.
     * This method is to achieve that extensibility. Override this in subclasses to allow only a certain type of nodes
     * to drop.
     *
     * @param node {ASTNode} node which is being dragged ATM
     * @return {boolean}
     */
    Canvas.prototype.isAValidNodeForCanvasDropArea = function (node) {
        return true;
    };

    Canvas.prototype.drawAccordionCanvas = function (parent, options, id, name, title) {
        var svgContainer = $('<div style="position:relative; top:0; right:0;"></div>');
        svgContainer.attr('id', id);
        svgContainer.attr('name', name);
        svgContainer.addClass(_.get(options, 'cssClass.outer_box'));
        var canvas = svgContainer;
        var svg = $('<svg class="service-container"></svg>');
        svgContainer.append(svg);
        this._rootGroup = D3Utils.group(d3.select(svg.get(0)));
        this._svg = svg;
        // Set the initial service container height to 300px
        this.setServiceContainerHeight(300);
        //draw a collapse accordion
        var outerDiv = $('<div></div>');
        outerDiv.attr('id', '_'+canvas[0].id);//to support HTML4
        outerDiv.addClass(_.get(options, 'cssClass.outer_div'));
        var panelHeading = $('<div></div>');
        panelHeading.attr('id', canvas[0].id + 3).attr('role', 'tab');
        panelHeading.attr('role', 'button').attr('data-toggle', 'collapse').attr('data-parent', "#accordion").attr('href', '#' + canvas[0].id).attr('aria-expanded', 'false').attr('aria-controls', canvas[0].id);
        var panelTitle = $('<h4></h4>');
        panelTitle.addClass(_.get(options, 'cssClass.panel_title'));
        var panelIcon = $('<i></i>');
        panelIcon.addClass(_.get(options, 'cssClass.panel_icon'));
        panelIcon.addClass(_.get(options, 'panelIcon'));
        panelTitle.append(panelIcon);
        var titleLink = $('<a></a>');
        titleLink.attr('id', 'title-' + id);
        titleLink[0].setAttribute("contenteditable", "true");
        titleLink[0].setAttribute("spellcheck", "false");
        titleLink.focus();
        titleLink.blur();
        if (title !== undefined) {
            titleLink.append("&nbsp;" + title);
        }
        titleLink.addClass(_.get(options, 'cssClass.title_link'));
        //TODO: update href,aria-controls
        panelTitle.append(titleLink);

        this._canvasOperationsWrapper = $("<div class='canvas-operations-wrapper'/>");

        panelTitle.append(this._canvasOperationsWrapper);

        // Creating collapsable icon.
        var panelRightIcon = $("<i/>", {
            class: _.get(options, 'cssClass.panel_right_icon'),
            title:"Collapse pane"
        }).appendTo(this._canvasOperationsWrapper).tooltip();

        $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(this._canvasOperationsWrapper);

        // Creating delete icon.
        var panelDeleteIcon = $("<i/>", {
            class: _.get(options, 'cssClass.panel_delete_icon'),
            title:"Delete"
        }).appendTo(this._canvasOperationsWrapper).tooltip();

        $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(this._canvasOperationsWrapper);

        panelHeading.append(panelTitle);

        panelHeading.click(function() {
            $(this).find('i.collapser').toggleClass('fw-down fw-up');
        });

        var bodyDiv = $('<div></div>');
        bodyDiv.addClass(_.get(options, 'cssClass.body_div'));
        bodyDiv.attr('id', canvas[0].id).attr('aria-labelledby', canvas[0].id + 3).attr('role', 'tabpanel').attr('class', 'collapse in');
        bodyDiv.addClass(_.get(options, 'cssClass.canvas'));
        bodyDiv.append(canvas);

        outerDiv.append(panelHeading);
        outerDiv.append(bodyDiv);

        // append to parent
        parent.append(outerDiv);

        var self = this,
            dropActiveClass = _.get(options, 'cssClass.design_view_drop');
        outerDiv.mouseover(function(event){

            //if someone is dragging a tool from tool-palette
            if(self.toolPalette.dragDropManager.isOnDrag()){

                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)){
                    return;
                }

                // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                // tool view will use this to provide feedback on impossible drop zones
                self.toolPalette.dragDropManager.setActivatedDropTarget(self._model, function(nodeBeingDragged){
                    return self.isAValidNodeForCanvasDropArea(nodeBeingDragged);
                });

                // indicate drop area
                outerDiv.addClass(dropActiveClass);

                // reset ui feed back on drop target change
                self.toolPalette.dragDropManager.once("drop-target-changed", function(){
                    outerDiv.removeClass(dropActiveClass);
                });
            }
            event.stopPropagation();
        }).mouseout(function(event){
            // reset ui feed back on hover out
            if(self.toolPalette.dragDropManager.isOnDrag()){
                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self._model)){
                    self.toolPalette.dragDropManager.clearActivatedDropTarget();
                }
            }
            event.stopPropagation();
        });

        panelDeleteIcon.click(function (event) {
            log.debug("Clicked delete button");

            event.stopPropagation();

            var child = self._model;
            var parent = child.parent;
            self.trigger("remove-view", parent, child);
        });

        $(svgContainer).mCustomScrollbar({
            theme: "dark",
            axis: "x",
            scrollInertia: 0,
            autoHideScrollbar: true,
            mouseWheel: {
                enable: false
            }
        });
    };

    /**
     * Set canvas container height
     * @param newHeight
     */
    Canvas.prototype.setServiceContainerHeight = function (newHeight) {
        this._svg.attr('height', newHeight);
        this.getBoundingBox().h(newHeight);

        // If service container's height is lesser than the height of the svg
        // Increase the height of the service container and the inner div
        if($(this._container).closest("svg").attr('height')) {
            if ($(this._container).closest(".panel-body").height() < $(this._container).closest("svg").attr('height')) {
                $(this._container).closest(".panel-body").height($(this._container).closest("svg").attr("height"));
                $(this._container).closest(".panel-body").find("#" + $(this._container).closest(".panel-body").attr("id"))
                    .height($(this._container).closest("svg").attr('height'));
            }
        }else{
            if($(this._container).height() < $(this._container).find('svg').attr('height')) {
                $(this._container).height($(this._container).find('svg').attr('height'));
                $(this._container).find("#" + $(this._container).attr('id')).
                    height($(this._container).find('svg').attr('height'));
            }
        }
    };

    /**
     * Set canvas container width
     * @param {number} newWidth
     */
    Canvas.prototype.setServiceContainerWidth = function (newWidth) {
        this._svg.attr('width', newWidth);
        this.getBoundingBox().w(newWidth);
        $(this._container).closest(".panel-body").find(".outer-box").mCustomScrollbar("update");
    };

    Canvas.prototype.getServiceContainer = function () {
        return this._svg;
    };

    /**
     * Override the remove view callback
     * @param {ASTNode} parent - parent node
     * @param {ASTNode} child - child node
     */
    Canvas.prototype.removeViewCallback = function (parent, child) {
        $("#_" +this._model.id).remove();
        this.unplugView(
            {
                w: 0,
                h: 0
            }, parent, child);
    };

    return Canvas;

});