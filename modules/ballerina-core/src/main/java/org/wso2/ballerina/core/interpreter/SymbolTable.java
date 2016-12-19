/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code SymbolTable} represents a data structure which hold information about the program constructs
 * <p>
 * TODO Please note that, this is the initial version of the implementation. We need to improve this.
 * TODO Refactor this implementation
 *
 * @since 1.0.0
 */
public class SymbolTable {

    private Map<SymbolName, BValue> map;
    private Map<SymbolName, VariableRefExpr> variableRefExprMap;
    private SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        map = new HashMap<>();
        variableRefExprMap = new HashMap<>();
        this.parent = parent;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void put(SymbolName symbolName, BValue value) {
        map.put(symbolName, value);
    }

    public BValue get(SymbolName symbolName) {
        for (SymbolTable t = this; t != null; t = t.parent) {
            BValue value = t.map.get(symbolName);
            if (value != null) {
                return value;
            }
        }

        // TODO Implement proper error handling here.
        throw new BallerinaException("Value not found for identifier: " + symbolName.getName());
    }

    public SymbolName lookup(String identifier1) {
        SymbolName symbolName = new SymbolName(identifier1);
        for (SymbolTable t = this; t != null; t = t.parent) {
            BValue value = t.map.get(symbolName);
            if (value != null) {
                return symbolName;
            }
        }

        // TODO Implement proper error handling here.
        throw new BallerinaException("Value not found for identifier: " + symbolName.getName());
    }

    public void putVarRefExpr(SymbolName symbolName, VariableRefExpr variableRefExpr) {
        variableRefExprMap.put(symbolName, variableRefExpr);
    }

    public VariableRefExpr lookupVarRefExpr(SymbolName symbolName) {
        for (SymbolTable t = this; t != null; t = t.parent) {
            VariableRefExpr variableRefExpr = t.variableRefExprMap.get(symbolName);
            if (variableRefExpr != null) {
                return variableRefExpr;
            }
        }

        // TODO Implement proper error handling here.
        throw new BallerinaException("Variable reference '" + symbolName.getName() + "'  is not declared.");
    }
}
