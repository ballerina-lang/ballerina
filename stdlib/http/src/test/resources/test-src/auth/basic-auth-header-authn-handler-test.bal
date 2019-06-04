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

import ballerina/auth;
import ballerina/http;

function testCanHandleHttpBasicAuthWithoutHeader() returns boolean {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthHeaderAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "123Basic xxxxxx";
    inRequest.setHeader("123Authorization", basicAuthHeaderValue);
    return handler.canHandle(inRequest);
}

function testCanHandleHttpBasicAuth() returns boolean {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthHeaderAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic xxxxxx";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return handler.canHandle(inRequest);
}

function testHandleHttpBasicAuthFailure() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthHeaderAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic YW1pbGE6cHFy";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return handler.handle(inRequest);
}

function testHandleHttpBasicAuth() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthHeaderAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic aXN1cnU6eHh4";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return handler.handle(inRequest);
}

function testExtractBasicAuthHeaderValue() returns string|error {
    // create dummy request
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic aXN1cnU6eHh4";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return http:extractAuthorizationHeaderValue(inRequest);
}

function createRequest() returns http:Request {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}
