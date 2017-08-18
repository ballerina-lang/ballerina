import ballerina.lang.system;
import ballerina.lang.datatables;
import ballerina.data.sql;
import ballerina.doc;

@doc:Description {value:"This is the Employee struct. The field names of this should match column names of the table. The field types should match with sql types."}
struct Employee {
    int id;
    string name;
    float salary;
    boolean status;
    string birthdate;
    string birthtime;
    string updated;
}

function main (string[] args) {
    //Create a SQL connector by providing the required database connection
    //pool properties.
    sql:ConnectionProperties properties = {maximumPoolSize:5};
    sql:ClientConnector empDB = create sql:ClientConnector(
      sql:MYSQL, "localhost", 3306, "db", "sa", "root", properties);
    //Query the table using SQL connector select action. Either select or call
    //action can return a datatable.
    sql:Parameter[] params = [];
    datatable dt = empDB.select("SELECT * from employees", params);

    //Iterate through the result until hasNext() become false and retrieve
    //the data struct corresponding to each row.
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        var rs, _ = (Employee)dataStruct;
        system:println("Employee:" + rs.id + "|" + rs.name +
                       "|" + rs.salary + "|" + rs.status +
                       "|" + rs.birthdate +
                       "|" + rs.birthtime +
                       "|" + rs.updated);
    }

    //Convert a datatable to json.
    dt = empDB.select("SELECT id,name from employees", params);
    var jsonRes, _ = <json>dt;
    system:println(jsonRes);

    //Convert a datatable to xml.
    dt = empDB.select("SELECT id,name from employees", params);
    var xmlRes, _ = <xml>dt;
    system:println(xmlRes);

    //Finally close the DB connection.
    empDB.close();
}
