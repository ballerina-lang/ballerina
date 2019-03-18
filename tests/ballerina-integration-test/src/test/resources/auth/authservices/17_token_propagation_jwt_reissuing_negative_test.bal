// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

http:AuthProvider basicAuthProvider17 = {
    scheme: http:JWT_AUTH,
    config: {
        issuer: "example1",
        audience: ["ballerina"],
        certificateAlias: "ballerina",
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

listener http:Listener listener17_1 = new(9107, config = {
    authProviders: [basicAuthProvider17],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

http:Client nyseEP17 = new("https://localhost:9108", config = {
    auth: {
        scheme: http:JWT_AUTH,
        config: {
            inferredJwtIssuerConfig: {
                issuer: "ballerina",
                audience: ["ballerina"],
                keyAlias: "ballerina",
                keyPassword: "ballerina",
                keyStore: {
                    path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                    password: "ballerina"
                }
            }
        }
    }
});

@http:ServiceConfig { basePath: "/passthrough" }
service passthroughService17 on listener17_1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function passthrough(http:Caller caller, http:Request clientRequest) {
        var response = nyseEP17->get("/nyseStock/stocks", message = untaint clientRequest);
        if (response is http:Response) {
            _ = caller->respond(response);
        } else {
            http:Response resp = new;
            json errMsg = { "error": "error occurred while invoking the service: " + response.reason() };
            resp.statusCode = 500;
            resp.setPayload(errMsg);
            _ = caller->respond(resp);
        }
    }
}

http:AuthProvider jwtAuthProvider17 = {
    scheme: http:JWT_AUTH,
    config: {
        issuer: "example2aaaaaaaaaaaaaa",
        audience: ["ballerina"],
        certificateAlias: "ballerina",
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

listener http:Listener listener17_2 = new(9108, config = {
        authProviders: [jwtAuthProvider17],
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            }
        }
    });

@http:ServiceConfig { basePath: "/nyseStock" }
service nyseStockQuote17 on listener17_2 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/stocks"
    }
    resource function stocks(http:Caller caller, http:Request clientRequest) {
        json payload = { "exchange": "nyse", "name": "IBM", "value": "127.50" };
        _ = caller->respond(payload);
    }
}
