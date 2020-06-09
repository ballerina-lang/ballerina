## Module overview

This module provides the common interface and functionality to interact with database. The corresponding database
clients can be created by using specific database modules such as `MySQL` or using Java Database Connectivity 
module `JDBC`. The available database modules are listed below.

### List of Database Modules
1. [JDBC (Java Database Connectivity) Module](https://ballerina.io/learn/api-docs/ballerina/java.jdbc/index.html)  
This module can be used to connect with any database by simply providing the JDBC URL, and other related properties. 
Please refer the JDBC module [API docs](https://ballerina.io/learn/api-docs/ballerina/java.jdbc/index.html) for 
more details.

2. [MySQL Module](https://ballerina.io/learn/api-docs/ballerina/mysql/index.html)   
This module is specially designed to work with MySQL database, and allows to access the functionality 
provided by MySQL 8.0.x onwards. Please refer the MySQL module [API docs](https://ballerina.io/learn/api-docs/ballerina/mysql/index.html) 
for more details.


### Client

The database client should be created by any of the above listed database modules, and once it is created the 
below explained operations and functionality can be used. 

#### Connection pool handling

All database modules share the same connection pooling concept, and there are 3 possible scenarios for 
connection pool handling. Please refer `sql:ConnectionPool` for its properties, and possible values. 

1. Global, shareable default connection pool

    If you do not provide the `poolOptions` field when creating the database client, a globally shareable pool will be 
    created for your database unless a connection pool matching with the properties you provided already exists. 
    Below shown is the example from JDBC module to use the global connection pool. 

    ```ballerina
    jdbc:Client dbClient = new ("jdbc:mysql://localhost:3306/testdb", 
                                "root", "root");
    ```

2. Client owned, unsharable connection pool

    If you define the `connectionPool` field inline when creating the database client with `sql:ConnectionPool` type, 
    an unshareable connection pool will be created. Below shown is the example from JDBC module to use the global 
    connection pool.

    ```ballerina
    jdbc:Client dbClient = new (url = "jdbc:mysql://localhost:3306/testdb", 
                                connectionPool = { maxOpenConnections: 5 });
    ```

3. Local shareable connection pool

    If you create a record of type `sql:ConnectionPool` and reuse that in the configuration of multiple clients, 
    for each  set of clients that connects to the same database instance with the same set of properties, a shared 
    connection pool will be created. Below shown is the example from JDBC module to use the global connection pool.

    ```ballerina
    sql:ConnectionPool connPool = {maximumPoolSize: 5};
    
    jdbc:Client dbClient1 = new (url = "jdbc:mysql://localhost:3306/testdb",    
                                 connectionPool = connPool);
    jdbc:Client dbClient2 = new (url = "jdbc:mysql://localhost:3306/testdb",       
                                 connectionPool = connPool);
    jdbc:Client dbClient3 = new (url = "jdbc:mysql://localhost:3306/testdb",    
                                 connectionPool = connPool);
    ```
    
#### Closing the client

Once all database operations are performed you can close the database client you have created by invoking `close()`
operation. This will close the corresponding connection pool if it is not shared by any other database clients. 

```ballerina
error? e = dbClient.close();
if (e is error){
    io:println("Error occured:", e);
}

```    
### Database operations

Once the client is created, database operations can be executed through that client. This module defines the interface 
and common properties that are shared among multiple database clients.  It also supports querying, inserting, deleting, 
updating, and batch updating data.  

#### Creating tables

This sample creates a table with two columns. One column is of type `int`, and the other is of type `varchar`.
The CREATE statement is executed via the `execute` remote function of the client.

```ballerina
// Create the ‘Students’ table with fields ‘id’, 'name' and ‘age’.
var ret = dbClient->execute("CREATE TABLE student(id INT AUTO_INCREMENT, " +
                         "age INT, name VARCHAR(255), PRIMARY KEY (id))");
if (ret is sql:ExecutionResult) {
    io:println("Students table create status in DB: ", ret.affectedRowCount);
} else {
    error err = ret;
    io:println("Students table creation failed: ",
                <string>err.detail()["message"]);
}
```

#### Inserting data

This sample shows three examples of data insertion by executing an INSERT statement using the `execute` remote function 
of the client.

In the first example, query parameter values are passed directly into the query statement of the `execute` 
remote function.

```ballerina
var ret = dbClient->execute("INSERT INTO student(age, name) " +
                         "values (23, 'john')");
if (ret is sql:ExecutionResult) {
    io:println("Inserted row count to Students table: ", ret.affectedRowCount);
} else {
    error err = ret;
    io:println("Insert to Students table failed: ",
                <string>err.detail()["message"]);
}
```

In the second example, the parameter values, which are in local variables, are used to parameterize the SQL query in 
`execute` remote function. This type of parameterized SQL query can be used with any primitive Ballerina type 
like `string`, `int`, `float`, or `boolean`, and in that case, the corresponding SQL type of the parameter is derived 
from the type of the Ballerina variable that is passed in. 

```ballerina
string name = "Anne";
int age = 8;

var ret = dbClient->execute(`INSERT INTO student(age, name) 
                             values (${age}, ${name})`);
if (ret is sql:ExecutionResult) {
    io:println("Inserted row count to Students table: ", ret.affectedRowCount);
} else {
    error err = ret;
    io:println("Insert to Students table failed: ",
                <string>err.detail()["message"]);
}
```

In the third example, parameter values are passed as a `sql:TypedValue` to the `execute` remote function. Use 
corresponding subtype of `sql:TypedValue` such as `sql:Varchar`, `sql:Char`, `sql:Integer`, etc when you need to 
provide more details such as the exact SQL type of the parameter.

```ballerina
sql:Varchar name = new ("James");
sql:Integer age = new (10);

var ret = dbClient->execute(`INSERT INTO student(age, name) 
                             values (${age}, ${name})`);
if (ret is sql:ExecutionResult) {
    io:println("Inserted row count to Students table: ", ret.affectedRowCount);
} else {
    error err = ret;
    io:println("Insert to Students table failed: ",
                <string>err.detail()["message"]);
}
```

#### Inserting data with auto-generated keys

This example demonstrates inserting data while returning the auto-generated keys. It achieves this by using the 
`execute` remote function to execute the INSERT statement.

```ballerina
int age = 31;
string name = "Kate";
var ret = dbClient->execute(`INSERT INTO student(age, name) 
                             values (${age}, ${name})`);
if (ret is sql:ExecutionResult) {
    int? count = ret.affectedRowCount;
    string|int? generatedKey = ret.lastInsertId;
    io:println("Inserted row count: ", count);
    io:println("Generated key: ", generatedKey);
} else {
    error err = ret;
    io:println("Insert to table failed: ", <string>err.detail()["message"]);
}
```

#### Querying data

This sample shows three examples to demonstrate the different usages of the `query` operation and query the
database table and obtain the results. 

This example demonstrates querying data from a table in a database. 
First, a type is created to represent the returned result set. Please note the mapping of the database column 
to the returned record's property is case insensitive (i.e., `ID` column in result, can be mapped to `id` 
property in record). Next, the SELECT query is executed via the `query` remote function of the client by passing that 
result set type. Once the query is executed, each data record can be retrieved by looping the result set. The `stream` 
returned by the select operation holds a pointer to the actual data in the database and it loads data from the table 
only when it is accessed. This stream can be iterated only once. 

```ballerina
// Define a type to represent the results.
type Student record {
    int id;
    int age;
    string name;
};

// Select the data from the database table. Query Parameters are passed directly. 
// Similar to `execute` examples, parameters can be passed as sub types of 
// `sql:TypedValue`s as well.
int id = 10;
int age = 12;
stream<Student, sql:Error> resultStream = dbClient->query(`SELECT * FROM students 
                                                          WHERE id < ${id} AND 
                                                          age > ${age}`, Student);

// Iterating returned table.
error? e = resultStream.forEach(function(Student student) {
   io:println("Student Id: ", student.id);
   io:println("Student age: ", student.age);
   io:println("Student name: ", student.name);
});
if (e is error) {
   io:println("Query execution failed.", e);
}
```

Defining the return type is optional, and you can query the database without providing the result type. And hence 
the above example can be modified as below with open record type as return type. The property name in the open record 
type will be same as how the column is defined in the database. 

```ballerina
// Select the data from the database table. Query Parameters are passed directly. 
// Similar to `execute` examples, parameters can be passed as sub types of 
// `sql:TypedValue`s as well.
int id = 10;
int age = 12;
stream<record{}, sql:Error> resultStream = dbClient->query(`SELECT * FROM students 
                                                            WHERE id < ${id} AND 
                                                            age > ${age}`, Student);

// Iterating returned table.
error? e = resultStream.forEach(function(record{} student) {
   io:println("Student Id: ", student["id"]);
   io:println("Student age: ", student["age"]);
   io:println("Student name: ", student["name"];
});
if (e is error) {
   io:println("Query execution failed.", e);
}
```

There are situations where you may do not want to iterate through the database, and in that case you may decide
to only use the `next()` operation in the result `stream` and retrieve the first record. In such cases, the returned
result stream will not be closed, and you have to explicitly invoke `close` operation on the 
`sql:Client` to release the connection resources and avoid connection leak as shown below.

```ballerina
stream<record{}, sql:Error> resultStream = dbClient->query("SELECT count(*) as " + 
                                                           "total FROM students");

record {|record {} value;|}|error? result = resultStream.next();

if (result is record {|record {} value;|}) {
    io:println("Total students : ", result.value["total"]);
} else if (result is error) {
    io:println("Error encoutered when executing query. ", result);
} else {
    io:println("Student table is empty");
}

error? e = resultStream.close();
if(e is error){
    io:println("Error when closing the stream", e);
}
```

#### Updating data

This example demonstrates modifying data by executing an UPDATE statement via the `execute` remote function of 
the client.

```ballerina
int age = 23;
var ret = dbClient->execute(`UPDATE students SET name = 'John' WHERE age = ${age}`);
if (ret is sql:ExecutionResult) {
    io:println("Updated row count in Students table: ", ret.affectedRowCount);
} else {
    error err = ret;
    io:println("Update to students table failed: ",
                <string>err.detail()["message"]);
}
```

#### Deleting data

This example demonstrates deleting data by executing an DELETE statement via the `execute` remote function of 
the client.

```ballerina
string name = "John";
var ret = dbClient->execute(`DELETE from students WHERE name = ${name}`);
if (ret is sql:ExecutionResult) {
    io:println("Deleted student count: ", ret.affectedRowCount);
} else {
    error err = ret;
    io:println("Delete from students table failed: ",
                <string>err.detail()["message"]);
}
```

#### Batch updating data

This example demonstrates how to insert multiple records with a single INSERT statement that is executed via the 
`batchExecute` remote function of the client. This is done by creating a `table` with multiple records and 
parameterizing the SQL query as same as `execute` operations above.  

```ballerina
// Create the table with records that needs to be inserted.
var data = [
  { name: "John", age: 25  },
  { name: "Peter", price: 24 },
  { name: "jane", price: 22 }
];

// Do the batch update by passing the batches.
var ret = dbClient->batchExecute(from {name, age} in data 
                                 select `INSERT INTO students 
                                 ('name', 'age') 
                                 VALUES (${name}, ${age})`);

if (ret is error) {
    io:println("Error occurred:", <string>e.detail()["message"]);
} else {
    io:println("Batch item 1 update count: ", ret[0].affectedRowCount);
    io:println("Batch item 2 update count: ", ret[1].affectedRowCount);
}
```

>**Note:** The default thread pool size used in Ballerina is the number of processors available * 2. You can configure
the thread pool size by using the `BALLERINA_MAX_POOL_SIZE` environment variable.