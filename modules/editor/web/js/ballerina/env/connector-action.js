/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['log', 'lodash'],
    function (log, _) {

        /**
         * @class Package
         * @augments EventChannel
         * @param args {Object} - args.name: name of the package
         * @constructor
         */
        var ConnectorAction = function (args) {
            this.name = _.get(args, 'name', '');
            this.action = _.get(args, 'action', '');
            this._id = _.get(args, 'id', '');
            this._title = _.get(args, 'title', '');
        };

        ConnectorAction.prototype.setName = function (name) {
            this.name = name;
        };

        ConnectorAction.prototype.getName = function () {
            return this.name;
        };

        ConnectorAction.prototype.setId = function (id) {
            this._id = id;
        };

        ConnectorAction.prototype.getId = function () {
            return this._id;
        };

        ConnectorAction.prototype.setTitle = function (title) {
            this._title = title;
        };

        ConnectorAction.prototype.getTitle = function () {
            return this._title;
        };

        ConnectorAction.prototype.setAction = function (action) {
            this.action = action;
        };

        ConnectorAction.prototype.getAction = function () {
            return this.action;
        };

        ConnectorAction.prototype.initFromJson = function (jsonNode) {
            this.setName(jsonNode.name);
            this.setAction(jsonNode.meta.action);
            this.setId(jsonNode.id);
            this.setTitle(jsonNode.title);
        };

        return ConnectorAction;
    });