package samples.message.passthrough;

import ballerina.lang.message;
import ballerina.net.connectors.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        ballerina.net.connectors.http:HTTPConnector nyseEP = new ballerina.net.connectors.http:HTTPConnector("http://localhost:8280/services", 100);

        message response;
        response = ballerina.net.connectors.http:http.post(nyseEP, "/EchoProxy", m);
        reply response;
    }
}