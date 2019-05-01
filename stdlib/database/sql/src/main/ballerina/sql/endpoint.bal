// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Represents the base SQL Client
public type Client client object {
    *AbstractSQLClient;

    # The call remote function implementation for SQL connector to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call. The number of parameters is variable
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `error` will be returned if there is any error
    public remote function call(@sensitive string sqlQuery, typedesc[]? recordType, Param... parameters)
        returns @tainted table<record {}>[]|()|error {
        return nativeCall(self, sqlQuery, recordType, ...parameters);
    }

    # The select remote function implementation for SQL Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + loadToMemory - Indicates whether to load the retrieved data to memory or not
    # + parameters - The parameters to be passed to the select query. The number of parameters is variable
    # + return - A `table` returned by the sql query statement else `error` will be returned if there is any error
    public remote function select(@sensitive string sqlQuery, typedesc? recordType, boolean loadToMemory = false,
    Param... parameters) returns @tainted table<record {}>|error {
        return nativeSelect(self, sqlQuery, recordType, loadToMemory = loadToMemory, ...parameters);
    }

    # The update remote function implementation for SQL Client to update data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + keyColumns - Names of auto generated columns for which the auto generated key values are returned
    # + parameters - The parameters to be passed to the update query. The number of parameters is variable
    # + return - A `sql:UpdateResult` with the updated row count and key column values,
    #            else `error` will be returned if there is any error
    public remote function update(@sensitive string sqlQuery, string[]? keyColumns = (), Param... parameters)
                               returns UpdateResult|error {
        return nativeUpdate(self, sqlQuery, keyColumns = keyColumns, ...parameters);
    }

    # The batchUpdate remote function implementation for SQL Client to batch data insert.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - Variable number of parameter arrays each representing the set of parameters of belonging to each
    #                individual update
    # + return - An `int[]` - The elements in the array returned by the operation may be one of the following  or else
    #            an`error` will be returned if there is any error.
    #            A number greater than or equal to zero - indicates that the command was processed successfully
    #                                                     and is an update count giving the number of rows
    #            A value of -2 - Indicates that the command was processed successfully but that the number of rows affected
    #                            is unknown
    #            A value of -3 - Indicates that the command failed to execute successfully and occurs only if a driver
    #                            continues to process commands after a command fails
    public remote function batchUpdate(@sensitive string sqlQuery, Param?[]... parameters) returns int[]|error {
        return nativeBatchUpdate(self, sqlQuery, ...parameters);
    }
};

function nativeSelect(Client sqlClient, @sensitive string sqlQuery, typedesc? recordType,
   boolean loadToMemory = false, Param... parameters) returns @tainted table<record {}>|error = external;

function nativeCall(Client sqlClient, @sensitive string sqlQuery, typedesc[]? recordType, Param... parameters)
   returns @tainted table<record {}>[]|()|error = external;

function nativeUpdate(Client sqlClient, @sensitive string sqlQuery, string[]? keyColumns = (),
                             Param... parameters) returns UpdateResult|error = external;

function nativeBatchUpdate(Client sqlClient, @sensitive string sqlQuery, Param?[]... parameters)
    returns int[]|error = external;

# An internal function used by clients to shutdown the connection pool.
#
# + sqlClient - The Client object which represents the connection pool.
# + return - Possible error during closing
public function close(Client sqlClient) returns error? = external;

