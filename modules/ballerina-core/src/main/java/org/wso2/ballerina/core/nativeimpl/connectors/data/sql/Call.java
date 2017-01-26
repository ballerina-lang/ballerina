/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.nativeimpl.connectors.data.sql;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;

/**
 * {@code Call} is the Call action implementation of the SQL Connector
 */
@BallerinaAction(
        packageName = "ballerina.data.sql",
        actionName = "call",
        connectorName = SQLConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                          type = TypeEnum.CONNECTOR),
                @Argument(name = "query",
                          type = TypeEnum.STRING)/*, //TODO:Add Parameter[]
                @Argument(name = "optionalProperties",
                          type = TypeEnum.MAP)*/
        },
        returnType = { TypeEnum.DATAFRAME })
@Component(
        name = "action.data.sql.call",
        immediate = true,
        service = AbstractNativeAction.class)
public class Call extends AbstractSQLAction {
    @Override
    public BValue execute(Context context) {
        BConnector bConnector = (BConnector) getArgument(context, 0);
        String query = getArgument(context, 1).stringValue();

        Connector connector = bConnector.value();
        if (!(connector instanceof SQLConnector)) {
            throw new BallerinaException("Need to use a SQL Connector as the first argument", context);
        }

        return executeProcedure(context, connector, query);
    }
}
