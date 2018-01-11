package ballerina.net.http.transactions.coordinator;

import ballerina.net.http;

public connector ParticipantClient () {

    action prepare (string transactionId, string participantURL) returns
                                                                 (string status, error err) {
        endpoint<http:HttpClient> participantEP {
            create http:HttpClient(participantURL, {});
        }
        http:Request req = {};
        PrepareRequest prepareReq = {transactionId:transactionId};
        var j, _ = <json>prepareReq;
        req.setJsonPayload(j);
        var res, communicationErr = participantEP.post("/prepare", req);
        if (communicationErr == null) {
            var prepareRes, transformErr = <PrepareResponse>res.getJsonPayload();
            if (transformErr == null) {
                int statusCode = res.getStatusCode();
                string msg = prepareRes.message;
                if (statusCode == 200) {
                    status = msg;
                } else if (statusCode == 404 && msg == "Transaction-Unknown") {
                    err = {msg:msg};
                } else {
                    err = {msg:"Prepare failed. Transaction: " + transactionId + ", Participant: " + participantURL};
                }
            } else {
                err = (error)transformErr;
            }
        } else {
            err = (error)communicationErr;
        }
        return;
    }

    action notify (string transactionId, string participantURL, string message) returns
                                                                                (string status, error err) {
        endpoint<http:HttpClient> participantEP {
            create http:HttpClient(participantURL, {});
        }
        http:Request req = {};
        NotifyRequest notifyReq = {transactionId:transactionId, message:message};
        var j, _ = <json>notifyReq;
        req.setJsonPayload(j);
        var res, communicationErr = participantEP.post("/notify", req);
        if (communicationErr == null) {
            var notifyRes, transformErr = <NotifyResponse>res.getJsonPayload();
            if (transformErr == null) {
                int statusCode = res.getStatusCode();
                string msg = notifyRes.message;
                if (statusCode == 200) {
                    if (transformErr == null) {
                        status = msg;
                    } else {
                        err = (error)transformErr;
                    }
                } else if ((statusCode == 400 && msg == "Not-Prepared") ||
                           (statusCode == 404 && msg == "Transaction-Unknown") ||
                           (statusCode == 500 && msg == "Failed-EOT")) {
                    err = {msg:msg};
                } else {
                    err = {msg:"Notify failed. Transaction: " + transactionId + ", Participant: " + participantURL};
                }
            } else {
                err = (error)transformErr;
            }
        } else {
            err = (error)communicationErr;
        }
        return;
    }
}
