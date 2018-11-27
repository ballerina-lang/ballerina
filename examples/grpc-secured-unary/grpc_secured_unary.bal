// This is the server implementation for the secured connection (HTTPS) scenario.
import ballerina/grpc;
import ballerina/io;

// Server endpoint configuration with the SSL configurations.
endpoint grpc:Listener listener {
    host: "localhost",
    port: 9090,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

service HelloWorld bind listener {
    hello(endpoint caller, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;

        // Send a response message to the caller.
        error? err = caller->send(message);

        io:println(err.message ?: "Server send response : " + message);
        // Send the `completed` notification to the caller.
        _ = caller->complete();

    }
}
