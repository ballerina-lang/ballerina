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

# A webSocket client endpoint which provides failover support over multiple webSocket targets.
#
# + id - The connection id
# + negotiatedSubProtocol - The subprotocols that are negotiated with the server
# + secure - `true` if the connection is secure
# + open - `true` if the connection is open
# + attributes - A map to store connection related attributes
public type WebSocketFailoverClient client object {

    private string id = "";
    private string? negotiatedSubProtocol = ();
    private boolean secure = false;
    private boolean open = false;
    private Response? response = ();
    private map<any> attributes = {};

    private WebSocketConnector conn = new;
    private WebSocketFailoverClientEndpointConfiguration config = {};

    # Failover caller action that provide failover capabilities to a webSocket client endpoint.
    #
    # + config - The configurations of the client endpoint associated with this `failover` instance
    public function __init(public WebSocketFailoverClientEndpointConfiguration? config = ()) {
        self.config = config ?: {};
        self.init();
    }

    # Initializes the endpoint.
    function init() = external;

    # Push text to the connection.
    #
    # + data - Data to be sent, if byte[] it is converted to a UTF-8 string for sending
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return  - `WebSocketError` if an error occurs when sending the text message to the server.
    #              Message will be lost, if an error occurs.
    public remote function pushText(string|json|xml|boolean|int|float|byte|byte[] data,
        public boolean finalFrame = true) returns WebSocketError? {
        return self.conn.pushText(data, finalFrame);
    }

    # Push binary data to the connection.
    #
    # + data - Binary data to be sent
    # + finalFrame - Set to `true` if this is a final frame of a (long) message
    # + return - `WebSocketError` if an error occurs when sending the binary message to the server.
    #             Message will be lost, if an error occurs.
    public remote function pushBinary(byte[] data, public boolean finalFrame = true) returns error? {
        return self.conn.pushBinary(data, finalFrame);
    }

    # Ping the connection.
    #
    # + data - Binary data to be sent.
    # + return - `WebSocketError` if an error occurs when sending the ping frame to the server.
    #             Frame will be lost, if an error occurs.
    public remote function ping(byte[] data) returns WebSocketError? {
        return self.conn.ping(data);
    }

    # Send pong message to the connection.
    #
    # + data - Binary data to be sent
    # + return - `WebSocketError` if an error occurs when sending the pong frame to the server.
    #             Frame will be lost, if an error occurs.
    public remote function pong(byte[] data) returns WebSocketError? {
        return self.conn.pong(data);
    }

    # Close the connection.
    #
    # + statusCode - Status code for closing the connection. Default value: 1000
    # + reason - Reason for closing the connection
    # + timeoutInSeconds - Time to wait for the close frame to be received from the remote endpoint before closing the
    #                   connection. If the timeout exceeds, then the connection is terminated even though a close frame
    #                   is not received from the remote endpoint. If the value < 0 (e.g., -1), then the connection waits
    #                   until a close frame is received. If WebSocket frame is received from the remote endpoint,
    #                   within waiting period the connection is terminated immediately. Default value: 60
    # + return - `WebSocketError` if an error occurs when closing the webSocket connection.
    public remote function close(public int? statusCode = 1000, public string? reason = (),
        public int timeoutInSeconds = 60) returns WebSocketError? {
        return self.conn.close(statusCode, reason, timeoutInSeconds);
    }

    # Called when the endpoint is ready to receive messages. Can be called only once per endpoint. For the
    # WebSocketListener can be called only in upgrade or onOpen resources.
    #
    # + return - `WebSocketError` if an error occurs when checking the connection state.
    public remote function ready() returns WebSocketError? {
        return self.conn.ready();
    }

    # Sets a connection related attribute.
    #
    # + key - key that identifies the attribute
    # + value - value of the attribute
    public function setAttribute(string key, any value) {
        self.attributes[key] = value;
    }

    # Gets connection related attribute if any.
    #
    # + key - the key to identify the attribute.
    # + return - the attribute related to the given key or `nil`
    public function getAttribute(string key) returns any {
        return self.attributes[key];
    }

    # Removes connection related attribute if any.
    #
    # + key - the key to identify the attribute.
    # + return - the attribute related to the given key or `nil`
    public function removeAttribute(string key) returns any {
        return self.attributes.remove(key);
    }

    # Gives the connection id associated with this connection.
    #
    # + return - the unique id associated with the connection
    public function getConnectionId() returns string {
        return self.id;
    }

    # Gives the subprotocol if any that is negotiated with the client.
    #
    # + return - The subprotocol if any negotiated with the client or `nil`
    public function getNegotiatedSubProtocol() returns string? {
        return self.negotiatedSubProtocol;
    }

    # Gives the secured status of the connection.
    #
    # + return - `true` if the connection is secure.
    public function isSecure() returns boolean {
        return self.secure;
    }

    # Gives the open or closed status of the connection.
    #
    # + return - `true` if the connection is open
    public function isOpen() returns boolean {
        return self.open;
    }

    # Gives the HTTP response if any received for the client handshake request.
    #
    # + return - the HTTP response received for the client handshake request
    public function getHttpResponse() returns Response? {
        return self.response;
    }
};

# Configuration for the WebSocket client endpoint.
#
# + callbackService - The callback service for the client. Resources in this service gets called on receipt of messages
#                     from the server.
# + subProtocols - Negotiable sub protocols for the client
# + customHeaders - Custom headers which should be sent to the server
# + idleTimeoutInSeconds - Idle timeout of the client. Upon timeout, `onIdleTimeout` resource (if defined) in the client
#                          service will be triggered. Default value: -1
# + readyOnConnect - `true` if the client is ready to receive messages as soon as the connection is established.
#                    This is true by default. If changed to false the function ready() of the
#                    `WebSocketClient` needs to be called once to start receiving messages.
# + secureSocket - SSL/TLS related options
# + maxFrameSize - The maximum payload size of a WebSocket frame in bytes.
#                  If this is not set or is negative or zero the default frame size of 65536 will be used.
#                  Default value: 0
# + targetUrls - The set of urls which are used to connect the server.
# + failoverInterval - The maximum number of milliseconds to delay a failover attempt. Default value: 1000
# + webSocketCompressionEnabled - Enable support for compression in WebSocket
# + retryConfig - Configurations related to retry
public type WebSocketFailoverClientEndpointConfiguration record {|
    service? callbackService = ();
    string[]? subProtocols = [];
    map<string> customHeaders?;
    int idleTimeoutInSeconds = -1;
    boolean readyOnConnect = true;
    ClientSecureSocket? secureSocket = ();
    int maxFrameSize = 0;
    string[] targetUrls = [];
    int failoverInterval = 1000;
    boolean webSocketCompressionEnabled = true;
    WebSocketRetryConfig retryConfig?;
|};
