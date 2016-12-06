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
package org.wso2.ballerina.interpreter;

import org.wso2.ballerina.model.Identifier;
import org.wso2.ballerina.model.values.BValue;

/**
 * {@code Context} represents the runtime state of a program.
 *
 * @since 1.0.0
 */
public class Context {

    private SymbolTable table;
    private ControlStack controlStack;

    public Context() {
        table = new SymbolTable(null);
        controlStack = new ControlStack();
    }

    public ControlStack getControlStack() {
        return controlStack;
    }

    public BValue lookup(Identifier id) {
        return table.get(id);
    }

    public void put(Identifier id, BValue value) {
        table.put(id, value);
    }

    public void createChild() {
        SymbolTable child = new SymbolTable(table);
        this.table = child;
    }

    public void switchToParent() {
        table = table.getParent();
    }
}
