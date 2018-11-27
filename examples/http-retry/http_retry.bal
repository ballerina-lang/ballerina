import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Define the end point to the call the `mockHelloService`.
endpoint http:Client backendClientEP {
    url: "http://localhost:8080",
    // Retry configuration options.
    retryConfig: {

        // Initial retry interval in milliseconds.
        interval: 3000,

        // Number of retry attempts before giving up
        count: 3,

        // Multiplier of the retry interval to exponentailly
        // increase; retry interval
        backOffFactor: 2.0,

        // Upper limit of the retry interval in milliseconds
        // If interval into backOffFactor value exceeded
        // maxWaitInterval interval values. maxWaitInterval
        // will be considered as the retry intrval.
        maxWaitInterval: 20000

    },
    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/retry"
}
service<http:Service> retryDemoService bind { port: 9090 } {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and an object of
    // the request data.
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendResponse = backendClientEP->get("/hello", message = untaint request);
        // `match` is used to handle union-type returns.
        // If a response is returned, the normal process runs.
        // If the service does not get the expected response,
        // the error-handling logic is executed.
        match backendResponse {

            http:Response response => {
                // '->' signifies remote call.

                var result = caller->respond(response);

                if (result is error) {
                    log:printError("Error sending response", err = result);
                }

            }
            error responseError => {
                // Create a new HTTP response by looking at the error message.
                http:Response errorResponse = new;
                errorResponse.statusCode = 500;
                errorResponse.setPayload(responseError.message);

                var result = caller->respond(errorResponse);

                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
            }
        }
    }
}

public int counter = 0;

// This sample service is used to mock connection timeouts and service outages.
// The service outage is mocked by stopping/starting this service.
// This should run separately from the `retryDemoService` service.

@http:ServiceConfig { basePath: "/hello" }
service<http:Service> mockHelloService bind { port: 8080 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        counter = counter + 1;
        if (counter % 4 != 0) {
            log:printInfo(
                "Request received from the client to delayed service.");
            // Delay the response by 5000 milliseconds to
            // mimic network level delays.
            runtime:sleep(5000);

            http:Response res = new;
            res.setPayload("Hello World!!!");
            var result = caller->respond(res);

            if (result is error) {
                log:printError("Error sending response from mock service", err = result);
            }
        } else {
            log:printInfo(
                "Request received from the client to healthy service.");
            http:Response res = new;
            res.setPayload("Hello World!!!");
            var result = caller->respond(res);

            if (result is error) {
                log:printError("Error sending response from mock service", err = result);
            }
        }
    }
}
