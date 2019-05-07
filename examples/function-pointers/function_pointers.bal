import ballerina/io;

// The `test` function acts as a variable function pointer in the `main` function
function test(int x, string s) returns float {
    int|error y = int.convert(s);
    float f = 0.0;

    if (y is int) {
        f = x * 1.0 * y;
    } else {
        // The type of `y` within the else block would be `error`, since the `int`
        // case is already handled by the `if` block.
        panic y;
    }
    return f;
}

// Function pointer as a parameter. Use the `.call()` method to invoke the function using the function pointer.
function foo(int x, function (int, string) returns float bar)
             returns float {
    return x * bar.call(10, "2");
}

// Function pointer as a return type.
function getFunctionPointer() returns (function (int, string) returns float) {
    return test;
}

public function main() {
    // Value `test` will serve as a function pointer to the `foo` function.
    io:println("Answer: " + foo(10, test));
    io:println("Answer: " + foo(10, getFunctionPointer()));
    // Function pointer as a variable.
    function (int, string) returns float f = getFunctionPointer();
    io:println("Answer: " + foo(10, f));
}
