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

import ballerina/crypto;
import ballerina/io;

////////////////////////////////
///// HTTP Client Endpoint /////
////////////////////////////////

# The HTTP client provides the capability for initiating contact with a remote HTTP service. The API it
# provides includes functions for the standard HTTP methods, forwarding a received request and sending requests
# using custom HTTP verbs.

# + config - The configurations associated with the client
# + httpClient - Chain of different HTTP clients which provides the capability for initiating contact with a remote
#                HTTP service in resilient manner
public type Client client object {

    public ClientEndpointConfig config = {};
    public Client httpClient;

    # Gets invoked to initialize the client. During initialization, configurations provided through the `config`
    # record is used to determine which type of additional behaviours are added to the endpoint (e.g: caching,
    # security, circuit breaking).
    #
    # + url - URL of the target service
    # + config - The configurations to be used when initializing the client
    public function __init(string url, ClientEndpointConfig? config = ()) {
        self.config = config ?: {};
        var result = initialize(url, self.config);
        if (result is error) {
            panic result;
        } else {
            self.httpClient = result;
        }
    }

    # The `post()` function can be used to send HTTP POST requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function post(@sensitive string path, RequestMessage message) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->post(path, req);
    }

    # The `head()` function can be used to send HTTP HEAD requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function head(@sensitive string path, RequestMessage message = ()) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->head(path, message = req);
    }

    # The `put()` function can be used to send HTTP PUT requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function put(@sensitive string path, RequestMessage message) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->put(path, req);
    }

    # Invokes an HTTP call with the specified HTTP verb.
    #
    # + httpVerb - HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function execute(@sensitive string httpVerb, @sensitive string path, RequestMessage message) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->execute(httpVerb, path, req);
    }

    # The `patch()` function can be used to send HTTP PATCH requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function patch(@sensitive string path, RequestMessage message) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->patch(path, req);
    }

    # The `delete()` function can be used to send HTTP DELETE requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function delete(@sensitive string path, RequestMessage message) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->delete(path, req);
    }

    # The `get()` function can be used to send HTTP GET requests to HTTP endpoints.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function get(@sensitive string path, RequestMessage message = ()) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->get(path, message = req);
    }

    # The `options()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function options(@sensitive string path, RequestMessage message = ()) returns Response|error {
        Request req = buildRequest(message);
        return self.httpClient->options(path, message = req);
    }

    # The `forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb
    #
    # + path - Request path
    # + request - An HTTP inbound request message
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function forward(@sensitive string path, Request request) returns Response|error {
        return self.httpClient->forward(path, request);
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `submit()` function does not give out a `Response` as the result,
    # rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    public remote function submit(@sensitive string httpVerb, string path, RequestMessage message) returns HttpFuture|error {
        Request req = buildRequest(message);
        return self.httpClient->submit(httpVerb, path, req);

    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|error {
        return self.httpClient->getResponse(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean {
        return self.httpClient->hasPromise(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
        return self.httpClient->getNextPromise(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|error {
        return self.httpClient->getPromisedResponse(promise);
    }

    # This just pass the request to actual network call.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        return self.httpClient->rejectPromise(promise);
    }
};

# Represents a single service and its related configurations.
#
# + url - URL of the target service
# + secureSocket - Configurations for secure communication with the remote HTTP endpoint
public type TargetService record {|
    string url = "";
    SecureSocket? secureSocket = ();
|};

# Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
#
# + httpVersion - The HTTP version understood by the client
# + http1Settings - Configurations related to HTTP/1.x protocol
# + http2Settings - Configurations related to HTTP/2 protocol
# + timeoutMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + forwarded - The choice of setting `forwarded`/`x-forwarded` header
# + followRedirects - Configurations associated with Redirection
# + poolConfig - Configurations associated with request pooling
# + proxy - Proxy server related options
# + secureSocket - SSL/TLS related options
# + cache - HTTP caching related configurations
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + auth - HTTP authentication related configurations
# + circuitBreaker - Configurations associated with Circuit Breaker behaviour
# + retryConfig - Configurations associated with Retry
public type ClientEndpointConfig record {|
    string httpVersion = HTTP_1_1;
    Http1Settings http1Settings = {};
    Http2Settings http2Settings = {};
    int timeoutMillis = 60000;
    string forwarded = "disable";
    FollowRedirects? followRedirects = ();
    ProxyConfig? proxy = ();
    PoolConfiguration? poolConfig = ();
    SecureSocket? secureSocket = ();
    CacheConfig cache = {};
    Compression compression = COMPRESSION_AUTO;
    OutboundAuthConfig? auth = ();
    CircuitBreakerConfig? circuitBreaker = ();
    RetryConfig? retryConfig = ();
|};

# Provides settings related to HTTP/1.x protocol.
#
# + keepAlive - Specifies whether to reuse a connection for multiple requests
# + chunking - The chunking behaviour of the request
public type Http1Settings record {|
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    Chunking chunking = CHUNKING_AUTO;
|};

function createSimpleHttpClient(string uri, ClientEndpointConfig config, PoolConfiguration globalPoolConfig)
                    returns Client = external;

# Provides settings related to HTTP/2 protocol.
#
# + http2PriorKnowledge - Configuration to enable HTTP/2 prior knowledge
public type Http2Settings record {|
    boolean http2PriorKnowledge = false;
|};

# Provides configurations for controlling the retrying behavior in failure scenarios.
#
# + count - Number of retry attempts before giving up
# + interval - Retry interval in milliseconds
# + backOffFactor - Multiplier of the retry interval to exponentailly increase retry interval
# + maxWaitInterval - Maximum time of the retry interval in milliseconds
# + statusCodes - HTTP response status codes which are considered as failures
public type RetryConfig record {|
    int count = 0;
    int interval = 0;
    float backOffFactor = 0.0;
    int maxWaitInterval = 0;
    int[] statusCodes = [];
|};

# Provides configurations for facilitating secure communication with a remote HTTP endpoint.
#
# + trustStore - Configurations associated with TrustStore
# + keyStore - Configurations associated with KeyStore
# + certFile - A file containing the certificate of the client
# + keyFile - A file containing the private key of the client
# + keyPassword - Password of the private key if it is encrypted
# + trustedCertFile - A file containing a list of certificates or a single certificate that the client trusts
# + protocol - SSL/TLS protocol related options
# + certValidation - Certificate validation against CRL or OCSP related options
# + ciphers - List of ciphers to be used
#             eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
# + verifyHostname - Enable/disable host name verification
# + shareSession - Enable/disable new SSL session creation
# + ocspStapling - Enable/disable OCSP stapling
# + handshakeTimeout - SSL handshake time out
# + sessionTimeout - SSL session time out
public type SecureSocket record {|
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
    string certFile = "";
    string keyFile = "";
    string keyPassword = "";
    string trustedCertFile = "";
    Protocols? protocol = ();
    ValidateCert? certValidation = ();
    string[] ciphers = [];
    boolean verifyHostname = true;
    boolean shareSession = true;
    boolean ocspStapling = false;
    int handshakeTimeout?;
    int sessionTimeout?;
|};

# Provides configurations for controlling the endpoint's behaviour in response to HTTP redirect related responses.
#
# + enabled - Enable/disable redirection
# + maxCount - Maximum number of redirects to follow
public type FollowRedirects record {|
    boolean enabled = false;
    int maxCount = 5;
|};

# Proxy server configurations to be used with the HTTP client endpoint.
#
# + host - Host name of the proxy server
# + port - Proxy server port
# + userName - Proxy server username
# + password - proxy server password
public type ProxyConfig record {|
    string host = "";
    int port = 0;
    string userName = "";
    string password = "";
|};

# The `OutboundAuthConfig` record can be used to configure the authentication mechanism used by the HTTP endpoint.
#
# + scheme - Authentication scheme
# + config - Configuration related to the selected authenticator.
public type OutboundAuthConfig record {|
    OutboundAuthScheme scheme;
    BasicAuthConfig|OAuth2AuthConfig|JwtAuthConfig config?;
|};

# The `BasicAuthConfig` record can be used to configure Basic Authentication used by the HTTP endpoint.
#
# + username - Username for Basic authentication
# + password - Password for Basic authentication
public type BasicAuthConfig record {|
    string username;
    string password;
|};

# The `OAuth2AuthConfig` record can be used to configure OAuth2 based authentication used by the HTTP endpoint.
#
# + grantType - OAuth2 grant type
# + config - Configurations for the given grant type
public type OAuth2AuthConfig record {|
    OAuth2GrantType grantType;
    ClientCredentialsGrantConfig|PasswordGrantConfig|DirectTokenConfig config;
|};

# The `ClientCredentialsGrantConfig` record can be used to configue OAuth2 client credentials grant type.
#
# + tokenUrl - Token URL for the authorization server
# + clientId - Client ID for the client credentials grant authentication
# + clientSecret - Client secret for the client credentials grant authentication
# + scopes - Scope of the access request
# + clockSkew - Clock skew in seconds
# + retryRequest - Retry the request if the initial request returns a 401 response
# + credentialBearer - How authentication credentials are sent to the authorization server
public type ClientCredentialsGrantConfig record {|
    string tokenUrl;
    string clientId;
    string clientSecret;
    string[] scopes?;
    int clockSkew = 0;
    boolean retryRequest = true;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
|};

# The `PasswordGrantConfig` record can be used to configue OAuth2 password grant type
#
# + tokenUrl - Token URL for the authorization server
# + username - Username for password grant authentication
# + password - Password for password grant authentication
# + clientId - Client ID for password grant authentication
# + clientSecret - Client secret for password grant authentication
# + scopes - Scope of the access request
# + refreshConfig - Configurations for refreshing the access token
# + clockSkew - Clock skew in seconds
# + retryRequest - Retry the request if the initial request returns a 401 response
# + credentialBearer - How authentication credentials are sent to the authorization server
public type PasswordGrantConfig record {|
    string tokenUrl;
    string username;
    string password;
    string clientId?;
    string clientSecret?;
    string[] scopes?;
    RefreshConfig refreshConfig?;
    int clockSkew = 0;
    boolean retryRequest = true;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
|};

# The `DirectTokenConfig` record configures the access token directly.
#
# + accessToken - Access token for the authorization server
# + refreshConfig - Configurations for refreshing the access token
# + clockSkew - Clock skew in seconds
# + retryRequest - Retry the request if the initial request returns a 401 response
# + credentialBearer - How authentication credentials are sent to the authorization server
public type DirectTokenConfig record {|
    string accessToken?;
    DirectTokenRefreshConfig refreshConfig?;
    int clockSkew = 0;
    boolean retryRequest = true;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
|};

# The `RefreshConfig` record can be used to pass the configurations for refreshing the access token of password grant type.
#
# + refreshUrl - Refresh token URL for the refresh token server
# + scopes - Scope of the access request
# + credentialBearer - How authentication credentials are sent to the authorization server
public type RefreshConfig record {|
    string refreshUrl;
    string[] scopes?;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
|};

# The `DirectTokenRefreshConfig` record passes the configurations for refreshing the access token for 
# the grant type of the direct token grant type.
#
# + refreshUrl - Refresh token URL for the refresh token server
# + refreshToken - Refresh token for the refresh token server
# + clientId - Client ID for authentication with the authorization server
# + clientSecret - Client secret for authentication with the authorization server
# + scopes - Scope of the access request
# + credentialBearer - How authentication credentials are sent to the authorization server
public type DirectTokenRefreshConfig record {|
    string refreshUrl;
    string refreshToken;
    string clientId;
    string clientSecret;
    string[] scopes?;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
|};

// TODO: Resolve with https://github.com/ballerina-platform/ballerina-lang/issues/15487
# The `JwtAuthConfig` record can be used to configure JWT based authentication used by the HTTP endpoint.
#
//# + inferredJwtIssuerConfig - JWT issuer configuration used to issue JWT with specific configuration
public type JwtAuthConfig record {|
    //jwt:InferredJwtIssuerConfig inferredJwtIssuerConfig;
|};

function initialize(string serviceUrl, ClientEndpointConfig config) returns Client|error {
    boolean httpClientRequired = false;
    string url = serviceUrl;
    if (url.hasSuffix("/")) {
        int lastIndex = url.length() - 1;
        url = url.substring(0, lastIndex);
    }
    var cbConfig = config.circuitBreaker;
    if (cbConfig is CircuitBreakerConfig) {
        if (url.hasSuffix("/")) {
            int lastIndex = url.length() - 1;
            url = url.substring(0, lastIndex);
        }
    } else {
        httpClientRequired = true;
    }
    if (httpClientRequired) {
        var redirectConfigVal = config.followRedirects;
        if (redirectConfigVal is FollowRedirects) {
            return createRedirectClient(url, config);
        } else {
            return checkForRetry(url, config);
        }
    } else {
        return createCircuitBreakerClient(url, config);
    }
}

function createRedirectClient(string url, ClientEndpointConfig configuration) returns Client|error {
    var redirectConfig = configuration.followRedirects;
    if (redirectConfig is FollowRedirects) {
        if (redirectConfig.enabled) {
            var retryClient = createRetryClient(url, configuration);
            if (retryClient is Client) {
                return new RedirectClient(url, configuration, redirectConfig, retryClient);
            } else {
                return retryClient;
            }
        } else {
            return createRetryClient(url, configuration);
        }
    } else {
        return createRetryClient(url, configuration);
    }
}

function checkForRetry(string url, ClientEndpointConfig config) returns Client|error {
    var retryConfigVal = config.retryConfig;
    if (retryConfigVal is RetryConfig) {
        return createRetryClient(url, config);
    } else {
        if (config.cache.enabled) {
            return createHttpCachingClient(url, config, config.cache);
        } else {
            return createHttpSecureClient(url, config);
        }
    }
}

function createCircuitBreakerClient(string uri, ClientEndpointConfig configuration) returns Client|error {
    Client cbHttpClient;
    var cbConfig = configuration.circuitBreaker;
    if (cbConfig is CircuitBreakerConfig) {
        validateCircuitBreakerConfiguration(cbConfig);
        boolean[] statusCodes = populateErrorCodeIndex(cbConfig.statusCodes);
        var redirectConfig = configuration.followRedirects;
        if (redirectConfig is FollowRedirects) {
            var redirectClient = createRedirectClient(uri, configuration);
            if (redirectClient is Client) {
                cbHttpClient = redirectClient;
            } else {
                return redirectClient;
            }
        } else {
            var retryClient = checkForRetry(uri, configuration);
            if (retryClient is Client) {
                cbHttpClient = retryClient;
            } else {
                return retryClient;
            }
        }

        time:Time circuitStartTime = time:currentTime();
        int numberOfBuckets = (cbConfig.rollingWindow.timeWindowMillis / cbConfig.rollingWindow.bucketSizeMillis);
        Bucket?[] bucketArray = [];
        int bucketIndex = 0;
        while (bucketIndex < numberOfBuckets) {
            bucketArray[bucketIndex] = {};
            bucketIndex = bucketIndex + 1;
        }

        CircuitBreakerInferredConfig circuitBreakerInferredConfig = {
            failureThreshold: cbConfig.failureThreshold,
            resetTimeMillis: cbConfig.resetTimeMillis,
            statusCodes: statusCodes,
            noOfBuckets: numberOfBuckets,
            rollingWindow: cbConfig.rollingWindow
        };
        CircuitHealth circuitHealth = {
            startTime: circuitStartTime,
            lastRequestTime: circuitStartTime,
            lastErrorTime: circuitStartTime,
            lastForcedOpenTime: circuitStartTime,
            totalBuckets: bucketArray
        };
        return new CircuitBreakerClient(uri, configuration, circuitBreakerInferredConfig, cbHttpClient, circuitHealth);
    } else {
        //remove following once we can ignore
        if (configuration.cache.enabled) {
            return createHttpCachingClient(uri, configuration, configuration.cache);
        } else {
            return createHttpSecureClient(uri, configuration);
        }
    }
}

function createRetryClient(string url, ClientEndpointConfig configuration) returns Client|error {
    var retryConfig = configuration.retryConfig;
    if (retryConfig is RetryConfig) {
        boolean[] statusCodes = populateErrorCodeIndex(retryConfig.statusCodes);
        RetryInferredConfig retryInferredConfig = {
            count: retryConfig.count,
            interval: retryConfig.interval,
            backOffFactor: retryConfig.backOffFactor,
            maxWaitInterval: retryConfig.maxWaitInterval,
            statusCodes: statusCodes
        };
        if (configuration.cache.enabled) {
            var httpCachingClient = createHttpCachingClient(url, configuration, configuration.cache);
            if (httpCachingClient is Client) {
                return new RetryClient(url, configuration, retryInferredConfig, httpCachingClient);
            } else {
                return httpCachingClient;
            }
        } else {
            var httpSecureClient = createHttpSecureClient(url, configuration);
            if (httpSecureClient is Client) {
                return new RetryClient(url, configuration, retryInferredConfig, httpSecureClient);
            } else {
                return httpSecureClient;
            }
        }
    } else {
        //remove following once we can ignore
        if (configuration.cache.enabled) {
            return createHttpCachingClient(url, configuration, configuration.cache);
        } else {
            return createHttpSecureClient(url, configuration);
        }
    }
}

function createClient(string url, ClientEndpointConfig config) returns Client|error {
    HttpClient simpleClient = new(url, config);
    return simpleClient;
}
