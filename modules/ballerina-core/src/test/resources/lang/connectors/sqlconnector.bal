import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.datatable;
import ballerina.data.sql;

@BasePath ("/invoke")
service SQLConnectorTestService {

    @GET
    @Path ("/createTable")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        int returnValue;
        message response;
        json payload;

        returnValue = sql:Connector.update(testDB, "CREATE TABLE IF NOT EXISTS Students(studentID int,
            LastName varchar(255))");

        response = new message;
        message:setStringPayload(response,string:valueOf(returnValue));
        reply response;
        }

    @GET
    @Path ("/insertData")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        int insertCount;
        message response;
        json payload;

        insertCount = sql:Connector.update(testDB, "insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('John', 'Thomas', 2, 5000.75, 'USA')");

        response = new message;
        message:setStringPayload(response,string:valueOf(insertCount));
        reply response;
        }

    @GET
    @Path ("/rowUpdate")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        int updateCount;
        message response;
        json payload;

        updateCount = sql:Connector.update(testDB, "Update Customers set country = 'UK' where registrationID = 1");

        response = new message;
        message:setStringPayload(response,string:valueOf(updateCount));
        reply response;
        }

    @GET
    @Path ("/getGeneratedKeysByColumn")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        int insertCount;
        string generatedID;
        message response;
        json payload;

        insertCount,generatedID = sql:Connector.updateWithGeneratedKeys(testDB,
            "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
            values ('Mary', 'Williams', 3, 5000.75, 'USA')");

        response = new message;
        message:setStringPayload(response,string:valueOf(generatedID));
        reply response;
        }

    @GET
    @Path ("/generatedKeys")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        int insertCount;
        string generatedID;
        message response;
        json payload;
        string[] keyColumns;
        keyColumns[0] = "customerId";

        insertCount,generatedID = sql:Connector.updateWithGeneratedKeys(testDB,
            "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
            values ('Mary', 'Williams', 3, 5000.75, 'USA')",keyColumns);

        response = new message;
        message:setStringPayload(response,string:valueOf(generatedID));
        reply response;
        }
	
    @GET
    @Path ("/dataSelect")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        datatable df;
        message response;
        string firstName;

        df = sql:Connector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
        while (datatable:next(df)) {
            firstName = datatable:getString(df, 1);
        }

        response = new message;
        message:setStringPayload(response,firstName);
        reply response;
        }

    @GET
    @Path ("/connectorWithDataSource")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"dataSourceClassName"  :"org.h2.jdbcx.JdbcDataSource ",
            "dataSource.user":"root", "dataSource.password":"root", "dataSource.loginTimeout":0,
            "dataSource.url":"jdbc:h2:file:./target/TEST_SQL_CONNECTOR"});

        datatable df;
        message response;
        string firstName;

        df = sql:Connector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
        while (datatable:next(df)) {
            firstName = datatable:getString(df, 1);
        }

        response = new message;
        message:setStringPayload(response,firstName);
        reply response;
        }

    @GET
    @Path ("/poolPropTest")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
                    "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1,
                    "idleTimeout":600000,"connectionTimeout":30000,"autoCommit":"true","maxLifetime":1800000,
                    "minimumIdle":1,"poolName":"testH2Pool","initializationFailTimeout":1,
                    "isolateInternalQueries":"false","allowPoolSuspension":"false","readOnly":"false",
                    "registerMbeans":"false","validationTimeout":5000,"leakDetectionThreshold":0,
                    "connectionInitSql":"SELECT 1","connectionTestQuery":"SELECT 1"});

        datatable df;
        message response;
        string firstName;

        df = sql:Connector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
        while (datatable:next(df)) {
            firstName = datatable:getString(df, 1);
        }

        response = new message;
        message:setStringPayload(response,firstName);
        reply response;
        }

    @GET
    @Path ("/selectExceptionTest")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        sql:Connector.select(testDB, "SELECT  FirstName from Customers2 where registrationID = 1");
        }

    @GET
    @Path ("/UpdateExceptionTest")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        sql:Connector.update(testDB, "Update Customers2 set country = 'UK' where registrationID = 1");
        }

    @GET
    @Path ("/keyUpdateExceptionTest")
        resource passthrough (message m) {
        sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_SQL_CONNECTOR",
            "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize":1});

        sql:Connector.updateWithGeneratedKeys(testDB,
                    "insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                    values ('Mary', 'Williams', 3, 5000.75, 'USA')");
        }
}
