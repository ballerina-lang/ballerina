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

import ballerina/io;
import ballerina/net.http;

endpoint http:ServiceEndpoint participant1EP {
    port:8889
};

endpoint http:ClientEndpoint participant2EP {
    targets: [{uri: "http://localhost:8890"}]
};

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> participant1 bind participant1EP {

    getState(endpoint ep, http:Request req) {
        string result = io:sprintf("abortedByParticipant=%b,abortedFunctionCalled=%b,committedFunctionCalled=%s," +
                                    "localParticipantAbortedFunctionCalled=%s,localParticipantCommittedFunctionCalled=%s",
                                    [abortedByParticipant, abortedFunctionCalled, committedFunctionCalled,
                                    localParticipantAbortedFunctionCalled, localParticipantCommittedFunctionCalled]);

        http:Response res = {};
        res.setStringPayload(result);
        _ = ep -> respond(res);
    }

    testRemoteParticipantAbort(endpoint ep, http:Request req) {
        reset();
        transaction with oncommit=onCommit, onabort=onAbort {
            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }
            abortedByParticipant = true;
            abort;
        }
        http:Response res = {statusCode: 200};
        _ = ep -> respond(res);
    }

    noOp(endpoint ep, http:Request req) {
        reset();
        transaction with oncommit=onCommit, onabort=onAbort {
            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }
        }
        http:Response res = {statusCode: 200};
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: false
    }
    nonInfectable(endpoint ep, http:Request req) {
        reset();
        transaction with oncommit=onCommit, onabort=onAbort {
            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
                abort;
            }
        }
        http:Response res = {statusCode: 200};
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: true
    }
    infectable(endpoint ep, http:Request req) {
        reset();
        transaction with oncommit=onCommit, onabort=onAbort {
            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
                abort;
            }
        }
        http:Response res = {statusCode: 200};
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        path:"/"
    }
    member (endpoint conn, http:Request req) {
        reset();
        http:Request newReq = {};
        newReq.setHeader("participant-id", req.getHeader("X-XID"));
        transaction {
            var forwardResult = participant2EP -> forward("/task1", req);
            match forwardResult {
                http:HttpConnectorError err => {
                    io:print("Participant1 could not send get request to participant2/task1. Error:");
                    sendErrorResponseToInitiator(conn);
                    abort;
                }
                http:Response forwardRes => {
                    var getResult = participant2EP -> get("/task2", newReq);
                    match getResult {
                        http:HttpConnectorError err => {
                            io:print("Participant1 could not send get request to participant2/task2. Error:");
                            sendErrorResponseToInitiator(conn);
                            abort;
                        }
                        http:Response getRes => {
                            var forwardRes2 = conn -> forward(getRes);
                            match forwardRes2 {
                                http:HttpConnectorError err => {
                                    io:print("Participant1 could not forward response from participant2 to initiator. Error:");
                                    io:println(err);
                                }
                                null => io:print("");
                            }
                        }
                    }
                }
            }
        } onretry {
            io:println("Participant1 failed");
        }
    }
}

function sendErrorResponseToInitiator(http:ServiceEndpoint conn) {
    endpoint http:ServiceEndpoint conn2 = conn;
    http:Response errRes = {statusCode: 500};
    var respondResult = conn2 -> respond(errRes);
    match respondResult {
        http:HttpConnectorError respondErr => {
            io:print("Participant1 could not send error response to initiator. Error:");
            io:println(respondErr);
        }
        null => return;
    }
}

function onAbort() {
    abortedFunctionCalled = true;
}

function onCommit() {
    committedFunctionCalled = true;
}

function onLocalParticipantAbort() {
    localParticipantAbortedFunctionCalled = true;
}

function onLocalParticipantCommit() {
    localParticipantCommittedFunctionCalled = true;
}

boolean abortedByParticipant;
boolean abortedFunctionCalled;
boolean committedFunctionCalled;
boolean localParticipantAbortedFunctionCalled;
boolean localParticipantCommittedFunctionCalled;

function reset() {
    abortedByParticipant = false;
    abortedFunctionCalled = false;
    committedFunctionCalled = false;
    localParticipantAbortedFunctionCalled = false;
    localParticipantCommittedFunctionCalled = false;
}
