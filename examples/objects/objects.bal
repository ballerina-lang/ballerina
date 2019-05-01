import ballerina/io;

// Defines an object called `Person`. It contains `public`, `private` and module-level visible fields along with their types.
// For fields, the default value must be specified if an explicit object initializer method is not defined.
type Person object {
    public string name = "";
    public int age = 0;
    // If you need to have a field with own type, you need to make that field defaultable
    // (i.e., able to specify a default value).
    // Here we have made the `parent` field defaultable by making it nilable.
    public Person? parent = ();
    // Private fields are only visible within the object and its functions.
    private string email = "default@abc.com";
    // Protected fields (i.e., no access modifiers) are visible only within the same module.
    string address = "No 20, Palm grove";
};

public function main() {
    // There are three ways to initialize a `Person` object.
    Person p1 = new;
    io:println(p1);

    Person p2 = new();
    io:println(p2);

    // This type of initialization is useful when you can not determine the type of the object to be
    // created based on the context (e.g., when the left hand side is a union of object types).
    Person p3 = new Person();
    io:println(p2);
}
