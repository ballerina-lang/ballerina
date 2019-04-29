import ballerina/io;

// Constants are defined using the `const` modifier.
const string GET = "GET";

// Constants can be defined without the type using the `var` keyword. Then the type is inferred from the right hand side.
const POST = "POST";

// Constants can be used to create type definitions. The type definition below specifies that a variable of the type
// `ACTION` can only hold either `GET` or `POST`.
type ACTION GET|POST;

// Integer constant.
const int I = 125;

// Constants of `map` type can be defined as well. The constraint of these constant maps should be either a simple type
// or an another map type which should eventually have a simple type constraint. `var` cannot be used to declare
// constant maps, so the correct type should be present when declaring the constant.
const map<string> data = {  "user": "Ballerina", "ID": "1234" };

// Constant with a nested map literals.
const map<map<string>> complexData = { "data": data, "data2": { "user": "WSO2" }};

public function main() {
    // This is identical to the code line `ACTION get = "GET";`.
    ACTION get = GET;
    testAction(get);

    ACTION post = "POST";
    testAction(POST);

    // We can concatenate `I` with any other integer value.
    int value = I + 10;
    io:println(value);

    io:println(data);
    io:println(complexData);

    // Values of a constant map can be accessed and used as usual.
    io:println(complexData.data.user);

    // But updating a constant map will produce a compile time error. Eg -
    // data.ID = "4321";
    // data.newField = 10;
}

function testAction(ACTION action) {
    if (action == GET) {
        io:println("GET action");
    } else if (action == POST) {
        io:println("POST action");
    }
}
