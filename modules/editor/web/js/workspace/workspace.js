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
define(['jquery', 'lodash', 'backbone', 'log', 'dialogs', 'welcome-page', 'tab','alerts', './service-client', 'bootstrap'],
    function ($, _, Backbone, log, Dialogs, WelcomePages, Tab, alerts, ServiceClient) {

    // workspace manager constructor
    /**
     * Arg: application instance
     */
    return function (app) {
        var self = this;

        this._serviceClient = new ServiceClient({application: app});

        if (_.isUndefined(app.commandManager)) {
            var error = "CommandManager is not initialized.";
            log.error(error);
            throw error;
        }

        this.getServiceClient = function(){
            return this._serviceClient;
        };

        this.listenToTabController = function(){
            app.tabController.on("active-tab-changed", this.onTabChange, this);
        };

        this.onTabChange = function(evt){
            this.updateMenuItems();
        };

        this.createNewTab = function createNewTab(options) {
            app.tabController.newTab(options);
        };

        this.displayInitialTab = function () {
            //TODO : remove this if else condition
            // display first launch welcome page tab
            if (!this.passedFirstLaunch()) {
                // create a generic tab - without ballerina editor components
                var tab = app.tabController.newTab({
                    tabModel: Tab.Tab,
                    tabOptions:{title: 'welcome-page'}
                });
                var opts = _.get(app.config, 'welcome');
                _.set(opts, 'application', app);
                _.set(opts, 'tab', tab);
                this.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                this.welcomePage.render();
            } else {
                // user has no active tabs from last session
                if (!app.tabController.hasFilesInWorkingSet()) {
                    // create a generic tab - without ballerina editor components
                    var tab = app.tabController.newTab({
                        tabModel: Tab.Tab,
                        tabOptions:{title: 'welcome-page'}
                    });
                    // Showing FirstLaunchWelcomePage instead of regularWelcomePage
                    var opts = _.get(app.config, 'welcome');
                    _.set(opts, 'application', app);
                    _.set(opts, 'tab', tab);
                    this.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                    this.welcomePage.render();
                }
            }
        };

        this.passedFirstLaunch = function(){
            return app.browserStorage.get("pref:passedFirstLaunch") || false;
        };

        /**
         * Showing the welcome page. If "welcome page" exists, then show existing "welcome page", else create new
         * "welcome page".
         * @param workspaceManager - The workspace manager.
         */
        this.showWelcomePage = function(workspaceManager) {
            var existingWelcomeTab = _.find(app.tabController.getTabList(), function (tab) {
                return tab._title == "welcome-page";
            });

            if (_.isUndefined(existingWelcomeTab)) {
                // Creating a new welcome tab.
                var tab = app.tabController.newTab({
                    tabModel: Tab.Tab,
                    tabOptions:{title: 'welcome-page'}
                });
                // Showing FirstLaunchWelcomePage instead of regularWelcomePage
                var opts = _.get(app.config, 'welcome');
                _.set(opts, 'application', app);
                _.set(opts, 'tab', tab);
                workspaceManager.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                workspaceManager.welcomePage.render();
            } else {
                // Showing existing welcome tab.
                app.tabController.setActiveTab(existingWelcomeTab);
            }
        };

        this.openFileSaveDialog = function openFileSaveDialog() {
            if(_.isNil(this._saveFileDialog)){
                this._saveFileDialog = new Dialogs.save_to_file_dialog(app);
            }
            this._saveFileDialog.render();
            this._saveFileDialog.show();
            var activeTab = app.tabController.getActiveTab();
            if(!_.isNil(activeTab) && _.isFunction(activeTab.getFile)){
                var activeFile = activeTab.getFile();
                if(activeFile.isPersisted()){
                    this._saveFileDialog.once('loaded', function(){
                        this._saveFileDialog.setSelectedFile(activeFile.getPath(), activeFile.getName());
                    }, this);
                }
            }

        };

        this.showFolderOpenDialog = function() {
            if(_.isNil(this._folderOpenDialog)){
                var opts = _.cloneDeep(_.get(app.config, 'open_folder_dialog'));
                _.set(opts, "application", app);
                this._folderOpenDialog = new Dialogs.FolderOpenDialog(opts);
            }
            this._folderOpenDialog.render();
            this._folderOpenDialog.show();
        };

        this.openFileOpenDialog = function openFileOpenDialog() {
            if(_.isNil(this._openFileDialog)){
                this._openFileDialog = new Dialogs.open_file_dialog(app);
                this._openFileDialog.render();
            }
            this._openFileDialog.show();
        };

        this.goToWelcomePage = function goToWelcomePage() {
            this.workspaceManager.showWelcomePage(this.workspaceManager);
        };

        this.updateUndoRedoMenus = function(){
            // undo manager for current tab
            var activeTab = app.tabController.getActiveTab(),
                undoMenuItem = app.menuBar.getMenuItemByID('edit.undo'),
                redoMenuItem = app.menuBar.getMenuItemByID('edit.redo');

            if(activeTab instanceof Tab.FileTab){
                var fileEditor = activeTab.getBallerinaFileEditor();
                if(!_.isUndefined(fileEditor)){
                    var undoManager = activeTab.getBallerinaFileEditor().getUndoManager();
                    if (undoManager.hasUndo()) {
                        undoMenuItem.enable();
                        undoMenuItem.addLabelSuffix(
                            undoManager.undoStackTop().getTitle());
                    } else {
                        undoMenuItem.disable();
                        undoMenuItem.clearLabelSuffix();
                    }
                    if (undoManager.hasRedo()) {
                        redoMenuItem.enable();
                        redoMenuItem.addLabelSuffix(
                            undoManager.redoStackTop().getTitle());
                    } else {
                        redoMenuItem.disable();
                        redoMenuItem.clearLabelSuffix();
                    }
                }
            } else {
                undoMenuItem.disable();
                undoMenuItem.clearLabelSuffix();
                redoMenuItem.disable();
                redoMenuItem.clearLabelSuffix();
            }
        };

        this.updateMenuItems = function(){
            this.updateUndoRedoMenus();
            this.updateSaveMenuItem();
        };

        this.updateSaveMenuItem = function(){
            var activeTab = app.tabController.getActiveTab(),
                saveMenuItem = app.menuBar.getMenuItemByID('file.save'),
                saveAsMenuItem = app.menuBar.getMenuItemByID('file.saveAs');
            if(activeTab instanceof Tab.FileTab){
                var file = activeTab.getFile();
                if(file.isDirty()){
                    saveMenuItem.enable();
                    saveAsMenuItem.enable();
                } else {
                    saveMenuItem.disable();
                }
            } else {
                saveMenuItem.disable();
                saveAsMenuItem.disable();
            }
        };

        this.handleUndo = function() {
            // undo manager for current tab
            var undoManager = app.tabController.getActiveTab().getBallerinaFileEditor().getUndoManager();
            if (undoManager.hasUndo()) {
                undoManager.undo();
            }
            self.updateUndoRedoMenus();
        };

        this.handleRedo = function() {
            // undo manager for current tab
            var undoManager = app.tabController.getActiveTab().getBallerinaFileEditor().getUndoManager();
            if (undoManager.hasRedo()) {
                undoManager.redo();
            }
            self.updateUndoRedoMenus();
        };

        this.handleSave = function() {
            var activeTab = app.tabController.getActiveTab();
            if(activeTab instanceof Tab.FileTab){
                var file = activeTab.getFile();
                if(file.isPersisted()){
                    if(file.isDirty()){
                        self._serviceClient.writeFile(file);
                        if(activeTab.getBallerinaFileEditor().isInSourceView()){
                            activeTab.getBallerinaFileEditor().getSourceView().markClean();
                        }
                    }
                } else {
                    app.commandManager.dispatch('open-file-save-dialog');
                }
            }
        };

        this.showAboutDialog = function(){
            var aboutModal = $(_.get(app, 'config.about_dialog.selector'));
            aboutModal.modal('show')
        };

        this.handleCreateNewItemAtPath = function(data){
            if(_.isNil(this._newItemDialog)){
                this._newItemDialog = new Dialogs.NewItemDialog({application: app});
                this._newItemDialog.render();
            }
            this._newItemDialog.displayWizard(data);
        };

        this.handleRemoveFromDisk = function(data){
            if(_.isNil(this._deleteItemWizard)){
                this._deleteItemWizard = new Dialogs.DeleteItemDialog({application: app});
                this._deleteItemWizard.render();
            }
            this._deleteItemWizard.displayWizard(data);
        };

        app.commandManager.registerHandler('create-new-tab', this.createNewTab);

        app.commandManager.registerHandler('undo', this.handleUndo);

        app.commandManager.registerHandler('redo', this.handleRedo);

        app.commandManager.registerHandler('save', this.handleSave);

        // Open file save dialog
        app.commandManager.registerHandler('open-file-save-dialog', this.openFileSaveDialog, this);

        // Open file open dialog
        app.commandManager.registerHandler('open-file-open-dialog', this.openFileOpenDialog, this);

        app.commandManager.registerHandler('show-folder-open-dialog', this.showFolderOpenDialog, this);

        // Go to Welcome Page.
        app.commandManager.registerHandler('go-to-welcome-page', this.goToWelcomePage);

        app.commandManager.registerHandler('show-about-dialog', this.showAboutDialog);

        app.commandManager.registerHandler('create-new-item-at-path', this.handleCreateNewItemAtPath, this);

        app.commandManager.registerHandler('remove-from-disk', this.handleRemoveFromDisk, this);

    }

});

