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


documentation {Load balancing algorithm - Round Robin}
@final public string ROUND_ROBIN = "round-robin";

documentation {
    Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient.

    F{{serviceUri}} The URL of the remote HTTP endpoint
    F{{config}} The configurations of the client endpoint associated with this `LoadBalancer` instance
    F{{loadBalanceClientsArray}} Array of HTTP clients for load balancing
    F{{algorithm}} Load balancing algorithm
    F{{nextIndex}} Index of the next load balancing client
    F{{failover}} Whether to fail over in case of a failure
}
public type LoadBalancer object {
   public {
       string serviceUri;
       ClientEndpointConfig config;
       CallerActions[] loadBalanceClientsArray;
       string algorithm;
       int nextIndex;
       boolean failover;
   }

   public new (serviceUri, config, loadBalanceClientsArray, algorithm, nextIndex, failover) {}

    documentation {
        The POST action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function post(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The HEAD action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function head(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The PATCH action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function patch(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The PUT action implementation of the Load Balance Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function put(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The OPTIONS action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function options(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The FORWARD action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function forward(string path, Request request) returns Response|HttpConnectorError;

    documentation {
        The EXECUTE action implementation of the LoadBalancer Connector.
        The Execute action can be used to invoke an HTTP call with the given HTTP verb.

        P{{httpVerb}} HTTP method to be used for the request
        P{{path}} Resource path
        P{{request}} An HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function execute(string httpVerb, string path, Request request) returns Response|HttpConnectorError;

    documentation {
        The DELETE action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function delete(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The GET action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `HttpConnectorError` if failed to fulfill the request
    }
    public function get(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The submit implementation of the LoadBalancer Connector.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The Resource path
        P{{request}} An HTTP request
        R{{}} The Future for further interactions or an `HttpConnectorError` if failed to fulfill the request
    }
    public function submit(string httpVerb, string path, Request request) returns HttpFuture|HttpConnectorError;

    documentation {
        The getResponse implementation of the LoadBalancer Connector.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} The response or an `HttpConnectorError` if failed to get the response
    }
    public function getResponse(HttpFuture httpFuture) returns Response|HttpConnectorError;

    documentation {
        The hasPromise implementation of the LoadBalancer Connector.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} Returns true if the push promise exists
    }
    public function hasPromise(HttpFuture httpFuture) returns boolean;

    documentation {
        The getNextPromise implementation of the LoadBalancer Connector.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} The HTTP Push Promise message or an `HttpConnectorError` if failed to fulfill the request
    }
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|HttpConnectorError;

    documentation {
        The getPromisedResponse implementation of the LoadBalancer Connector.

        P{{promise}} The related Push Promise message
        R{{}} The HTTP Push Response message or an `HttpConnectorError` if failed to fulfill the request
    }
    public function getPromisedResponse(PushPromise promise) returns Response|HttpConnectorError;

    documentation {
        The rejectPromise implementation of the LoadBalancer Connector.

        P{{promise}} The Push Promise to be rejected
    }
    public function rejectPromise(PushPromise promise);

};

documentation {
    Represents an error occurred in an action of the Load Balance connector.

    F{{message}} An error message explaining about the error
    F{{cause}} The error that caused HttpConnectorError to get thrown
    F{{statusCode}} HTTP status code of the LoadBalanceConnectorError
    F{{httpConnectorError}} Array of HttpConnectorError error occurred at each endpoint
}
public type LoadBalanceConnectorError {
    string message,
    error? cause,
    int statusCode,
    HttpConnectorError[] httpConnectorError,
};

public function LoadBalancer::post(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    return performLoadBalanceAction(self, path, req, HTTP_POST);
}

public function LoadBalancer::head(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    return performLoadBalanceAction(self, path, req, HTTP_HEAD);
}

public function LoadBalancer::patch(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    return performLoadBalanceAction(self, path, req, HTTP_PATCH);
}

public function LoadBalancer::put(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    return performLoadBalanceAction(self, path, req, HTTP_PUT);
}

public function LoadBalancer::options(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    return performLoadBalanceAction(self, path, req, HTTP_OPTIONS);
}

public function LoadBalancer::forward(string path, Request request) returns Response|HttpConnectorError {
    return performLoadBalanceAction(self, path, request, HTTP_FORWARD);
}

public function LoadBalancer::execute(string httpVerb, string path, Request request) returns Response|HttpConnectorError {
    return performLoadBalanceExecuteAction(self, path, request, httpVerb);
}

public function LoadBalancer::delete(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    return performLoadBalanceAction(self, path, req, HTTP_DELETE);
}

public function LoadBalancer::get(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    return performLoadBalanceAction(self, path, req, HTTP_GET);
}

public function LoadBalancer::submit(string httpVerb, string path, Request request) returns HttpFuture|HttpConnectorError {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

public function LoadBalancer::getResponse(HttpFuture httpFuture) returns Response|HttpConnectorError {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

public function LoadBalancer::hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

public function LoadBalancer::getNextPromise(HttpFuture httpFuture) returns PushPromise|HttpConnectorError {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

public function LoadBalancer::getPromisedResponse(PushPromise promise) returns Response|HttpConnectorError {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

public function LoadBalancer::rejectPromise(PushPromise promise) {
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction(LoadBalancer lb, string path, Request request,
                                          string httpVerb) returns Response|HttpConnectorError {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    if (connectorAction != HTTP_NONE) {
        return performLoadBalanceAction(lb, path, request, connectorAction);
    } else {
        HttpConnectorError httpConnectorError = {message:"Unsupported connector action received.", statusCode:501};
        return httpConnectorError;
    }
}

// Handles all the actions exposed through the Load Balance connector.
function performLoadBalanceAction(LoadBalancer lb, string path, Request request, HttpOperation requestAction)
                                    returns Response|HttpConnectorError {
    int loadBalanceTermination = 0; // Tracks at which point failover within the load balancing should be terminated.
    //TODO: workaround to initialize a type inside a function. Change this once fix is aailable.
    LoadBalanceConnectorError loadBalanceConnectorError = {statusCode:500};
    loadBalanceConnectorError.httpConnectorError = [];
    Request loadBlancerInRequest = request;
    mime:Entity requestEntity = new;

    if (lb.failover) {
        // When performing passthrough scenarios using Load Balance connector, message needs to be built before trying out the
        // load balance endpoints to keep the request message to load balance the messages in case of failure.
        var binaryPayload = loadBlancerInRequest.getBinaryPayload();
        requestEntity = check loadBlancerInRequest.getEntity();
    }

    while (loadBalanceTermination < lengthof lb.loadBalanceClientsArray) {
        CallerActions loadBalanceClient = roundRobin(lb, lb.loadBalanceClientsArray);

        match invokeEndpoint(path, request, requestAction, loadBalanceClient) {
            Response inResponse => return inResponse;

            HttpConnectorError httpConnectorError => {
                if (!lb.failover) {
                    return httpConnectorError;
                } else {
                    Request newOutRequest = new;
                    populateRequestFields(loadBlancerInRequest, newOutRequest);
                    newOutRequest.setEntity(requestEntity);
                    loadBlancerInRequest = newOutRequest;
                    loadBalanceConnectorError.httpConnectorError[lb.nextIndex] = httpConnectorError;
                    loadBalanceTermination = loadBalanceTermination + 1;
                }
            }
        }
    }
    return populateGenericLoadBalanceConnectorError(loadBalanceConnectorError);
}

// Round Robin Algorithm implementation with respect to load balancing endpoints.
public function roundRobin(LoadBalancer lb, CallerActions[] loadBalanceConfigArray) returns CallerActions {
    CallerActions httpClient = new;

    lock {
        if (lb.nextIndex == ((lengthof (loadBalanceConfigArray)) - 1)) {
            httpClient = loadBalanceConfigArray[lb.nextIndex];
            lb.nextIndex = 0;
        } else {
            httpClient = loadBalanceConfigArray[lb.nextIndex];
            lb.nextIndex = lb.nextIndex + 1;
        }
    }

    return httpClient;
}

// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
function populateGenericLoadBalanceConnectorError(LoadBalanceConnectorError loadBalanceConnectorError)
                                                    returns HttpConnectorError {
    int nErrs = lengthof loadBalanceConnectorError.httpConnectorError;
    loadBalanceConnectorError.statusCode = INTERNAL_SERVER_ERROR_500;
    loadBalanceConnectorError.message = "All the load balance endpoints failed. Last error was: "
                                        + loadBalanceConnectorError.httpConnectorError[nErrs - 1].message;
    HttpConnectorError httpConnectorError = loadBalanceConnectorError;
    return httpConnectorError;
}
