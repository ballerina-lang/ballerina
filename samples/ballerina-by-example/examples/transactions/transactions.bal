import ballerina.lang.system;
import ballerina.data.sql;

function main (string[] args) {
    sql:ConnectionProperties Properties = {maximumPoolSize:5};
    sql:ClientConnector testDB = create sql:ClientConnector(
      sql:MYSQL, "localhost", 3306, "db", "sa", "root", Properties);

    //Here is the transaction block. You can use a Try catch here since update action can throw
    //errors due to SQL errors, connection pool errors etc.
    transaction {

        //This is the first action participate in the transaction.
        sql:Parameter[] parameters = [];
        int count = testDB.update("Insert into Customers(id,name)
                          values (1, 'Anne')", parameters);

        //This is the second action participate in the transaction.
        count = testDB.update("Insert into Salary (id, salary)
                       values (1, 2500)", parameters);

        system:println("Inserted count:" + count);

        //Anytime the transaction can be forcefully aborted
        //using the abort keyword.
        if (count == 0) {
            abort;
        }
        //The end curly bracket marks the end of the transaction
        //and the transaction will be committed or rolled back at
        //this point.
    } aborted {
        //The aborted block will be executed if the transaction is
        //rolled back due to an exception, or an abort statement or a throw statement.
        system:println("Transaction aborted");
    } committed {
        //The committed block will be executed if the transaction
        //is successfully committed.
        system:println("Transaction committed");
    }

    //Close the connection pool.
    testDB.close();
}
