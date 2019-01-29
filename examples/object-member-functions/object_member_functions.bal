import ballerina/io;

// Defines an object called `Person`. It has attached functions both inside and outside of the object.
type Person object {
    public int age;
    public string firstName;
    public string lastName;

    function __init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    // Function defined within the `object`.
    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

    // Function declaration defined within the `object`.
    function checkAndModifyAge(int condition, int a);
};

// Implementation for the declared function.
function Person.checkAndModifyAge(int condition, int a) {
    // The keyword `self` is bound to the `object` and can be used to
    // access fields and functions of the `object`.
    if (self.age < condition) {
        self.age = a;
    }
}

public function main() {
    // Initializing variable of `object` type `Person`
    Person p1 = new(5, "John", "Doe");
    io:println(p1);

    io:println(p1.getFullName());

    p1.checkAndModifyAge(10, 50);

    io:println(p1);
}
