import ballerina/io;

type Person record {
    string name = "";
    int age = 0;
};

type Employee record {
    string name = "";
    int age = 0;
    int empNo = 0;
};

function assertTypes(anydata emp) {
    // The `emp` variable is asserted to be of type `Employee`, and if successful the value is assigned to the variable
    // `employee`.
    Employee employee = <Employee>emp;
    io:println("Type asserted employee name: ", employee.name);

    // Asserting `emp` to be of type `Person` will result in a panic, since `emp` is inherently an `Employee`. `trap` is used to
    // handle the error.
    Person|error person = trap <Person>emp;
    io:println("Type asserted person name or error: ",
               (person is Person) ? person.name : person.detail().message);
}

public function main() {
    Employee emp = { name: "Speedy Gonzales", age: 4, empNo: 1 };
    assertTypes(emp);
}
