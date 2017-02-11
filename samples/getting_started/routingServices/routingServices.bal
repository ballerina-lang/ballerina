import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;

@BasePath ("/cbr")
service contentBasedRouting {

    @POST
    resource cbrResource (message m) {

        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");

        string nyseString = "nyse";

        json jsonMsg = message:getJsonPayload(m);
        string nameString = json:getString(jsonMsg, "$.name");

        message response = {};

        if (nameString == nyseString) {
            response = http:ClientConnector.post(nyseEP, "/", m);
        } else {
            response = http:ClientConnector.post(nasdaqEP, "/", m);
        }

        reply response;
    }
}

@BasePath ("/hbr")
service headerBasedRouting {

    @GET
    resource cbrResource (message m) {

        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");

        string nyseString = "nyse";

        string nameString = message:getHeader(m, "name");

        message response = {};

        if (nameString == nyseString) {
            response = http:ClientConnector.post(nyseEP, "/", m);
        } else {
            response = http:ClientConnector.post(nasdaqEP, "/", m);
        }

        reply response;
    }
}

@BasePath("/nyseStocks")
service nyseStockQuote {

    @POST
    resource stocks (message m) {

        message response = {};

        json payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);

        reply response;
    }
}

@BasePath("/nasdaqStocks")
service nasdaqStocksQuote {

    @POST
    resource stocks (message m) {

        message response = {};

        json payload = `{"exchange":"nasdaq", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);

        reply response;
    }
}
