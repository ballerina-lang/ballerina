
//
//Work in progress
//

function jsonFormat() {
    json j1;
    string s1;
    json j2;
    json j3;

    // following are examples of jason literal templates
    j1 = `{"name" : "ballerina"}`;
    s1 = json:get(j1, "$.name");
    //assert(s1 == "ballerina");

    json:set(j1, "$.name", "wso2");
    //assert(j1 == `{"name" : "wso2"}`);

    j2 = `{"fullName": {"firstName" : "Alice" , "lastName": "Bob"}}`;
    json:remove(j2, "$.fullName.lastName");
    //assert(j2 == `{"fullName": {"firstName" : "Alice"}}`);

    j3 = `{"letters" : ["a", "b", "c", "d"]}`;
    json:remove(j3, "$.letters[3]");
    //assert (j3 == `{"letters" : ["a", "b", "c"]}`);
}