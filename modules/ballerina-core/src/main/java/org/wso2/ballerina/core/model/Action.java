/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.types.BType;

/**
 * {@code {@link Action}} represents any Ballerina Action.
 */
public interface Action {

    /**
     * Get Name of the Action.
     *
     * @return name of the function.
     */
    String getName();

    /**
     * Get all the Annotations associated with a BallerinaAction
     *
     * @return list of Annotations
     */
    Annotation[] getAnnotations();

    /**
     * Get list of Arguments associated with the action definition
     *
     * @return list of Arguments
     */
    Parameter[] getParameters();

    VariableDcl[] getVariableDcls();

    /**
     * Get list of return Types associated with action definition.
     *
     * @return list of Return types.
     */
    BType[] getReturnTypes();

    int getStackFrameSize();

    void setStackFrameSize(int stackFrameSize);

    /**
     * Get the action Identifier
     *
     * @return action identifier
     */
    SymbolName getSymbolName();

    /**
     * Set the action identifier
     *
     * @param symbolName identifier
     */
    void setSymbolName(SymbolName symbolName);
}
