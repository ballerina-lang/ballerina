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

define(['log', 'jquery', 'backbone', 'lodash', './explorer-item', './service-client', 'nano_scroller'],

    function (log, $, Backbone, _, ExplorerItem, ServiceClient) {

    var WorkspaceExplorer = Backbone.View.extend({

        initialize: function (config) {
            var errMsg;
            if (!_.has(config, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(config, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for file browser with selector: ' + _.get(config, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;

            if(!_.has(config, 'application')){
                log.error('Cannot init file browser. config: application not found.')
            }
            this.application = _.get(config, 'application');
            this._options = config;
            this.workspaceServiceURL = _.get(this._options, 'application.config.services.workspace.endpoint');
            this._isActive = false;
            this._lastWidth = undefined;
            this._verticalSeparator = $(_.get(this._options, 'separator'));
            this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));
            this._openedFolders = this.application.browserStorage.get("file-explorer:openedFolders")||[];
            this._items = [];

            this._serviceClient = new ServiceClient({application: this.application});

            // register command
            this.application.commandManager.registerCommand(config.command.id, {shortcuts: config.command.shortcuts});
            this.application.commandManager.registerHandler(config.command.id, this.toggleExplorer, this);

            this.application.commandManager.registerCommand("open-folder", {});
            this.application.commandManager.registerHandler("open-folder", this.openFolder, this);

            this.application.commandManager.registerCommand("open-file", {});
            this.application.commandManager.registerHandler("open-file", this.openFile, this);
        },

        openFolder: function(folderPath){
            // this is the first folder to open
            if(_.isEmpty(this._openedFolders)){
                this._openFolderBtn.hide();
            }
            this._openedFolders.push(folderPath);
            this.createExplorerItem(folderPath);
            this.persistState();
        },

        openFile: function(filePath){
            var file = this._serviceClient.readFile(filePath);
            var currentTabForFile = this.application.tabController.getTabForFile(file);
            if(!_.isNil(currentTabForFile)){
                this.application.tabController.setActiveTab(currentTabForFile);
                return;
            }
            this.application.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
        },

        createExplorerItem: function(folderPath){
            var opts = {};
            _.set(opts, "application", this.application);
            _.set(opts, "path", folderPath);
            _.set(opts, "index", this._items.length - 1);
            _.set(opts, "container", this._explorerContainer);
            var explorerItem = new ExplorerItem(opts);
            explorerItem.render();
            this._items.push(explorerItem);
        },

        persistState: function(){
            this.application.browserStorage.put("file-explorer:openedFolders", this._openedFolders);
        },

        isEmpty: function(){
              return _.isEmpty(this._openedFolders);
        },

        isActive: function(){
              return this._isActive;
        },

        toggleExplorer: function(){
            if(this._isActive){
                this._$parent_el.parent().width('0px');
                this._containerToAdjust.css('margin-left', _.get(this._options, 'leftOffset'));
                this._verticalSeparator.css('left', _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
                this._isActive = false;
            } else {
                var width = this._lastWidth || _.get(this._options, 'defaultWidth');
                this._$parent_el.parent().width(width);
                this._containerToAdjust.css('margin-left', width + _.get(this._options, 'leftOffset'));
                this._verticalSeparator.css('left',  width + _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
                this._isActive = true;
            }
        },

        render: function () {
            var self = this;
            var activateBtn = $(_.get(this._options, 'activateBtn'));

            var explorerContainer = $('<div></div>');
            explorerContainer.addClass(_.get(this._options, 'cssClass.container'));
            this._$parent_el.append(explorerContainer);

            activateBtn.on('click', function(){
                self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
            });
            if (this.application.isRunningOnMacOS()) {
                activateBtn.attr("title", "Open file explorer (" + _.get(self._options, 'command.shortcuts.mac.label') + ") ")
            } else {
                activateBtn.attr("title", "Open file explorer  (" + _.get(self._options, 'command.shortcuts.other.label') + ") ")
            }

            this._verticalSeparator.on('drag', function(event){
                if( event.originalEvent.clientX >= _.get(self._options, 'resizeLimits.minX')
                    && event.originalEvent.clientX <= _.get(self._options, 'resizeLimits.maxX')){
                    self._verticalSeparator.css('left', event.originalEvent.clientX - _.get(self._options, 'separatorOffset'));
                    self._verticalSeparator.css('cursor', 'ew-resize');
                    var newWidth = event.originalEvent.clientX -  _.get(self._options, 'leftOffset');
                    self._$parent_el.parent().width(newWidth);
                    self._containerToAdjust.css('margin-left', event.originalEvent.clientX);
                    self._lastWidth = newWidth;
                    self._isActive = true;
                }
                event.preventDefault();
                event.stopPropagation();
            });

            if(_.isEmpty(this._openedFolders)){
                var openFolderBtn = $("<button></button>");
                    openFolderBtn.attr("type", "button");
                    openFolderBtn.text("Open Folder");
                    openFolderBtn.addClass(_.get(this._options, 'cssClass.openFolderButton'));
                    openFolderBtn.click(function(){
                        self.application.commandManager.dispatch("show-folder-open-dialog");
                    });
                    this._openFolderBtn = openFolderBtn;
                explorerContainer.append(openFolderBtn);
            }
            this._explorerContainer = explorerContainer;

            if(!_.isEmpty(this._openedFolders)){
                this._openedFolders.forEach(function(folder){
                    self.createExplorerItem(folder);
                })
            }
            return this;
        }
    });

    return WorkspaceExplorer;

});
