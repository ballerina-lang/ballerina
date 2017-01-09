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

define(['require', 'jquery', 'log', 'backbone', 'file_browser'], function (require, $, log, Backbone, FileBrowser) {
    var SaveToFileDialog = Backbone.View.extend(
        /** @lends SaveToFileDialog.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class SaveToFileDialog
             * @param {Object} config configuration options for the SaveToFileDialog
             */
            initialize: function (options) {
                this.app = options;
                this.dialog_container = _.get(options.config.dialog, 'container');
                this.notification_container = _.get(options.config.tab_controller.tabs.tab.ballerina_editor.notifications, 'container');
            },

            render: function () {
                //TODO : this render method should be rewritten with improved UI
                var fileBrowser;
                var app = this.app;
                var parent = this;
                var notification_container = this.notification_container;

                var fileSave = $(
                    "<div class='modal fade' id='saveConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                    "<div class='modal-dialog file-dialog' role='document'>" +
                    "<div class='modal-content'>" +
                    "<div class='modal-header'>" +
                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                    "<span aria-hidden='true'>&times;</span>" +
                    "</button>" +
                    "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Ballerina Service Save Wizard</h4>" +
                    "<hr class='style1'>"+
                    "</div>" +
                    "<div class='modal-body'>" +
                    "<div class='container-fluid'>" +
                    "<form class='form-horizontal'>" +
                    "<div class='form-group'>" +
                    "<label for='location' class='col-sm-2 file-dialog-label'>Location :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input type='text' class='file-dialog-form-control' id='location' placeholder='eg: /home/user/wso2-integration-server/ballerina-configs'>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<div class='file-dialog-form-scrollable-block'>" +
                    "<div id='fileTree'>" +
                    "</div>" +
                    "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<div class='file-dialog-form-btn'>" +
                    "<button id='createFolderButton' type='button' class='btn btn-file-dialog'>create new folder" +
                    "</button>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label for='configName' class='col-sm-2 file-dialog-label'>File Name :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input class='file-dialog-form-control' id='configName' placeholder='eg: sample.bal'>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<div class='file-dialog-form-btn'>" +
                    "<button id='saveButton' type='button' class='btn btn-file-dialog'>save" +
                    "</button>" +
                    "<div class='divider'/>" +
                    "<button type='cancelButton' class='btn btn-file-dialog' data-dismiss='modal'>cancel</button>" +
                    "</div>" +
                    "</div>" +
                    "</form>" +
                    "<div id='newWizardError' class='alert alert-danger'>" +
                    "<strong>Error!</strong> Something went wrong." +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>"
                );

                var successNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>"+
                    "<span class='notification'>"+
                    "Configuration saved successfully !"+
                    "</span>"+
                    "</div>");

                var errorNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>"+
                    "<span class='notification'>"+
                    "Error while saving configuration !"+
                    "</span>"+
                    "</div>");

                var successFolderCreationNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>"+
                    "<span class='notification'>"+
                    "Location created successfully !"+
                    "</span>"+
                    "</div>");

                var errorFolderCreationNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>"+
                    "<span class='notification'>"+
                    "Error while creating the new location !"+
                    "</span>"+
                    "</div>");


                var saveConfigModal = fileSave.filter("#saveConfigModal");
                var newWizardError = fileSave.find("#newWizardError");
                var location = fileSave.find("input").filter("#location");
                var configName = fileSave.find("input").filter("#configName");

                var treeContainer  = fileSave.find("div").filter("#fileTree")
                fileBrowser = new FileBrowser({container: treeContainer, application:app, action:'saveFile'});

                fileBrowser.render();

                var selectedNode;
                //Gets the selected location from tree and sets the value as location
                this.listenTo(fileBrowser, 'selected', function (selectedLocation) {
                    if(selectedLocation){
                        selectedNode = selectedLocation;
                        location.val(selectedNode);
                    }
                });

                var createdNode;
                fileSave.find("button").filter("#createFolderButton").click(function () {
                    treeContainer.jstree().create_node(selectedNode, null, "last", function (node) {
                        this.edit(node);
                        parent.listenTo(fileBrowser, 'changedName', function (newNode) {
                            if (newNode) {
                                createdNode = newNode;
                                createLocationInFileSystem({location: location, createdNode: createdNode});
                                treeContainer.jstree("refresh");
                            }
                        });
                    });
                });


                fileSave.find("button").filter("#saveButton").click(function() {

                    var _location = location.val();
                    var _configName = configName.val();
                    if (_.isEmpty(_location)) {
                        newWizardError.text("Invalid Value for Location.");
                        newWizardError.show();
                        return;
                    }
                    if (_.isEmpty(_configName)) {
                        newWizardError.text("Invalid Value for File Name.");
                        newWizardError.show();
                        return;
                    }
                    saveConfigModal.modal('hide');
                    saveConfiguration({location: location, configName:configName});
                });


                $(this.dialog_container).append(fileSave);
                newWizardError.hide();
                fileSave.modal('show');

                function alertSuccess(){
                    $(notification_container).append(successNotification);
                    successNotification.fadeTo(2000, 200).slideUp(1000, function(){
                        successNotification.slideUp(1000);
                    });
                };

                function alertError(){
                    $(notification_container).append(errorNotification);
                    errorNotification.fadeTo(2000, 200).slideUp(1000, function(){
                        errorNotification.slideUp(1000);
                    });
                };

                function alertFolderCreationSuccess() {
                    $(notification_container).append(successFolderCreationNotification);
                    successFolderCreationNotification.fadeTo(2000, 200).slideUp(1000, function () {
                        successFolderCreationNotification.slideUp(1000);
                    });
                };

                function alertFolderCreationError() {
                    $(notification_container).append(errorFolderCreationNotification);
                    errorFolderCreationNotification.fadeTo(2000, 200).slideUp(1000, function () {
                        errorFolderCreationNotification.slideUp(1000);
                    });
                };

                function createLocationInFileSystem() {
                    var workspaceServiceURL = "http://localhost:8289/service/workspace";
                    var saveServiceURL = workspaceServiceURL + "/createLocation";
                    var payload = "location=" + (btoa(location.val())) + "&createdNode=" + (btoa(createdNode));

                    $.ajax({
                        url: saveServiceURL,
                        type: "POST",
                        data: payload,
                        contentType: "text/plain; charset=utf-8",
                        async: false,
                        success: function (data, textStatus, xhr) {
                            if (xhr.status == 200) {
                                alertFolderCreationSuccess();
                            } else {
                                alertFolderCreationError();
                            }
                        },
                        error: function (res, errorCode, error) {
                            alertFolderCreationError();
                        }
                    });
                };

                function saveConfiguration() {
                    var workspaceServiceURL = "http://localhost:8289/service/workspace";
                    var saveServiceURL = workspaceServiceURL + "/write";
                    var activeTab = app.tabController.activeTab;
                    var ballerinaFileEditor= activeTab.getBallerinaFileEditor();
                    var config = ballerinaFileEditor.generateSource();
                    var payload = "location=" + btoa(location.val()) + "&configName=" + btoa(configName.val()) + "&config=" + (btoa(config));

                    $.ajax({
                        url: saveServiceURL,
                        type: "POST",
                        data: payload,
                        contentType: "text/plain; charset=utf-8",
                        async: false,
                        success: function (data, textStatus, xhr) {
                            if (xhr.status == 200) {
                                activeTab.setTitle(configName.val());
                                activeTab.getFile().setPath(location.val()).setName(configName.val());
                                app.breadcrumbController.setPath(location.val(), configName.val());
                                alertSuccess();
                            } else {
                                alertError();
                            }
                        },
                        error: function(res, errorCode, error){
                            alertError();
                        }
                    });
                };
            },
        });

    return SaveToFileDialog;
});