/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with iterable objects.
 *
 * @since 1.1.0
 */
public class ForeachIterableObjectTest {

    private CompileResult program;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-iterable-objects.bal");
        negativeResult = BCompileUtil.compile("test-src/statements/foreach/foreach-iterable-object-negative.bal");
    }

    @Test
    public void testIterableObject() {
        BValue[] returns = BRunUtil.invoke(program, "testIterableObject");

        BValueArray arr = (BValueArray) returns[0];
        Assert.assertEquals(arr.size(), 7);
        int i = 0;
        Assert.assertEquals(arr.getInt(i++), 12);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 56);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 78);
        Assert.assertEquals(arr.getInt(i++), 21);
        Assert.assertEquals(arr.getInt(i), 90);

    }

    @Test
    public void testNestedIterableObject() {
        BValue[] returns = BRunUtil.invoke(program, "testNestedIterableObject");

        BValueArray arr = (BValueArray) returns[0];
        Assert.assertEquals(arr.size(), 14);
        int i = 0;
        Assert.assertEquals(arr.getInt(i++), 12);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 56);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 78);
        Assert.assertEquals(arr.getInt(i++), 21);
        Assert.assertEquals(arr.getInt(i++), 90);
        Assert.assertEquals(arr.getInt(i++), 12);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 56);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 78);
        Assert.assertEquals(arr.getInt(i++), 21);
        Assert.assertEquals(arr.getInt(i), 90);

    }

    @Test
    public void testIterableObjectErrors() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'object { public isolated " +
                "function next () returns (record {| int value; |}?); }', found 'object { int[] integers; int " +
                "cursorIndex; public isolated function next () returns ((record {| int value; |}|CustomError)?); }'",
                124, 16);
        BAssertUtil.validateError(negativeResult, i++, "invalid iterable completion type '(int|CustomError)' " +
                        "in foreach statement: next method completion type cannot contain type 'CustomError'",
                239, 25);
        BAssertUtil.validateError(negativeResult, i++, "invalid iterable completion type '(int|error)' " +
                        "in foreach statement: next method completion type cannot contain type 'error'", 241, 25);
        BAssertUtil.validateError(negativeResult, i++, "invalid iterable completion type '(int|CustomError)'" +
                " in foreach statement: next method completion type cannot contain type 'CustomError'", 244, 25);
        BAssertUtil.validateError(negativeResult, i++, "mismatched function signatures: expected 'public function" +
                " iterator() returns object { public isolated function next () returns ((" +
                "record {| (any|error) value; |}|error)?); }', found 'public function iterator() returns " +
                "object { public function foo () returns (record {| int value; |}?); }'", 251, 5);
        BAssertUtil.validateError(negativeResult, i++, "no implementation found for the method 'iterator' of class" +
                " 'Iterable11'", 273, 1);
        BAssertUtil.validateError(negativeResult, i++, "mismatched function signatures: expected 'public function" +
                " iterator() returns object { public isolated function next () returns ((" +
                "record {| (any|error) value; |}|error)?); }', found 'public function iterator() returns " +
                "object { public isolated function next () returns (record {| int x; |}?); }'", 299, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }
}
