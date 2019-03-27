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

import ballerina/sql;

# Represents personally identifiable information (PII) stored in a database
#
# + pii - personally identifiable information
type PiiData record {|
    string pii;
|};

# Build insert query based on the table name and column names
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + piiColumn - column name used to store PII
# + return - insert query
function buildInsertQuery (string tableName, string idColumn, string piiColumn) returns string {
    return string `INSERT INTO ${tableName} (${idColumn}, ${piiColumn}) VALUES (?, ?)`;
}

# Build select query based on the table name and column names
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + piiColumn - column name used to store PII
# + return - select query
function buildSelectQuery (string tableName, string idColumn, string piiColumn) returns string {
    return string `SELECT ${piiColumn} FROM ${tableName} WHERE ${idColumn} = ?`;
}

# Build delete query based on the table name and column names
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + return - delete query
function buildDeleteQuery (string tableName, string idColumn) returns string {
    return string `DELETE FROM ${tableName} WHERE ${idColumn} = ?`;
}

# Validate the table name and column names and throw errors if validation errors are present
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + piiColumn - column name used to store PII
function validateFieldName (string tableName, string idColumn, string piiColumn) {
    if (tableName == "") {
        error err = error("Table name is required");
        panic err;
    }
    if (idColumn == "") {
        error err = error("ID column name is required");
        panic err;
    }
    if (piiColumn == "") {
        error err = error("PII column name is required");
        panic err;
    }
}

# Process results of the insert query
#
# + id - pseudonymized identifier getting inserted
# + queryResult - results of the insert query
# + return - pseudonymized identifier if insert was successful, error if insert failed
function processInsertResult (string id, sql:UpdateResult|error queryResult) returns string|error {
    if (queryResult is error) {
        return queryResult;
    } else {
        if (queryResult.updatedRowCount > 0) {
            return id;
        } else {
            error err = error("Unable to insert PII with identifier " + id);
            return err;
        }
    }
}

# Process results of the select query
#
# + id - pseudonymized identifier getting selected
# + queryResult - results of the select query
# + return - personally identifiable information (PII) if select was successful, error if select failed
function processSelectResult (string id, table<PiiData>|error queryResult) returns string|error {
    if (queryResult is error) {
        return queryResult;
    } else {
        if (queryResult.hasNext()) {
            PiiData piiData = check trap <PiiData>queryResult.getNext();
            queryResult.close();
            return piiData.pii;
        } else {
            error err = error("Identifier " + id + " is not found in PII store");
            return err;
        }
    }
}

# Process results of the delete query
#
# + id - pseudonymized identifier getting deleted
# + queryResult - results of the delete query
# + return - nil if deletion was successful, error if deletion failed
function processDeleteResult (string id, sql:UpdateResult|error queryResult) returns error? {
    if (queryResult is error) {
        return queryResult;
    } else {
        if (queryResult.updatedRowCount > 0) {
            return ();
        } else {
            error err = error("Identifier " + id + " is not found in PII store");
            return err;
        }
    }
}
