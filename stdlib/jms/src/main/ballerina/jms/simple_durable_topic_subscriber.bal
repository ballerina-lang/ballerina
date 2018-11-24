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

# Simple Durable Topic Subscriber endpoint
# Simplified endpoint to consume from a topic without the explicit creation for JMS connection and session
#
# + config - configurations related to the endpoint
public type SimpleDurableTopicConsumer object {

    *AbstractListener;

    public SimpleDurableTopicConsumerEndpointConfiguration config = {};

    private Connection? connection = ();
    private Session? session = ();
    private DurableTopicConsumer? subscriber = ();
    private SimpleDurableTopicCaller? consumerActions = ();

    # Initializes the simple durable topic subscriber endpoint
    #
    # + c - Configurations related to the endpoint
    public function __init(SimpleDurableTopicConsumerEndpointConfiguration c) {
        self.config = c;
        Connection conn = new({
                initialContextFactory: self.config.initialContextFactory,
                providerUrl: self.config.providerUrl,
                connectionFactoryName: self.config.connectionFactoryName,
                properties: self.config.properties
            });
        self.connection = conn;

        Session newSession = new(conn, {
                acknowledgementMode: self.config.acknowledgementMode
            });
        self.session = newSession;

        DurableTopicConsumerEndpointConfiguration consumerConfig = {session: newSession,
                                                                          topicPattern: c.topicPattern,
                                                                          messageSelector: c.messageSelector,
                                                                          identifier: c.identifier};
        DurableTopicConsumer topicSubscriber = new (consumerConfig);
        self.subscriber = topicSubscriber;
        self.consumerActions = new SimpleDurableTopicCaller(topicSubscriber.getCallerActions(),
                                   newSession, c.identifier);
    }

    # Binds the endpoint to a service
    #
    # + serviceType - type descriptor of the service to bind to
    public function __attach(service s, map<any> data) returns error?  {
        var subscriber = self.subscriber;
        if (subscriber is DurableTopicConsumer) {
            var result = subscriber.__attach(s, data);
            if(result is error) {
                return result;
            } else {
                return ();
            }
        } else {
            string errorMessage = "Topic Subscriber cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            return e;
        }
    }

    # Starts the endpoint. Function is ignored by the subscriber endpoint
    public function __start() returns error? {
        return ();
    }

    //# Retrieves the durable topic subscriber consumer actions
    //#
    //# + return - Durable topic subscriber actions
    //public function getCallerActions() returns SimpleDurableTopicSubscriberActions {
    //    var consumerActions = self.consumerActions;
    //    if (consumerActions is SimpleDurableTopicSubscriberActions) {
    //        return consumerActions;
    //    } else {
    //        string errorMessage = "Consumer actions cannot be nil";
    //        map errorDetail = { message: errorMessage };
    //        error e = error(JMS_ERROR_CODE, errorDetail);
    //        panic e;
    //    }
    //}

    # Stops the endpoint. Function is ignored by the subscriber endpoint
    public function __stop() returns error? {
        return ();
    }

    # Creates a text message that can be sent through any JMS message producer to a queue or topic.
    #
    # + message - text content of the message
    # + return - the created message, or nil if the session is nil
    public function createTextMessage(string message) returns Message|error {
        var session = self.session;
        if (session is Session) {
            return session.createTextMessage(message);
        } else {
            string errorMessage = "Session cannot be nil";
            map errorDetail = { message: errorMessage };
            error e = error(JMS_ERROR_CODE, errorDetail);
            panic e;
        }
    }
};

# Configurations of the simple durable topic subscriber endpoint
#
# + initialContextFactory - JMS initial context factory name
# + providerUrl - Connection url of the JMS provider
# + connectionFactoryName - Name of the JMS connection factory created
# + acknowledgementMode - Sets the acknowledgment mode for the underlying session. String representation of the
#                         JMS acknowledgment mode needs to be provided.
# + identifier - Unique identifier for the subscriber
# + properties - Custom properties related to JMS provider
# + messageSelector - JMS selector statement
# + topicPattern - Name or the pattern of the topic subscription
public type SimpleDurableTopicConsumerEndpointConfiguration record {
    string initialContextFactory = "bmbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string identifier = "";
    map properties = {};
    string messageSelector = "";
    string topicPattern = "";
    !...
};


# Caller actions related to durable topic subscriber endpoint
public type SimpleDurableTopicCaller client object {

    private DurableTopicCaller helper;
    private Session session;
    private string identifier;

    public function __init(DurableTopicCaller subscriberActions, Session session, string id) {
        self.helper = subscriberActions;
        self.session = session;
        self.identifier = id;
    }

    # Acknowledges a received message
    #
    # + message - JMS message to be acknowledged
    # + return - error upon failure to acknowledge the received message
    public remote function acknowledge(Message message) returns error? {
        return self.helper->acknowledge(message);
    }

    # Synchronously receive a message from the JMS provider
    #
    # + timeoutInMilliSeconds - time to wait until a message is received
    # + return - Returns a message or nill if the timeout exceededs. Returns an error on jms provider internal error.
    public remote function receive(int timeoutInMilliSeconds = 0) returns (Message|error)? {
        return self.helper->receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
    }

    # Unsubscribes the durable subscriber from topic
    #
    # + return - Returns an error on JMS provider internal error
    public remote function unsubscribe() returns error? {
        return self.session.unsubscribe(self.identifier);
    }
};
