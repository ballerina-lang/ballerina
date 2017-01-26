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

/**
 * A module representing the factory for Ballerina Env
 */
define(['./package', './../ast/ballerina-ast-factory'],
    function (Package, BallerinaASTFactory) {

        /**
         * @class BallerinaEnvFactory
         */
        var BallerinaEnvFactory = {};

        /**
         * creates Package from json
         * @param args
         */
        BallerinaEnvFactory.createPackage = function (jsonNode) {
            var package = new Package();
            package.initFromJson(jsonNode);
            return package;
        };

        /**
         * creates ConnectorDefinition from json
         * @param args
         */
        BallerinaEnvFactory.createConnectorDefinition = function (jsonNode) {
            var connector = BallerinaASTFactory.createConnectorDefinition();
            connector.initFromJson(jsonNode);
            return connector;
        };

        return BallerinaEnvFactory;

    });
