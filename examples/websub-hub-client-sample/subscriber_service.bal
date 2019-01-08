// Ballerina WebSub Subscriber service, which represents the callback registered at the Hub.
import ballerina/log;
import ballerina/websub;

// The endpoint to which the subscriber service is bound.
listener websub:Listener websubEP = new(8181);

// Annotations specifying the subscription parameters.
// Note the omission of `subscribeOnStartUp` as an annotation due to which a subscription request would not be sent
// automatically on start up.
// Also note the exclusion of the onIntentVerification resource which will result in auto intent-verification.
@websub:SubscriberServiceConfig {
    path: "/websub",
    topic: "http://websubpubtopic.com",
    hub: "https://localhost:9191/websub/hub",
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber on websubEP {

    // Resource accepting content delivery requests.
    resource function onNotification(websub:Notification notification) {
        var payload = notification.getPayloadAsString();
        if (payload is string) {
            log:printInfo("WebSub Notification Received: " + payload);
        } else {
            log:printError("Error retrieving payload as string", err = payload);
        }
    }
}
