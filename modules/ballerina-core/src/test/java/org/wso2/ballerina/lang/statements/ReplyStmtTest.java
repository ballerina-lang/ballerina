/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.lang.statements;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;

public class ReplyStmtTest {
    private SymScope globalSymScope;

    @BeforeClass
    public void setup() {
        BuiltInNativeConstructLoader.loadConstructs();
        globalSymScope = GlobalScopeHolder.getInstance().getScope();
    }

    /* Negative Tests */
    
    @Test(description = "Test reply statement in a function",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "reply-from-function.bal:1: reply statement cannot be used in a " +
            "function definition")
    public void testReplyFromFunction() {
        ParserUtils.parseBalFile("lang/statements/replyStmt/reply-from-function.bal", globalSymScope);
    }
    
    @Test(description = "Test reply statement in a action",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "reply-from-action.bal:5: reply statement cannot be used in a action " +
            "definition")
    public void testReplyFromAction() {
        ParserUtils.parseBalFile("lang/statements/replyStmt/reply-from-action.bal", globalSymScope);
    }
}
