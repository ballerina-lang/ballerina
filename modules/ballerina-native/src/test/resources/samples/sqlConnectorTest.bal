import ballerina.lang.datatables;
import ballerina.data.sql;

function testInsertTableData() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    int insertCount = sql:ClientConnector.update(testDB, "Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 2, 5000.75, 'USA')",
            parameters);
    return insertCount;
}

function testCreateTable() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    int returnValue = sql:ClientConnector.update(testDB, "CREATE TABLE IF NOT EXISTS Students(studentID int,
                LastName varchar(255))", parameters);
    return returnValue;
}

function testUpdateTableData() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    int updateCount = sql:ClientConnector.update(testDB, "Update Customers set country = 'UK' where registrationID = 1",
            parameters);
    return updateCount;
}

function testGeneratedKeyOnInsert() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount;
    string[] generatedID;
    sql:Parameter[] parameters = [];

    insertCount,generatedID = sql:ClientConnector.updateWithGeneratedKeys(testDB,
        "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
        values ('Mary', 'Williams', 3, 5000.75, 'USA')", parameters);
    return generatedID[0];
}

function testGeneratedKeyWithColumn() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount;
    string[] generatedID;
    string[] keyColumns;
    keyColumns = ["CUSTOMERID"];
    sql:Parameter[] parameters = [];

    insertCount,generatedID = sql:ClientConnector.updateWithGeneratedKeys(testDB,
        "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
        values ('Kathy', 'Williams', 4, 5000.75, 'USA')", parameters, keyColumns);
    return generatedID[0];
}

function testSelectData() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testCallProcedure() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];
    sql:ClientConnector.call(testDB, "{call InsertPersonData(100,'James')}", parameters);

    string firstName;
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 100",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testCallProcedureWithResultSet() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.call(testDB, "{call SelectPersonData()}", parameters);

    string firstName;
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testConnectorWithDataSource() (string) {
    map propertiesMap = {"dataSourceClassName"  :"org.hsqldb.jdbc.JDBCDataSource",
        "dataSource.user":"SA", "dataSource.password":"", "dataSource.loginTimeout":0,
        "dataSource.url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testConnectionPoolProperties() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "driverClassName":"org.hsqldb.jdbcDriver", "username":"SA", "password":"", "maximumPoolSize":1,
        "idleTimeout":600000, "connectionTimeout":30000, "autoCommit":"true", "maxLifetime":1800000,
        "minimumIdle":1, "poolName":"testHSQLPool", "initializationFailTimeout":1,
        "isolateInternalQueries":"false", "allowPoolSuspension":"false", "readOnly":"false",
        "registerMbeans":"false", "validationTimeout":5000, "leakDetectionThreshold":0,
        "connectionInitSql":"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
        "transactionIsolation":"2", "catalog":"PUBLIC",
        "connectionTestQuery":"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testQueryParameters() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter para1 = {sqlType:"integer", value:"1", direction:0};
    sql:Parameter[] parameters = [para1];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = ?",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testInsertTableDataWithParameters() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
	sql:Parameter para1 = {sqlType:"varchar", value:"Anne", direction:0};
	sql:Parameter para2 = {sqlType:"varchar", value:"James", direction:0};
	sql:Parameter para3 = {sqlType:"integer", value:"3", direction:0};
	sql:Parameter para4 = {sqlType:"double", value:"5000.75", direction:0};
	sql:Parameter para5 = {sqlType:"varchar", value:"UK", direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

	int insertCount = sql:ClientConnector.update(testDB,"Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters);
    return insertCount;
}

function testOutParameters()
    (string, string, string, string, string, string, string, string, string, string, string, string, string, string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:"1", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:1};
    sql:Parameter paraLong = {sqlType:"bigint", direction:1};
    sql:Parameter paraFloat = {sqlType:"float", direction:1};
    sql:Parameter paraDouble = {sqlType:"double", direction:1};
    sql:Parameter paraBool = {sqlType:"boolean", direction:1};
    sql:Parameter paraString = {sqlType:"varchar", direction:1};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:1};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:1};
    sql:Parameter paraReal = {sqlType:"real", direction:1};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:1};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:1};
    sql:Parameter paraClob = {sqlType:"clob", direction:1};
    sql:Parameter paraBlob = {sqlType:"blob", direction:1};
    sql:Parameter paraBinary = {sqlType:"binary", direction:1};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call(testDB, "{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
        paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
        paraBlob.value, paraBinary.value;
}

