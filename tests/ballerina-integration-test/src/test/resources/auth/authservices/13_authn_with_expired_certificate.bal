// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;

http:AuthProvider jwtAuthProvider4 = {
    scheme:"jwt",
    issuer:"ballerina",
    audience: "ballerina.io",
    certificateAlias: "cert",
    trustStore: {
        path: "../../../src/test/resources/auth/testtruststore.p12",
        password: "ballerina"
    }
};

listener http:Listener listener13 = new(9101, config = {
    authProviders:[jwtAuthProvider4],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

service echo13 on listener13 {

    resource function test13 (http:Caller caller, http:Request req) {
        _ = caller -> respond(());
    }
}
