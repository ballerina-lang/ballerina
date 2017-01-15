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
define(['lodash', './node'], function(_, ASTNode){

    /**
     * Constructor for ThenBody
     * @param {Object} args - Arguments to create the ThenBody
     * @constructor
     * @augments ASTNode
     */
    var ThenBody = function (args) {
        ASTNode.call(this, 'ThenBody');
    }

    ThenBody.prototype = Object.create(ASTNode.prototype);
    ThenBody.prototype.constructor = ThenBody;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     * @param {Array} [jsonNode.children] - array which contains the statements of 'then block'
     */
    ThenBody.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return ThenBody;
});
