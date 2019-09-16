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
import ballerina/log;

@http:WebSocketServiceConfig {
}
service on new http:Listener(30004) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketFailoverClient wsClientEp = new({ callbackService:
            failoverClientWithRetryCallbackService, readyOnConnect: false, targetUrls:
            ["ws://localhost:8080/websocket", "ws://localhost:15100/websocket",
            "ws://localhost:15200/websocket"], retryConfig: {maxCount: 5}});
        wsEp.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, wsEp);
        var returnVal = wsClientEp->ready();
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketFailoverClient clientEp = getAssociatedFailoverClientEndpoint(wsEp);
        var returnVal = clientEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketFailoverClient clientEp = getAssociatedFailoverClientEndpoint(wsEp);
        var returnVal = clientEp->pushBinary(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onClose(http:WebSocketCaller wsEp, int statusCode, string reason) {
        http:WebSocketFailoverClient clientEp = getAssociatedFailoverClientEndpoint(wsEp);
        var returnVal = clientEp->close(statusCode, reason);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    //This resource gets invoked when an error occurs in the connection.
    resource function onError(http:WebSocketCaller caller, error err) {
        http:WebSocketFailoverClient clientEp =
                        getAssociatedFailoverClientEndpoint(caller);
        var e = clientEp->close(1011, <string>err.detail()["message"]);
        if (e is http:WebSocketError) {
            log:printError("Error occurred when closing the connection",
                            <error> e);
        }
        _ = caller.removeAttribute(ASSOCIATED_CONNECTION);
        log:printError("Unexpected error hence closing the connection",
                        <error> err);
    }
}

service failoverClientWithRetryCallbackService = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketFailoverClient wsEp, string text) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        var returnVal = serviceEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onBinary(http:WebSocketFailoverClient wsEp, byte[] data) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        var returnVal = serviceEp->pushBinary(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onClose(http:WebSocketFailoverClient wsEp, int statusCode, string reason) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        var returnVal = serviceEp->close(statusCode = statusCode, reason = reason);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    //This resource gets invoked when an error occurs in the connection.
    resource function onError(http:WebSocketFailoverClient caller, error err) {
        http:WebSocketCaller serverEp = getAssociatedFailoverListener(caller);
        var e = serverEp->close(1011, <string>err.detail()["message"]);
        if (e is http:WebSocketError) {
            log:printError("Error occurred when closing the connection",
                            err = e);
        }
        _ = caller.removeAttribute(ASSOCIATED_CONNECTION);
        log:printError("Unexpected error hense closing the connection",
                        <error> err);
    }
};
