// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

// The server endpoint configuration.
listener grpc:Listener ep = new ({
    host:"localhost",
    port:9090
});

@grpc:ServiceDescriptor {
    descriptor: string.create(descriptorMap[DESCRIPTOR_KEY]),
    descMap: descriptorMap
}
service HelloWorld on ep {

    resource function hello (grpc:Caller caller, string name, grpc:Headers headers) {
        io:println("name: " + name);
        string message = "Hello " + name;
        // Reads custom headers in request message.
        io:println(headers.get("client_header_key"));

        // Writes custom headers to response message.
        grpc:Headers resHeader = new;
        resHeader.setEntry("server_header_key", "Response Header value");

        // Sends response message with headers.
        error? err = caller->send(message, headers = resHeader);
        io:println(err.message ?: "Server send response : " + message);

        // Sends `completed` notification to caller.
        _ = caller->complete();
    }
}

// Service descriptor data.
const string DESCRIPTOR_KEY = "HelloWorld.proto";
map<any> descriptorMap =
{
    "HelloWorld.proto":
    "0A1048656C6C6F576F726C642E70726F746F1A1E676F6F676C652F70726F746F6275662F77"
    + "726170706572732E70726F746F32530A0A48656C6C6F576F726C6412450A0568656C6C6F"
    + "121B676F6F676C652E70726F746F6275662E537472696E6756616C75651A1B676F6F676C"
    + "652E70726F746F6275662E537472696E6756616C756528003000620670726F746F33"
    ,

    "google/protobuf/wrappers.proto":
    "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F"
    + "676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565"
    + "180120012801221B0A0A466C6F617456616C7565120D0A0576616C756518012001280222"
    + "1B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74"
    + "363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565"
    + "120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A057661"
    + "6C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C75651801200128"
    + "08221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A42"
    + "7974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F67"
    + "6C652E70726F746F627566420D577261707065727350726F746F50015A2A676974687562"
    + "2E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273"
    + "F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F77"
    + "6E5479706573620670726F746F33"

};
