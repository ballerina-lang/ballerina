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

package org.wso2.ballerina.core.nativeimpl.connectors;

import org.testng.annotations.BeforeClass;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnString;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Test class for Native Action test.
 */
public class NativeActionTest {

    private BallerinaFile bFile;
    private SymScope globalScope;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/parser/PassthroughService.bal");

        globalScope = new SymScope(SymScope.Name.GLOBAL);

        PrintlnString printlnFunc = new PrintlnString();

        printlnFunc.getParameters();


    }



    public void testNativeFuncInvocation() {

    }
}
