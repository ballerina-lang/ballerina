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

package org.wso2.ballerina.core.nativeimpl.connectors;

import org.h2.tools.DeleteDbFiles;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.message.StringDataSource;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.MessageUtils;
import org.wso2.ballerina.core.utils.XMLUtils;
import org.wso2.ballerina.lang.util.Services;
import org.wso2.carbon.messaging.CarbonMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test Class for SQL Connector.
 */
public class SQLConnectorTest {

    @BeforeClass()
    public void setup() {
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.message:setStringPayload_message_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        EnvironmentInitializer.initialize("lang/connectors/sqlconnector.bal");
        DeleteDbFiles.execute("./target/", "TEST_SQL_CONNECTOR", true);
        initDatabase();
    }

    //Update Action Tests
    @Test(description = "Test Create Table")
    public void testActionCreateTable() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/actionCreateTable", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "0");
    }

    @Test(description = "Test Insert Data")
    public void testActionInsertData() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/actionInsertData", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "1");
    }

    @Test(description = "Test Update Data")
    public void testActionUpdateData() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/actionUpdateData", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "1");
    }

    @Test(description = "Test Insert Data with Generated Keys")
    public void testActionInsertDataWithKeys() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/actionDataInsertWithKeys", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        int generatedKey = Integer.parseInt(stringDataSource.getValue());

        Assert.assertTrue(generatedKey > 0);
    }

    @Test(description = "Test Insert Data with Generated Keys and Key Columns")
    public void testInsertWithKeyColumns() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/InsertWithKeyColumns", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        int generatedKey = Integer.parseInt(stringDataSource.getValue());

        Assert.assertTrue(generatedKey > 0);
    }

    @Test(description = "Test Select Data")
    public void testActionSelectData() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/actionSelectData", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "Peter");
    }

    @Test(description = "Test Connector With Data Source")
    public void testConnectorWithDataSource() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/connectorWithDataSource", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);

        Assert.assertEquals(stringDataSource.getValue(), "Peter");
    }

    private void initDatabase() {
        Connection connection = null;
        Statement st = null;
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:./target/TEST_SQL_CONNECTOR", "root", "root");
            String sql = XMLUtils.readFileToString("datafiles/SQLConnetorDataFile.sql");
            String[] sqlQuery = sql.split(";");
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

    @AfterSuite
    public void cleanup() {
        DeleteDbFiles.execute("./target/", "TEST_SQL_CONNECTOR", true);
    }
}
