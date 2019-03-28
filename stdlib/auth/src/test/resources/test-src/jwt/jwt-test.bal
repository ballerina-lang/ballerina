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

import ballerina/auth;
import ballerina/crypto;
import ballerina/time;

function testIssueJwt(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testIssueJwtWithSingleAud(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testIssueJwtWithSingleAudAndAudAsArray(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina",
        audienceAsArray: true
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testValidateJwt(string jwtToken, string trustStorePath) returns boolean|error {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTValidatorConfig config = {
        issuer: "wso2",
        certificateAlias: "ballerina",
        audience: ["ballerinaSamples"],
        clockSkew: 60,
        trustStore: trustStore
    };

    var result = auth:validateJwt(jwtToken, config);
    if (result is auth:JwtPayload) {
        return true;
    } else {
        return result;
    }
}

function testIssueJwtWithNoIssOrSub(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testIssueJwtWithNoAudOrSub(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testValidateJwtWithNoIssOrSub(string jwtToken, string trustStorePath) returns boolean|error {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTValidatorConfig config = {
        certificateAlias: "ballerina",
        audience: ["ballerinaSamples"],
        clockSkew: 60,
        trustStore: trustStore
    };

    var result = auth:validateJwt(jwtToken, config);
    if (result is auth:JwtPayload) {
        return true;
    } else {
        return result;
    }
}
