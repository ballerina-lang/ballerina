// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

// Server endpoint configuration.
endpoint grpc:Listener ep {
    host: "localhost",
    port: 9090
};

service HelloWorld bind ep {

    hello(endpoint caller, string name) {
        io:println("Received message from : " + name);
        string message = "Hello " + name;

        // Send a response message to the caller.
        error? err = caller->send(message);

        io:println(err.message ?: "Server send response : " + message);
        // Send the `completed` notification to the caller.
        _ = caller->complete();
    }
}
