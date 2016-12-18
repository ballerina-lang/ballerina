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

package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.Executable;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code worker} is a thread of execution that the integration developer programs as a lifeline.
 * <p>
 *
 * Workers are defined as follows:
 *
 *  worker WorkerName (message m) {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      Statement;+
 *      [reply MessageName;]
 *  }
 *
 *  @since 1.0.0
 */
@SuppressWarnings("unused")
public class Worker implements Executable, Node {

    private List<ConnectorDcl> connectorDcls;
    private List<VariableDcl> variables;
    private List<Statement> statements;

    public Worker(List<VariableDcl> variables, List<Statement> statements) {
        this.variables = variables;
        this.statements = statements;
    }

    public Worker(){}

    /**
     * Get all Connections declared within the Worker
     *
     * @return list of all the Connections belongs to the Worker
     */
    public List<ConnectorDcl> getConnectorDcls() {
        return connectorDcls;
    }

    /**
     * Assign connections to the Worker
     *
     * @param connectorDcls list of connections to be assigned to the Worker
     */
    public void setConnectorDcls(List<ConnectorDcl> connectorDcls) {
        this.connectorDcls = connectorDcls;
    }

    /**
     * Add a {@code Connection} to the Worker
     *
     * @param connectorDcl Connection to be added to the Worker
     */
    public void addConnection(ConnectorDcl connectorDcl) {
        if (connectorDcls == null) {
            connectorDcls = new ArrayList<ConnectorDcl>();
        }
        connectorDcls.add(connectorDcl);
    }

    /**
     * Get all the variables declared in the Worker
     *
     * @return list of all Worker scoped variables
     */
    public List<VariableDcl> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Worker
     *
     * @param variables list of variables
     */
    public void setVariables(List<VariableDcl> variables) {
        this.variables = variables;
    }

    /**
     * Add a {@code Variable} to the Worker
     *
     * @param variable variable to be added the Worker
     */
    public void addVariable(VariableDcl variable) {
        if (variables == null) {
            variables = new ArrayList<VariableDcl>();
        }
        variables.add(variable);
    }

    /**
     * Get all the Statements associated with the Worker
     *
     * @return list of Statements associated with the Worker
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Set Statements to be associated with the Worker
     *
     * @param statements list of Statements
     */
    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * Add a {@code Statement} to the Worker
     *
     * @param statement a Statement to be added to the Worker
     */
    public void addStatement(Statement statement) {
        if (statements == null) {
            statements = new ArrayList<Statement>();
        }
        statements.add(statement);
    }

    public boolean execute(Context context, BalCallback callback) {

        context.setBalCallback(callback);

        BLangInterpreter interpreter = new BLangInterpreter(context);
        this.accept(interpreter);

        return true;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
