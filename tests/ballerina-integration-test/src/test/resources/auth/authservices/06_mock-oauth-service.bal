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
import ballerina/io;

listener http:Listener tokenlistener = new(9095, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

service foo on tokenlistener {

    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function bar(http:Caller caller, http:Request req) {
        _ = caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function token(http:Caller caller, http:Request req) {
        // Mock token refresh resource
        var payload = req.getTextPayload();
        if (payload is string) {
            boolean clientIdInBody = payload.contains("client_id");
            string authHeader;
            var reqHeader = trap req.getHeader("Authorization");
            if (reqHeader is error) {
                authHeader = "";
            } else {
                authHeader = req.getHeader("Authorization");
            }
            boolean tokenScope = false;
            if (payload.contains("&scope=token-scope1 token-scope2")) {
                tokenScope = true;
            }
            json status = { clientIdInBody: clientIdInBody, hasAuthHeader: authHeader != "", tokenScope: tokenScope };
            io:println(status);
            json resp = { access_token: "acces-token" };
            _ = caller->respond(resp);
        } else {
            panic payload;
        }
    }
}
