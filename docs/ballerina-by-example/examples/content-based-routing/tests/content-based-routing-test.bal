import ballerina/test;
import ballerina/io;

boolean serviceStarted;

function startService(){
    serviceStarted = test:startServices("content-based-routing");
}

@test:Config {
    before:"startService",
    after:"stopService",
    enable:false
}
function testFunc() {
    // Invoking the main function
    endpoint http:Client httpEndpoint { targets:[{ url:"http://localhost:9090" }] };
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");
    json payload = {"name" : "sanFrancisco"};
    json payload2 = {"name" : "sanFrancisco"};

    json response1 = {
        "name": "San Francisco Test Station,USA",
        "longitude": -122.43,
        "latitude": 37.76,
        "altitude": 150,
        "rank": 1
    };

    json response2 = {
        "name": "London Test Station,England",
        "longitude": -156.49,
        "latitude": 57.76,
        "altitude": 430,
        "rank": 5
    };

    http:Request req = new;
    req.setJsonPayload(payload);
    // Send a GET request to the specified endpoint
    var response = httpEndpoint -> post("/cbr/route", req);
    match response {
        http:Response resp => {
            var jsonRes = check resp.getJsonPayload();
            test:assertEquals(jsonRes, response1);
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req2 = new;
    req.setJsonPayload(payload2);
    // Send a GET request to the specified endpoint
    var respnc = httpEndpoint -> post("/cbr/route", req2);
    match respnc {
        http:Response resp => {
            var jsonRes = check resp.getJsonPayload();
            test:assertEquals(jsonRes, response2);
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService(){
    test:stopServices("content-based-routing");
}