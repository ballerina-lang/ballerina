/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.util.LangModelUtils;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * This class contain util methods to add connectors and actions to a given symbol scope
 *
 * @since 1.0.0
 */
public class Connectors {

    private Connectors() {
    }

    /**
     * Add Native Connector instance to given SymScope.
     *
     * @param symScope  SymScope instance.
     * @param connector Connector instance.
     */
    public static void addNativeConnector(SymScope symScope, AbstractNativeConnector connector) {
        SymbolName symbolName = LangModelUtils.getConnectorSymName(
                connector.getClass().getAnnotation(BallerinaConnector.class).connectorName(),
                connector.getPackageName());
        Symbol symbol = new Symbol(connector);
        symScope.insert(symbolName, symbol);
    }

    /**
     * Add action instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param action   Action instance.
     */
    public static void addAction(SymScope symScope, AbstractNativeAction action) {
        BallerinaAction annotations = (BallerinaAction) action.getClass().getAnnotation(BallerinaAction.class);
        SymbolName symbolName = LangModelUtils.getActionSymName(annotations.actionName(), annotations.connectorName(),
                annotations.packageName(), LangModelUtils.getTypesOfParams(action.getParameters()));
        Symbol symbol = new Symbol(action);
        symScope.insert(symbolName, symbol);
    }
}
