import ballerina.net.http;

@http:configuration {
    basePath:"/hello",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
    keyStorePassword:"ballerina",
    certPassword:"ballerina"
}

service<http> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:Request req, http:Response res) {
        res.setStringPayload("Successful");
        _ = res.send();
    }
}

@Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a 1-way ssl connection, client needs to provide trustStoreFile and trustStorePassword."}
function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient("https://localhost:9095", getConnectorConfigs());
    }
    //Creates a request.
    http:Request req = {};
    http:Response resp = {};
    resp, _ = httpEndpoint.get("/hello/", req);
    println("Response code: " + resp.getStatusCode());
    println("Response: " + resp.getStringPayload());
}

function getConnectorConfigs() (http:Options) {
    http:Options option = {
          ssl: {
                 trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                 trustStorePassword:"ballerina"
               },
          followRedirects: {}
    };
    return option;
}
