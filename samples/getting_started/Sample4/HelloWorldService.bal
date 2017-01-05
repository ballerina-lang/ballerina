package org.wso2.ballerina.sample;

import ballerina.lang.message;
import ballerina.net.uri;

@BasePath ("/hello")
service HelloWorldService {

    @GET
    resource sayHello(message m) {

        string name;
        message response;
        string greeting;

        name = uri:getQueryParam(m, "name");

        if (name == "") {
            greeting = "Hello, Welcome to Ballerina!";
        } else {
            greeting = "Hello "+name+", Welcome to Ballerina!";
        }

        message:setStringPayload(response, greeting);

        reply response;

    }
}