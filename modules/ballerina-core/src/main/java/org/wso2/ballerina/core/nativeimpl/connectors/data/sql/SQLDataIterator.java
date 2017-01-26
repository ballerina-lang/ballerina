/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.core.nativeimpl.connectors.data.sql;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.DataIterator;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;

import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

/**
 * This iterator mainly wrap java.sql.ResultSet.
 */
public class SQLDataIterator implements DataIterator {

    private Connection conn;
    private Statement stmt;
    private ResultSet rs;

    public SQLDataIterator(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
    }

    @Override
    public void close() {
        cleanupConnection(rs, stmt, conn);
    }

    @Override
    public boolean next() {
        try {
            return rs.next();
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform next: " + e.getMessage(), e);
        }
    }

    public String getString(int index) {
        try {
            return rs.getString(index);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getString: " + e.getMessage(), e);
        }
    }

    @Override
    public String getString(String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getString: " + e.getMessage(), e);
        }
    }

    public long getLong(int index) {
        try {
            return rs.getLong(index);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getLong: " + e.getMessage(), e);
        }
    }

    @Override
    public long getLong(String columnName) {
        try {
            return rs.getLong(columnName);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getLong: " + e.getMessage(), e);
        }
    }

    public int getInt(int index) {
        try {
            return rs.getInt(index);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getInt: " + e.getMessage(), e);
        }
    }

    @Override
    public int getInt(String columnName) {
        try {
            return rs.getInt(columnName);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getInt: " + e.getMessage(), e);
        }
    }

    public float getFloat(int index) {
        try {
            return rs.getFloat(index);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getFloat: " + e.getMessage(), e);
        }
    }

    @Override
    public float getFloat(String columnName) {
        try {
            return rs.getFloat(columnName);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getFloat: " + e.getMessage(), e);
        }
    }

    public double getDouble(int index) {
        try {
            return rs.getDouble(index);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getDouble: " + e.getMessage(), e);
        }
    }

    @Override
    public double getDouble(String columnName) {
        try {
            return rs.getDouble(columnName);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getDouble: " + e.getMessage(), e);
        }
    }

    public boolean getBoolean(int index) {
        try {
            return rs.getBoolean(index);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getBoolean: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean getBoolean(String columnName) {
        try {
            return rs.getBoolean(columnName);
        } catch (SQLException e) {
            throw new BallerinaException("Unable to perform getBoolean: " + e.getMessage(), e);
        }
    }

    // Below method doesn't support streaming.
    @Override
    public BValue get(int columnIndex, String type) {
        try {
            // Directly allocating full length array for decode byte array since anyway we are building
            // new String in memory.
            switch (type) {
            case "blob":
                Blob blob = rs.getBlob(columnIndex);
                byte[] decode = getBase64Decode(
                        new String(blob.getBytes(1L, (int) blob.length()), Charset.defaultCharset()));
                return new BString(new String(decode, Charset.defaultCharset()));
            case "clob":
                Clob clob = rs.getClob(columnIndex);
                decode = getBase64Decode(clob.getSubString(0, (int) clob.length()));
                return new BString(new String(decode, Charset.defaultCharset()));
            case "nclob":
                NClob nClob = rs.getNClob(columnIndex);
                decode = getBase64Decode(nClob.getSubString(0, (int) nClob.length()));
                return new BString(new String(decode, Charset.defaultCharset()));
            case "date":
                return new BLong(rs.getDate(columnIndex).getTime());
            case "timestamp":
                return new BLong(rs.getTimestamp(columnIndex).getTime());
            }
        } catch (SQLException e) {
            throw new BallerinaException("Unable to get given column value as " + type + ": " + e.getMessage(), e);
        }
        return null;
    }

    private byte[] getBase64Decode(String st) {
        return Base64.getDecoder().decode(st.getBytes(Charset.defaultCharset()));
    }

    // Below method doesn't support streaming.
    @Override
    public BValue get(String columnName, String type) {
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        cleanupConnection(rs, stmt, conn);
        super.finalize();
    }

    private void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        this.rs = null;
        this.stmt = null;
        this.conn = null;
    }
}
