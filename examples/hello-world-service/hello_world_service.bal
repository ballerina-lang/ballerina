import ballerina/http;
import ballerina/log;

// By default, Ballerina exposes a service via HTTP/1.1.
service hello on new http:Listener(9090) {

    // Invoke all resources with arguments of server connector and request.
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;
        // Use a util method to set a string payload.
        res.setPayload("Hello, World!");

        // Send the response back to the caller.

        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}
