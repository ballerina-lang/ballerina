// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/mime;
import ballerina/http;

# This functions pulls a module from ballerina central.
#
# + definedEndpoint - Endpoint defined with the proxy configurations
# + accessToken - Access token
# + mdFileContent - Module.md file content of the module
# + summary - Summary of the module
# + homePageURL - Website URL of the module
# + repositoryURL - Source code URL of the module
# + apiDocURL - API documentation URL of the module
# + authors - Authors of the module
# + keywords - Keywords which describes the module
# + license - License of the module
# + url - URL to be invoked to push the module
# + dirPath - Directory path where the archived module resides
# + ballerinaVersion - Ballerina version the module is built
# + msg - Message printed when the module is pushed successfully which includes module info
# + baloVersion - Balo version of the module
function pushPackage (http:Client definedEndpoint, string accessToken, string mdFileContent, string summary, string homePageURL, string repositoryURL,
                string apiDocURL, string authors, string keywords, string license, string url, string dirPath, string ballerinaVersion, string msg, string baloVersion) {
    
    http:Client httpEndpoint = definedEndpoint;
    mime:Entity mdFileContentBodyPart = addStringBodyParts("description", mdFileContent);
    mime:Entity summaryBodyPart = addStringBodyParts("summary", summary);
    mime:Entity homePageURLBodyPart = addStringBodyParts("websiteURL", homePageURL);
    mime:Entity repositoryURLBodyPart = addStringBodyParts("repositoryURL", repositoryURL);
    mime:Entity apiDocURLBodyPart = addStringBodyParts("apiDocURL", apiDocURL);
    mime:Entity authorsBodyPart = addStringBodyParts("authors", authors);
    mime:Entity keywordsBodyPart = addStringBodyParts("keywords", keywords);
    mime:Entity licenseBodyPart = addStringBodyParts("license", license);
    mime:Entity ballerinaVersionBodyPart = addStringBodyParts("ballerinaVersion", ballerinaVersion);
    mime:Entity baloVersionBodyPart = addStringBodyParts("baloVersion", baloVersion);

    // Artifact
    mime:Entity filePart = new;
    filePart.setContentDisposition(getContentDispositionForFormData("artifact"));
    filePart.setFileAsEntityBody(untaint dirPath);
    var contentTypeSetResult = filePart.setContentType(mime:APPLICATION_OCTET_STREAM);
    if (contentTypeSetResult is error)  {
        panic contentTypeSetResult;
    }

    mime:Entity[] bodyParts = [filePart, mdFileContentBodyPart, summaryBodyPart, homePageURLBodyPart, repositoryURLBodyPart,
                               apiDocURLBodyPart, authorsBodyPart, keywordsBodyPart, licenseBodyPart, ballerinaVersionBodyPart, 
                               baloVersionBodyPart];
    http:Request req = new;
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);

    var result = httpEndpoint -> post("", req);
    http:Response httpResponse = new;
    if (result is http:Response) {
        httpResponse = result;
    } else {
        io:println("connection to the remote host failed : " + result.reason());
        return;
    }
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        io:println("remote registry failed for url :" + url);
    } else if (statusCode != "200") {
        var jsonResponse = httpResponse.getJsonPayload();
        if (jsonResponse is json) {
            string message = jsonResponse.message.toString();
            io:println(message);
        } else {
            io:println("invalid response json");
        }
    } else {
        io:println(msg);
    }
}

# This function will invoke the method to push the module.
# + args - Arguments passed
public function main (string... args) {
    http:Client httpEndpoint;
    string host = args[13];
    string strPort = args[14];
    if (host != "" && strPort != "") {
        var port = int.convert(strPort);
        if (port is int) {
            http:Client|error result = trap defineEndpointWithProxy(args[9], host, port, args[15], args[16]);
            if (result is http:Client) {
                httpEndpoint = result;
                pushPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[12], args[11], args[17]);
            } else {
                io:println("failed to resolve host : " + host + " with port " + port);
                return;
            }
        } else {
            io:println("invalid port : " + strPort);
        }
    } else  if (host != "" || strPort != "") {
        io:println("both host and port should be provided to enable proxy");     
        return;   
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[9]);
        pushPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[12], args[11], args[17]);
    }
}

# This function defines an endpoint with proxy configurations.
#
# + url - URL to be invoked
# + hostname - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Endpoint defined
function defineEndpointWithProxy (string url, string hostname, int port, string username, string password) returns http:Client{
    http:Client httpEndpoint = new (url, config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
            proxy : getProxyConfigurations(hostname, port, username, password)
    });
    return httpEndpoint;
}

# This function defines an endpoint without proxy configurations.
#
# + url - URL to be invoked
# + return - Endpoint defined
function defineEndpointWithoutProxy (string url) returns http:Client{
    http:Client httpEndpoint = new (url, config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        }
    });
    return httpEndpoint;
}

# This function will get the content disposition of the form data sent.
#
# + partName - Multipart name
# + return - `ContentDisposition` of the multipart
function getContentDispositionForFormData(string partName) returns (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

# This function will add string part information in multiparts.
#
# + key - Name of the multipart
# + value - String value to be included in the multipart
# + return - `Entity` with the part information.
function addStringBodyParts (string key, string value) returns (mime:Entity) {
    mime:Entity stringBodyPart = new;
    stringBodyPart.setContentDisposition(getContentDispositionForFormData(key));
    stringBodyPart.setText(untaint value);
    var contentTypeSetResult = stringBodyPart.setContentType(mime:TEXT_PLAIN);
    if (contentTypeSetResult is error)  {
        panic contentTypeSetResult;
    }
    return stringBodyPart;
}

# This function sets the proxy configurations for the endpoint.
#
# + hostName - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Proxy configurations for the endpoint
function getProxyConfigurations(string hostName, int port, string username, string password) returns http:ProxyConfig {
    http:ProxyConfig proxy = { host : hostName, port : port , userName: username, password : password };
    return proxy;
}
