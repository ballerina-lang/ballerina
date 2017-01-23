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

/**
 * Test class for return statement negative cases.
 */
public class ReturnStmtNegativeTest {
    private SymScope globalSymScope;

    @BeforeClass
    public void setup() {
        BuiltInNativeConstructLoader.loadConstructs();
        globalSymScope = GlobalScopeHolder.getInstance().getScope();
    }

    @Test(description = "Test return statement in resource",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "return-in-resource.bal:14: return statement cannot be used " +
                    "in a resource definition")
    public void testReturnInResource() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/return-in-resource.bal", globalSymScope);
    }

    @Test(description = "Test not enough arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "not-enough-args-to-return-1.bal:2: not enough arguments to return")
    public void testNotEnoughArgsToReturn1() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/not-enough-args-to-return-1.bal", globalSymScope);
    }

    @Test(description = "Test not enough arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "not-enough-args-to-return-2.bal:2: not enough arguments to return")
    public void testNotEnoughArgsToReturn2() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/not-enough-args-to-return-2.bal", globalSymScope);
    }

    @Test(description = "Test not enough arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "not-enough-args-to-return-3.bal:2: not enough arguments to return")
    public void testNotEnoughArgsToReturn3() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/not-enough-args-to-return-3.bal", globalSymScope);
    }

    @Test(description = "Test too many arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "too-many-args-to-return-1.bal:2: too many arguments to return")
    public void testTooManyArgsToReturn1() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/too-many-args-to-return-1.bal", globalSymScope);
    }

    @Test(description = "Test too many arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "too-many-args-to-return-2.bal:2: too many arguments to return")
    public void testTooManyArgsToReturn2() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/too-many-args-to-return-2.bal", globalSymScope);
    }

    @Test(description = "Test type mismatch",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "return-type-mismatch-1.bal:2: cannot use string as type " +
                    "int in return statement")
    public void testInputTypeMismatch1() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/return-type-mismatch-1.bal", globalSymScope);
    }

    @Test(description = "Test type mismatch",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "return-type-mismatch-2.bal:2: cannot use int as type " +
                    "string in return statement")
    public void testInputTypeMismatch2() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/return-type-mismatch-2.bal", globalSymScope);
    }

//    @Test(description = "Test type mismatch",
//            expectedExceptions = {SemanticException.class},
//            expectedExceptionsMessageRegExp = "multi-value-in-single-context.bal:2: multiple-value " +
//                    "split() in single-value context")
//    public void testMultiValueInSingleContext() {
//        ParserUtils.parseBalFile("lang/statements/returnstmt/multi-value-in-single-context.bal",
//                globalSymScope);
//    }
}
