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
import ballerina/grpc;
import ballerina/io;

listener grpc:Server server = new ({
    host:"localhost",
    port:9090
});

@grpc:ServiceDescriptor {
    descriptor: <string>descriptorMap1[DESCRIPTOR_KEY_1],
    descMap: descriptorMap1
}
service HelloWorld on server {

    resource function testInputNestedStruct(grpc:Caller caller, Person req) {
        io:println("name: " + req.name);
        io:println(req.address);
        string message = "Submitted name: " + req.name;
        io:println("Response message " + message);
        error? err = caller->send(message);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    resource function testOutputNestedStruct(grpc:Caller caller, string name) {
        io:println("requested name: " + name);
        Person person = {name:"Sam", address:{postalCode:10300, state:"CA", country:"USA"}};
        io:println(person);
        error? err = caller->send(person);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    resource function testInputStructOutputStruct(grpc:Caller caller, StockRequest req) {
        io:println("Getting stock details for symbol: " + req.name);
        StockQuote res = {symbol:"WSO2", name:"WSO2.com", last:149.52, low:150.70, high:
        149.18};
        io:println(res);
        error? err = caller->send(res);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    resource function testInputStructNoOutput(grpc:Caller caller, StockQuote req) {
        io:println("Symbol: " + req.symbol);
        io:println("Name: " + req.name);
        io:println("Last: " + req.last);
        io:println("Low: " + req.low);
        io:println("High: " + req.high);
    }

    resource function testNoInputOutputStruct(grpc:Caller caller) {
        StockQuote res = {symbol:"WSO2", name:"WSO2 Inc.", last:14.0, low:15.0, high:16.0};
        StockQuote res1 = {symbol:"Google", name:"Google Inc.", last:100.0, low:101.0, high:102.0};
        StockQuotes quotes = {stock:[res, res1]};

        error? err = caller->send(quotes);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    resource function testNoInputOutputArray(grpc:Caller caller) {
        string[] names = ["WSO2", "Google"];
        StockNames stockNames = {names:names};
        error? err = caller->send(stockNames);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }
}

type Person record {
    string name = "";
    Address address = {};
};

type Address record {
    int postalCode = 0;
    string state = "";
    string country = "";
};

type StockQuote record {
    string symbol = "";
    string name = "";
    float last = 0.0;
    float low = 0.0;
    float high = 0.0;
};

type StockRequest record {
    string name = "";
};

type StockQuotes record {
    StockQuote[] stock = [];
};

type StockNames record {
    string[] names = [];
};

const string DESCRIPTOR_KEY_1 = "HelloWorld.proto";
map descriptorMap1 =
{
    "HelloWorld.proto":"0A1048656C6C6F576F726C642E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F224D0A06506572736F6E12120A046E616D6518012001280952046E616D65122F0A076164647265737318022001280B32152E6772706373657276696365732E4164647265737352076164647265737322590A0741646472657373121E0A0A706F7374616C436F6465180120012803520A706F7374616C436F646512140A0573746174651802200128095205737461746512180A07636F756E7472791803200128095207636F756E74727922220A0C53746F636B5265717565737412120A046E616D6518012001280952046E616D6522720A0A53746F636B51756F746512160A0673796D626F6C180120012809520673796D626F6C12120A046E616D6518022001280952046E616D6512120A046C61737418032001280252046C61737412100A036C6F7718042001280252036C6F7712120A0468696768180520012802520468696768223D0A0B53746F636B51756F746573122E0A0573746F636B18012003280B32182E6772706373657276696365732E53746F636B51756F7465520573746F636B22220A0A53746F636B4E616D657312140A056E616D657318012003280952056E616D657332E3030A0A48656C6C6F576F726C64124B0A1574657374496E7075744E657374656453747275637412142E6772706373657276696365732E506572736F6E1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565124C0A16746573744F75747075744E6573746564537472756374121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A142E6772706373657276696365732E506572736F6E12530A1B74657374496E7075745374727563744F7574707574537472756374121A2E6772706373657276696365732E53746F636B526571756573741A182E6772706373657276696365732E53746F636B51756F7465124B0A1774657374496E7075745374727563744E6F4F757470757412182E6772706373657276696365732E53746F636B51756F74651A162E676F6F676C652E70726F746F6275662E456D707479124C0A17746573744E6F496E7075744F757470757453747275637412162E676F6F676C652E70726F746F6275662E456D7074791A192E6772706373657276696365732E53746F636B51756F746573124A0A16746573744E6F496E7075744F7574707574417272617912162E676F6F676C652E70726F746F6275662E456D7074791A182E6772706373657276696365732E53746F636B4E616D6573620670726F746F33",

    "google/protobuf/wrappers.proto":
    "0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33",

    "google/protobuf/empty.proto":
    "0A0B656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942540A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};
