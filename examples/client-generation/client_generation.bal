import ballerina/http;
import ballerina/log;
import ballerina/swagger;

// Defines this endpoint as a selected endpoint for client generation
@swagger:ClientEndpoint
endpoint http:Listener helloEp {
    port: 9090
};

// Enable the client code generation for this service
@swagger:ClientConfig {
    generate: true
}
@http:ServiceConfig {
    basePath: "/sample"
}
service Hello bind helloEp {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hello"
    }
    hello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello");
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error when responding", err = result);
        }
    }
}
