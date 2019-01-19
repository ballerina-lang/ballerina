import ballerina/http;
import ballerina/io;
import ballerina/crypto;


endpoint http:Listener echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP{

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo (endpoint caller, http:Request req) {
        http:Response resp = new;
        var result = req.getTextPayload();
        match result {
            error payloadError => io:println(payloadError.message);
            string payload => {
                resp.setTextPayload(<string>crypto:unsafeMarkUntainted(payload));
                _ = caller -> respond(resp);
            }
        }
    }
}
