// The order management HTTP service acting as a Ballerina WebSub Publisher brings up an internal Ballerina WebSub Hub
// at which it will publish updates.
import ballerina/http;
import ballerina/log;
import ballerina/websub;

listener http:Listener httpListener = new(9090);

// The topic against which the publisher will publish updates, and the subscribers
// need to subscribe to, to receive notifications when an order is placed
final string ORDER_TOPIC = "http://localhost:9090/ordermgt/ordertopic";

// An in-memory map to which orders will be added for demonstration
map<json> orderMap = {};

// Invoke the function that start up a Ballerina WebSub Hub, register the topic
// against which updates will be published, and maintain a reference to the
// returned hub object to publish updates
websub:WebSubHub webSubHub = startHubAndRegisterTopic();

@http:ServiceConfig {
    basePath: "/ordermgt"
}
service orderMgt on httpListener {

    // Resource accepting discovery requests
    // Requests received at this resource would respond with a Link Header
    // indicating the topic to subscribe to and the hub(s) to subscribe at
    @http:ResourceConfig {
        methods: ["GET", "HEAD"],
        path: "/order"
    }
    resource function discoverPlaceOrder(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], ORDER_TOPIC);
        response.statusCode = 202;
        var result = caller->respond(response);
        if (result is error) {
           log:printError("Error responding on ordering", err = result);
        }
    }

    // Resource accepting order placement requests
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/order"
    }
    resource function placeOrder(http:Caller caller, http:Request req) {
        var orderReq = req.getJsonPayload();
        if (orderReq is json) {
            string orderId = orderReq.Order.ID.toString();
            orderMap[orderId] = orderReq;

            // Create the response message indicating successful order creation.
            http:Response response = new;
            response.statusCode = 202;
            var result = caller->respond(response);
            if (result is error) {
               log:printError("Error responding on ordering", err = result);
            }

            // Publish the update to the Hub, to notify subscribers
            string orderCreatedNotification = "New Order Added: " + orderId;
            log:printInfo(orderCreatedNotification);
            result = webSubHub.publishUpdate(ORDER_TOPIC,
                                                    orderCreatedNotification);
            if (result is error) {
                log:printError("Error publishing update", err = result);
            }
        } else {
            log:printError("Error retrieving payload", err = orderReq);
            panic orderReq;
        }
    }

}

// Start up a Ballerina WebSub Hub on port 9191 and register the topic against
// which updates will be published
function startHubAndRegisterTopic() returns websub:WebSubHub {
    var hubStartUpResult = websub:startHub(new http:Listener(9191));
    websub:WebSubHub internalHub = hubStartUpResult is websub:HubStartedUpError
                    ? hubStartUpResult.startedUpHub : hubStartUpResult;

    var result = internalHub.registerTopic(ORDER_TOPIC);
    if (result is error) {
        log:printError("Error registering topic", err = result);
    }
    return internalHub;
}
