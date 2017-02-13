package samples.fork_join;

import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.net.http;

@BasePath ("/AirfareProviderService")
@Service(description = "Airfare service")
service AirfareProviderService {

    http:ClientConnector abcAirlineEP = new http:ClientConnector("http://localhost:8080/ABCAirline");
    http:ClientConnector xyzAirlineEP = new http:ClientConnector("http://localhost:8080/XYZAirline");

    xml airfareAggregatedResponse;

    @POST
    @Path ("/airfare")
    resource airfareResource (message m) {
        log:info("Airfare ");
        fork (message m) {
            worker ABC_Airline (message m) {
                xml payload;
                string from;
                string to;
                string date;
                string query;
                message response;

                payload = message:getXmlPayload(m);
                from = xml:get(payload, "reservationInfo/from");
                to = xml:get(payload, "reservationInfo/to");
                date = xml:get(payload, "reservationInfo/date");
                query = "?departure_city=" + from + "&destination_city=" + to + "&date=" + date;
                response = http:ClientConnector.sendGet (abcAirlineEP, query, m);
                reply response;
            }

            worker XYZ_Airline (message m) {
                xml payload;
                string from;
                string to;
                string date;
                string query;
                message response;

                payload = message:getXmlPayload(m);
                from = xml:get(payload, "reservationInfo/from");
                to = xml:get(payload, "reservationInfo/to");
                date = xml:get(payload, "reservationInfo/date");
                query = "?From=" + from + "&To=" + to + "&Date=" + date;
                response = http:ClientConnector.sendGet (xyzAirlineEP, query, m);
                reply response;
            }
        } join (all) (message[] airfareResponses) {
            airfareAggregatedResponse = `<airfareRes></airfareRes>`;
            xml:set(airfareAggregatedResponse, "/airfareRes", null, airfareResponses[0]);
            xml:set(airfareAggregatedResponse, "/airfareRes", null, airfareResponses[1]);
            system:logDebug(xml:toString(airfareAggregatedResponse));
            reply airfareAggregatedResponse;
        }
   }

}