function testNullOutParameters()
    (string, string, string, string, string, string, string ,string ,string ,string ,string,string,string,string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:"2", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:1};
    sql:Parameter paraLong = {sqlType:"bigint", direction:1};
    sql:Parameter paraFloat = {sqlType:"float", direction:1};
    sql:Parameter paraDouble = {sqlType:"double", direction:1};
    sql:Parameter paraBool = {sqlType:"boolean", direction:1};
    sql:Parameter paraString = {sqlType:"varchar", direction:1};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:1};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:1};
    sql:Parameter paraReal = {sqlType:"real", direction:1};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:1};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:1};
    sql:Parameter paraClob = {sqlType:"clob", direction:1};
    sql:Parameter paraBlob = {sqlType:"blob", direction:1};
    sql:Parameter paraBinary = {sqlType:"binary", direction:1};

    sql:Parameter[] parameters=[paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call(testDB, "{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
        paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
        paraBlob.value, paraBinary.value;
}

function testINParameters() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

	sql:Parameter paraID = {sqlType:"integer", value:"3", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", value:"1", direction:0};
    sql:Parameter paraLong = {sqlType:"bigint", value:"9223372036854774807", direction:0};
    sql:Parameter paraFloat = {sqlType:"float", value:"123.34", direction:0};
    sql:Parameter paraDouble = {sqlType:"double", value:"2139095039", direction:0};
    sql:Parameter paraBool = {sqlType:"boolean", value:"TRUE", direction:0};
    sql:Parameter paraString = {sqlType:"varchar", value:"Hello", direction:0};
    sql:Parameter paraNumeric = {sqlType:"numeric", value:"1234.567", direction:0};
    sql:Parameter paraDecimal = {sqlType:"decimal", value:"1234.567", direction:0};
    sql:Parameter paraReal = {sqlType:"real", value:"1234.567", direction:0};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", value:"1", direction:0};
    sql:Parameter paraSmallInt = {sqlType:"smallint", value:"5555", direction:0};
    sql:Parameter paraClob = {sqlType:"clob", value:"very long text", direction:0};
    sql:Parameter paraBlob = {sqlType:"blob", value:"YmxvYiBkYXRh", direction:0};
    sql:Parameter paraBinary = {sqlType:"binary", value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:0};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
            paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    int insertCount = sql:ClientConnector.update(testDB, "INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    return insertCount;
}

function testNullINParameters() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

	sql:Parameter paraID = {sqlType:"integer", value:"4", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:0};
    sql:Parameter paraLong = {sqlType:"bigint", direction:0};
    sql:Parameter paraFloat = {sqlType:"float", direction:0};
    sql:Parameter paraDouble = {sqlType:"double", direction:0};
    sql:Parameter paraBool = {sqlType:"boolean", direction:0};
    sql:Parameter paraString = {sqlType:"varchar", direction:0};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:0};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:0};
    sql:Parameter paraReal = {sqlType:"real", direction:0};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:0};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:0};
    sql:Parameter paraClob = {sqlType:"clob", direction:0};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
            paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob];
    int insertCount = sql:ClientConnector.update(testDB, "INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    return insertCount;
}

function testINOutParameters()
    (string, string, string, string, string, string, string, string, string, string, string, string, string, string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:"5", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", value:"10", direction:2};
    sql:Parameter paraLong = {sqlType:"bigint", value:"9223372036854774807", direction:2};
    sql:Parameter paraFloat = {sqlType:"float", value:"123.34", direction:2};
    sql:Parameter paraDouble = {sqlType:"double", value:"2139095039", direction:2};
    sql:Parameter paraBool = {sqlType:"boolean", value:"true", direction:2};
    sql:Parameter paraString = {sqlType:"varchar", value:"Hello", direction:2};
    sql:Parameter paraNumeric = {sqlType:"numeric", value:"1234.567", direction:2};
    sql:Parameter paraDecimal = {sqlType:"decimal", value:"1234.567", direction:2};
    sql:Parameter paraReal = {sqlType:"real", value:"1234.567", direction:2};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", value:"1", direction:2};
    sql:Parameter paraSmallInt = {sqlType:"smallint", value:"5555", direction:2};
    sql:Parameter paraClob = {sqlType:"clob", value:"very long text", direction:2};
    sql:Parameter paraBlob = {sqlType:"blob", value:"YmxvYiBkYXRh", direction:2};
    sql:Parameter paraBinary = {sqlType:"binary", value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:2};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call(testDB, "{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
        paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
        paraBlob.value, paraBinary.value;
}

function testNullINOutParameters()
    (string, string, string, string, string, string, string, string, string, string, string, string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:"6", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:2};
    sql:Parameter paraLong = {sqlType:"bigint", direction:2};
    sql:Parameter paraFloat = {sqlType:"float", direction:2};
    sql:Parameter paraDouble = {sqlType:"double", direction:2};
    sql:Parameter paraBool = {sqlType:"boolean", direction:2};
    sql:Parameter paraString = {sqlType:"varchar", direction:2};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:2};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:2};
    sql:Parameter paraReal = {sqlType:"real", direction:2};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:2};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:2};
    sql:Parameter paraClob = {sqlType:"clob", direction:2};
    sql:Parameter paraBlob = {sqlType:"blob", direction:2};
    sql:Parameter paraBinary = {sqlType:"binary", direction:2};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call(testDB, "{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
        paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value;
}

function testEmptySQLType() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter para1 = {value:"Anne", direction:0};
    sql:Parameter[] parameters = [para1];
    int insertCount = sql:ClientConnector.update(testDB, "Insert into Customers (firstName) values (?)", parameters);
    return insertCount;
}
