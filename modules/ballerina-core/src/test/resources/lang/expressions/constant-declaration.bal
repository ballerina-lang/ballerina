/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import ballerina.net.http;

const int CONST_INT = 20;
const long CONST_LONG = 2560000;
const float CONST_FLOAT = 23.6;
const double CONST_DOUBLE = 23.6;
const boolean CONST_BOOLEAN = true;
const string CONST_STRING = "Ballerina";

@BasePath ("/echo")
service echo {
    @GET
    @Path ("/message")
    resource echo (message m) {
        testFunction();
        http:convertToResponse(m);
        reply m;
    }
}

function testFunction() {
    int i;
    i =0;
}
