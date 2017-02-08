/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Util class for SQL DB Tests.
 */
public class SQLDBUtils {

    public static final String DB_DIRECTORY = "./target/tempdb/";

    /**
     * Crete HyperSQL DB with the given name and initialize with given SQL file.
     *
     * @param dbName  Name of the DB instance.
     * @param sqlFile SQL statements for initialization.
     */
    public static void initDatabase(String dbDirectory, String dbName, String sqlFile) {
        Connection connection = null;
        Statement st = null;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            String jdbcURL = "jdbc:hsqldb:file:" + dbDirectory + dbName;
            connection = DriverManager.getConnection(jdbcURL, "SA", "");
            String sql = XMLUtils.readFileToString(sqlFile);
            String[] sqlQuery = sql.trim().split("/");
            st = connection.createStatement();
            for (String query : sqlQuery) {
                st.executeUpdate(query.trim());
            }
        } catch (ClassNotFoundException | SQLException e) {
            //Do nothing
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                //Do nothing
            }
        }
    }

    /**
     * Delete the given directory along with all files and sub directories.
     *
     * @param directory Directory to delete.
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                boolean success = deleteDirectory(f);
                if (!success) {
                    return false;
                }
            }
        }
        return directory.delete();
    }

    /**
     * Delete all the files and sub directories which matches given prefix in a given directory.
     *
     * @param directory Directory which contains files to delete.
     * @param prefix    Prefix for matching files to delete.
     */
    public static void deleteFiles(File directory, String prefix) {
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                if (f.getName().startsWith(prefix)) {
                    deleteDirectory(f);
                }
            }
        }
    }
}
