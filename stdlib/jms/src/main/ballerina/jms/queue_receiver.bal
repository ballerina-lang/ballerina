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

import ballerina/log;

# Queue Receiver endpoint
#
# + consumerActions - handles all the caller actions related to the QueueReceiver endpoint
# + config - configurations related to the QueueReceiver
public type QueueReceiver object {

    *AbstractListener;

    public QueueReceiverCaller consumerActions = new;
    public QueueReceiverEndpointConfiguration config = {};

    # Initializes the QueueReceiver endpoint
    #
    # + c - Configurations related to the QueueReceiver endpoint
    public function __init(QueueReceiverEndpointConfiguration c) {
        self.config = c;
        self.consumerActions.queueReceiver = self;
        var session = c.session;
        if (session is Session) {
            var queueName = c.queueName;
            if (queueName is string) {
                self.createQueueReceiver(session, c.messageSelector);
                log:printInfo("Message receiver created for queue " + queueName);
            } else {
                log:printInfo("Message receiver is not properly initialized for queue");
            }
        } else {
            log:printInfo("Message receiver is not properly initialized for queue");
        }
    }

    # Binds the queue receiver endpoint to a service
    #
    # + serviceType - Type descriptor of the service to bind to
    # + data - Service annotations
    # + return - Nil or error upon failure to register listener
    public function __attach(service serviceType, map<any> data) returns error? {
        return self.registerListener(serviceType, self.consumerActions, data);
    }

    extern function registerListener(service serviceType, QueueReceiverCaller actions, map<any> data) returns error?;

    extern function createQueueReceiver(Session session, string messageSelector, Destination? destination = ());

    # Starts the endpoint. Function is ignored by the receiver endpoint
    #
    # + return - Nil or error upon failure to start
    public function __start() returns error? {
        return ();
        // Ignore
    }

    # Retrieves the QueueReceiver consumer action handler
    #
    # + return - QueueReceiver actions handler
    public function getCallerActions() returns QueueReceiverCaller {
        return self.consumerActions;
    }

    # Stops consuming messages through QueueReceiver endpoint
    #
    # + return - Nil or error upon failure to close queue receiver
    public function __stop() returns error? {
        self.closeQueueReceiver(self.consumerActions);
        return ();
    }

    extern function closeQueueReceiver(QueueReceiverCaller actions);
};

# Configurations related to the QueueReceiver endpoint
#
# + session - JMS session object
# + queueName - Name of the queue
# + messageSelector - JMS selector statement
# + identifier - unique identifier for the subscription
public type QueueReceiverEndpointConfiguration record {
    Session? session = ();
    string? queueName = ();
    string messageSelector = "";
    string identifier = "";
    !...
};

# Caller actions related to queue receiver endpoint.
#
# + queueReceiver - queue receiver endpoint
public type QueueReceiverCaller client object {

    public QueueReceiver? queueReceiver = ();

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error upon failure to acknowledge the received message
    public remote extern function acknowledge(Message message) returns error?;

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or nil if the timeout exceeds, returns an error on JMS provider internal error
    public remote extern function receive(int timeoutInMilliSeconds = 0) returns (Message|error)?;

    # Synchronously receive a message from a given destination
    #
    # + destination - destination to subscribe to
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or () if the timeout exceeds, returns an error on JMS provider internal error
    public remote function receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|error)?;
};

remote function QueueReceiverCaller.receiveFrom(Destination destination, int timeoutInMilliSeconds = 0) returns (Message|
        error)? {
    var queueReceiver = self.queueReceiver;
    if (queueReceiver is QueueReceiver) {
        var session = queueReceiver.config.session;
        if (session is Session) {
            validateQueue(destination);
            queueReceiver.createQueueReceiver(session, queueReceiver.config.messageSelector,
            destination = destination);
        } else {
            log:printInfo("Session is (), Message receiver is not properly initialized for queue " +
            destination.destinationName);
        }
    } else {
         log:printInfo("Message receiver is not properly initialized for queue " + destination.destinationName);
    }
    var result = self->receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
    self.queueReceiver.closeQueueReceiver(self);
    return result;
}

function validateQueue(Destination destination) {
    if (destination.destinationName == "") {
        string errorMessage = "Destination name cannot be empty";
        map<any> errorDetail = { message: errorMessage };
        error queueReceiverConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic queueReceiverConfigError;
    } else if (destination.destinationType != "queue") {
        string errorMessage = "Destination should should be a queue";
        map<any> errorDetail = { message: errorMessage };
        error queueReceiverConfigError = error(JMS_ERROR_CODE, errorDetail);
        panic queueReceiverConfigError;
    }
}
