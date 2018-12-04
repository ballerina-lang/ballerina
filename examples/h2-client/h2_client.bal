import ballerina/h2;
import ballerina/io;

// Create an endpoint for h2 database. Change the path before running the sample.
h2:Client testDB = new({
        path: "./h2-client",
        name: "testdb",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 5 }
    });

public function main() {
    // Creates a table using the update operation.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE STUDENT(ID INTEGER,
                    AGE INTEGER, NAME VARCHAR(255), PRIMARY KEY (ID))");
    handleUpdate(ret, "Create student table");

    // Inserts data to the table using the update operation.
    io:println("\nThe update operation - Inserting data to a table");
    ret = testDB->update("INSERT INTO student(id, age, name)
                          values (1, 23, 'john')");
    handleUpdate(ret, "Insert to student table with no parameters");

    // Select data using the `select` operation.
    io:println("\nThe select operation - Select data from a table");
    var selectRet = testDB->select("SELECT * FROM student", ());
    if (selectRet is table<record {}>) {
        // Convert a table to JSON.
        io:println("\nConvert the table into json");
        var jsonConversionRet = json.convert(selectRet);
        if (jsonConversionRet is json) {
            io:print("JSON: ");
            io:println(io:sprintf("%s", jsonConversionRet));
        } else {
            io:println("Error in table to json conversion");
        }
    } else if (selectRet is error) {
        io:println("Select data from student table failed: "
                + <string>selectRet.detail().message);
    }

    // Drop the STUDENT table.
    io:println("\nThe update operation - Drop student table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");
}

// Function to handle return of the update operation.
function handleUpdate(int|error returned, string message) {
    if (returned is int) {
        io:println(message + " status: " + returned);
    } else if (returned is error) {
        io:println(message + " failed: " + <string>returned.detail().message);
    }
}
