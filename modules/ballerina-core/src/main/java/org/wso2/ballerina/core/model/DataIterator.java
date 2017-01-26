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

package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.values.BValue;

/**
 *  This interface represents an data iterator operations.
 */
public interface DataIterator {

    boolean next();

    void close();

    String getString(int columnIndex);

    String getString(String columnName);

    long getLong(int columnIndex);

    long getLong(String columnName);

    int getInt(int columnIndex);

    int getInt(String columnName);

    float getFloat(int columnIndex);

    float getFloat(String columnName);

    double getDouble(int columnIndex);

    double getDouble(String columnName);

    boolean getBoolean(int columnIndex);

    boolean getBoolean(String columnName);

    BValue get(int columnIndex, String type);

    BValue get(String columnName, String type);
}
