import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection jmsConnection = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon"
                + "?brokerlist='tcp://localhost:5672'"
});

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
    acknowledgementMode:"SESSION_TRANSACTED"
});

endpoint jms:QueueSender queueSender {
    session:jmsSession,
    queueName:"MyQueue"
};

public function main() {
    // Message is published within the transaction block.
    transaction {
    // Create a Text message.
        match (jmsSession.createTextMessage("Hello from Ballerina")) {
            error e => {
                log:printError("Error occurred while creating message",
                               err = e);
            }

            jms:Message msg => {
                // Send the Ballerina message to the JMS provider.
                var result = queueSender->send(msg);
                if (result is error) {
                    log:printError("Error occurred while sending message", err = result);
                }
            }
        }
    }
}
