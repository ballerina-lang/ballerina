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

import ballerina/reflect;

# Representation of the Authentication filter.
#
# + authnHandlers - Array of authentication handlers
public type AuthnFilter object {

    public AuthnHandler?[]|AuthnHandler?[][] authnHandlers;

    public function __init(AuthnHandler?[]|AuthnHandler?[][] authnHandlers) {
        self.authnHandlers = authnHandlers;
    }

    # Request filter method which attempts to authenticated the request.
    #
    # + caller - Caller for outbound HTTP responses
    # + request - An inboud HTTP request message
    # + context - A filter context
    # + return - True if the filter succeeds
    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean {
        boolean|error authenticated;
        var authnHandlers = getAuthnHandlers(context);
        if (authnHandlers is AuthnHandler?[]|AuthnHandler?[][]) {
            authenticated = handleAuthnRequest(authnHandlers, request);
        } else {
            if (authnHandlers) {
                authenticated = handleAuthnRequest(self.authnHandlers, request);
            } else {
                authenticated = true;
            }
        }
        return isAuthnSuccessful(caller, authenticated);
    }

    public function filterResponse(Response response, FilterContext context) returns boolean {
        return true;
    }
};

function handleAuthnRequest(AuthnHandler?[]|AuthnHandler?[][] authnHandlers, Request request) returns boolean|error {
    if (authnHandlers is AuthnHandler?[]) {
        return checkForAuthnHandlers(authnHandlers, request);
    } else {
        foreach AuthnHandler?[] authnHandler in authnHandlers {
            var response = checkForAuthnHandlers(authnHandler, request);
            if (response is boolean) {
                if (!response) {
                    return response;
                }
            } else {
                return response;
            }
        }
        return true;
    }
}

function checkForAuthnHandlers(AuthnHandler?[] authnHandlers, Request request) returns boolean|error {
    error? err = ();
    foreach AuthnHandler? authnHandler in authnHandlers {
        if (authnHandler is AuthnHandler) {
            boolean canHandleResponse = authnHandler.canHandle(request);
            if (canHandleResponse) {
                var handleResponse = authnHandler.handle(request);
                if (handleResponse is boolean) {
                    if (handleResponse) {
                        // If one of the authenticators from the chain could successfully authenticate the user,
                        // it is not required to look through other providers. The authenticator chain is using "OR"
                        // combination of provider results.
                        return true;
                    }
                } else {
                    err = handleResponse;
                }
            }
        }
    }
    if (err is error) {
        return err;
    }
    return false;
}

# Verifies if the authentication is successful. If not responds to the user.
#
# + caller - Caller for outbound HTTP responses
# + authenticated - Authentication status for the request, or `error` if error occurred
# + return - Authentication result to indicate if the filter can proceed(true) or not(false)
function isAuthnSuccessful(Caller caller, boolean|error authenticated) returns boolean {
    Response response = new;
    response.statusCode = 401;
    if (authenticated is boolean) {
        if (!authenticated) {
            response.setTextPayload("Authentication failure");
            var err = caller->respond(response);
            if (err is error) {
                panic err;
            }
            return false;
        }
    } else {
        response.setTextPayload("Authentication failure. " + authenticated.reason());
        var err = caller->respond(response);
        if (err is error) {
            panic err;
        }
        return false;
    }
    return true;
}
