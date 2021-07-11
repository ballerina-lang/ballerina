/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.record;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for user record definition negative test.
 */
public class RecordDefNegativeTest {

    private static final String INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION =
            "cannot use 'check' in the default expression of a record field";

    @Test(description = "Test usage of duplicate keys in record definition")
    public void duplicateKeyTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/duplicate-key-negative.bal");
        int errorIndex = 0;
        BAssertUtil.validateError(compileResult, errorIndex++, "invalid usage of record literal: duplicate key 'name'",
                26, 10);
        BAssertUtil.validateError(compileResult, errorIndex++, "invalid usage of record literal: duplicate key 'name'",
                35, 9);
        BAssertUtil.validateError(compileResult, errorIndex, "invalid usage of record literal: duplicate key 'name'",
                43, 10);
    }

    @Test(description = "Test duplicate fields")
    public void duplicateFieldTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/duplicate-field-negative.bal");
        int errorIndex = 0;
        BAssertUtil.validateError(compileResult, errorIndex++, "redeclared symbol 'error'", 19, 11);
        BAssertUtil.validateError(compileResult, errorIndex++, "redeclared symbol 'name'", 24, 12);
        assertEquals(compileResult.getErrorCount(), errorIndex);
    }

    @Test
    public void testFieldRefFromWithinARecordDef() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/field_ref_in_own_record.bal");
        int indx = 0;
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 19, 13);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 21, 17);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 26, 28);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'fname'", 33, 27);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'lname'", 33, 41);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 38, 17);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 41, 21);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 46, 32);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'x'", 52, 57);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 59, 21);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 61, 25);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 67, 33);
        BAssertUtil.validateError(compileResult, indx++, "undefined symbol 'a'", 69, 37);
        BAssertUtil.validateError(compileResult, indx++, "incompatible types: expected 'string', found 'int'", 79, 16);
        assertEquals(compileResult.getErrorCount(), indx);
    }

    @Test(description = "Test record destructure negative cases")
    public void recordDestructureTest() {
        CompileResult compileResult = BCompileUtil.
                compile("test-src/record/negative/record-destructure-negative.bal");
        BAssertUtil.validateError(compileResult, 0,
                "invalid record binding pattern; unknown field '_' in record type 'Person'",
                27, 5);
    }

    @Test
    public void testInvalidUsageOfCheckInRecordFieldDefaultValue() {
        CompileResult compileResult = BCompileUtil.
                compile("test-src/record/negative/record_field_default_value_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                22, 13);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                23, 21);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                24, 23);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                36, 25);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                39, 17);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                46, 19);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                52, 17);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                53, 18);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                54, 12);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                55, 16);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                60, 23);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                66, 21);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                67, 21);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                67, 42);
        BAssertUtil.validateError(compileResult, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION,
                68, 16);
        BAssertUtil.validateError(compileResult, i++, "cannot use 'check' in an object field initializer of an object" +
                " with no 'init' method", 70, 27);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }
}
