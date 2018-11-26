import ballerina/jms;
import ballerina/log;

// This initializes a JMS connection with the provider.
jms:Connection jmsConnection = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon?"
                + "brokerlist='tcp://localhost:5672'"
});

// This initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
});

jms:QueueSender queueSender = new({
    session:jmsSession,
    queueName:"MyQueue"
});

public function main() {
    // This creates a Text message.
    var msg = jmsSession.createTextMessage("Hello from Ballerina");
    if (msg is jms:Message) {
        var returnVal = queueSender->send(msg);
        if (returnVal is error) {
            log:printError("Error occurred while sending message",
                err=returnVal);
        }
    } else {
        log:printError("Error occurred while creating message", err=msg);
    }
}
