/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.stamp;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for stamping variables.
 *
 * @since 0.985.0
 */
public class StampInbuiltFunctionNegativeTest {

    private CompileResult compileResult;
    private CompileResult recordNegativeTestCompileResult;
    private CompileResult jsonNegativeTestCompileResult;
    private CompileResult xmlNegativeTestCompileResult;
    private CompileResult mapNegativeTestCompileResult;
    private CompileResult objectNegativeTestCompileResult;
    private CompileResult arrayNegativeTestCompileResult;
    private CompileResult tupleNegativeTestCompileResult;
    private CompileResult unionNegativeTestCompileResult;
    private CompileResult anydataNegativeTestCompileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/negative/stamp-expr-negative-test.bal");
        recordNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/record-stamp-expr-negative-test.bal");
        jsonNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/json-stamp-expr-negative-test.bal");
        xmlNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/xml-stamp-expr-negative-test.bal");
        mapNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/map-stamp-expr-negative-test.bal");
        objectNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/object-stamp-expr-negative-test.bal");
        arrayNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/array-stamp-expr-negative-test.bal");
        tupleNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/tuple-stamp-expr-negative-test.bal");
        unionNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/union-stamp-expr-negative-test.bal");
        anydataNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/anydata-stamp-expr-negative-test.bal");
    }

    //----------------------------- NegativeTest cases ------------------------------------------------------

    @Test
    public void testStampNegativeTest() {

        Assert.assertEquals(compileResult.getErrorCount(), 7);

        //Negative test case to verify the unsupported type for stamp operation.
        BAssertUtil.validateError(compileResult, 0,
                "stamp function on type 'stream<Employee>' is not supported",
                34, 35);

        //Negative test case to verify the no of arguments get passed to stamp function.
        BAssertUtil.validateError(compileResult, 1,
                "too many arguments in call to 'stamp()'",
                41, 24);

        //Negative test case to confirm primitive types are not supported for stamp operation.
        BAssertUtil.validateError(compileResult, 2,
                "stamp function on type 'string' is not supported",
                48, 22);

        //Negative test case to confirm primitive types are not supported for stamp operation.
        BAssertUtil.validateError(compileResult, 3,
                "incompatible stamp type: type 'string[]' cannot be stamped as type 'any'",
                55, 20);

        //Negative test case to confirm values cannot be stamped as primitive type.
        BAssertUtil.validateError(compileResult, 4,
                "incompatible stamp type: type 'any' cannot be stamped as type 'string'",
                62, 27);

        //Negative test case to confirm invalid types cannot be used as argument for stamp function.
        BAssertUtil.validateError(compileResult, 5,
                "undefined symbol 'TestType'",
                70, 24);
    }

    @Test
    public void testRecordStampNegativeTest() {

        Assert.assertEquals(recordNegativeTestCompileResult.getErrorCount(), 8);

        //Negative test case to confirm record cannot be stamped as xml.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 0,
                "incompatible stamp type: type 'Employee' cannot be " +
                        "stamped as type 'xml'",
                56, 20);

        //Negative test case to confirm open record to closed record stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 1,
                "incompatible stamp type: type 'Teacher' cannot be stamped as " +
                        "type 'Employee'",
                63, 25);

        //Negative test case to confirm closed record to closed record stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 2,
                "incompatible stamp type: type 'Person' cannot be stamped as type 'Student'",
                71, 23);

        //Negative test case to confirm closed record to object stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 3,
                "incompatible stamp type: type 'Teacher' cannot be stamped as " +
                        "type 'TeacherObj'",
                79, 30);

        //Negative test case to confirm closed record to map stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 4,
                "incompatible stamp type: type 'Person' cannot be stamped as " +
                        "type 'map<string>'",
                87, 28);

        //Negative test case to confirm closed record to array stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 5,
                "incompatible stamp type: type 'Employee' cannot be stamped as type" +
                        " 'string[]'",
                94, 28);

        //Negative test case to confirm closed record to tuple stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 6,
                "incompatible stamp type: type 'Employee' cannot be stamped as type" +
                        " '(string,string)'",
                102, 35);

        //Negative test case to confirm record(with  object) to anydata stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 7,
                "stamp function on type 'ExtendedEmployee' is not supported",
                123, 28);
    }

    @Test
    public void testJSONStampNegativeTest() {

        Assert.assertEquals(jsonNegativeTestCompileResult.getErrorCount(), 3);

        //Negative test case to confirm record cannot be stamped as xml.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 0,
                "incompatible stamp type: type 'json' cannot be stamped as type " +
                        "'xml'", 28, 20);

        //Negative test case to confirm record cannot be stamped as xml.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 1,
                "incompatible stamp type: type 'json' cannot be stamped as " +
                        "type 'EmployeeObj'", 35, 31);

        //Negative test case to confirm record cannot be stamped as tuple.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 2,
                "incompatible stamp type: type 'json' cannot be stamped as " +
                        "type '(string,string)'", 43, 35);
    }

    @Test
    public void testXMLStampNegativeTest() {

        Assert.assertEquals(xmlNegativeTestCompileResult.getErrorCount(), 6);

        //Negative test case to confirm xml cannot be stamped as record.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 0,
                "incompatible stamp type: type 'xml' cannot be stamped as type 'BookRecord'",
                29, 30);

        //Negative test case to confirm xml cannot be stamped as json.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 1,
                "incompatible stamp type: type 'xml' cannot be stamped as type 'json'",
                37, 22);

        //Negative test case to confirm xml cannot be stamped as Object.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 2,
                "incompatible stamp type: type 'xml' cannot be stamped as type 'BookObject'",
                45, 30);

        //Negative test case to confirm xml cannot be stamped as map.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 3,
                "incompatible stamp type: type 'xml' cannot be stamped as type 'map'",
                53, 25);

        //Negative test case to confirm xml cannot be stamped as array.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 4,
                "incompatible stamp type: type 'xml' cannot be stamped as type 'BookRecord[]'",
                61, 31);

        //Negative test case to confirm xml cannot be stamped as tuple.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 5,
                "incompatible stamp type: type 'xml' cannot be stamped as type '(string,string)'",
                69, 35);
    }

    @Test
    public void testMapStampNegativeTest() {

        Assert.assertEquals(mapNegativeTestCompileResult.getErrorCount(), 4);

        //Negative test case to confirm map cannot be stamped as xml.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 0,
                "incompatible stamp type: type 'map' cannot be stamped as type 'xml'",
                26, 20);

        //Negative test case to confirm map cannot be stamped as array.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 1,
                "incompatible stamp type: type 'map<anydata>' cannot be stamped as type 'string[]'",
                33, 27);

        //Negative test case to confirm map cannot be stamped as tuple.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 2,
                "incompatible stamp type: type 'map<anydata>' cannot be stamped as type '(string,string)'",
                40, 34);

        //Negative test case to confirm map cannot be stamped as object.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 3,
                "incompatible stamp type: type 'map<anydata>' cannot be stamped as type 'IntObject'",
                47, 29);
    }

    @Test
    public void testObjectStampNegativeTest() {

        Assert.assertEquals(objectNegativeTestCompileResult.getErrorCount(), 6);

        //Negative test case to confirm object cannot be stamped as record.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 0,
                "stamp function on type 'PersonObj' is not supported",
                41, 25);

        //Negative test case to confirm object cannot be stamped as json.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 1,
                "stamp function on type 'PersonObj' is not supported",
                49, 22);

        //Negative test case to confirm object cannot be stamped as xml.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 2,
                "stamp function on type 'PersonObj' is not supported",
                56, 20);

        //Negative test case to confirm object cannot be stamped as map.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 3,
                "stamp function on type 'PersonObj' is not supported",
                63, 25);

        //Negative test case to confirm object cannot be stamped as array.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 4,
                "stamp function on type 'PersonObj' is not supported",
                70, 22);

        //Negative test case to confirm object cannot be stamped as tuple.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 5,
                "stamp function on type 'PersonObj' is not supported",
                77, 32);
    }

    @Test
    public void testArrayStampNegativeTest() {

        Assert.assertEquals(arrayNegativeTestCompileResult.getErrorCount(), 4);

        //Negative test case to confirm array cannot be stamped as record.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 0,
                "incompatible stamp type: type 'anydata[]' cannot be stamped as type 'Employee'",
                34, 25);

        //Negative test case to confirm array cannot be stamped as xml.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 1,
                "incompatible stamp type: type 'anydata[]' cannot be stamped as type 'xml'",
                42, 20);

        //Negative test case to confirm array cannot be stamped as object.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 2,
                "incompatible stamp type: type 'anydata[]' cannot be stamped as type 'EmployeeObject'",
                50, 34);

        //Negative test case to confirm array cannot be stamped as map.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 3,
                "incompatible stamp type: type 'anydata[]' cannot be stamped as type 'map'",
                58, 25);
    }

    @Test
    public void testTupleStampNegativeTest() {

        Assert.assertEquals(tupleNegativeTestCompileResult.getErrorCount(), 5);

        //Negative test case to confirm tuple cannot be stamped as record.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 0,
                "incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'Employee'",
                41, 28);

        //Negative test case to confirm tuple cannot be stamped as json.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 1,
                "incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'json'",
                48, 22);

        //Negative test case to confirm tuple cannot be stamped as xml.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 2,
                "incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'xml'",
                55, 20);

        //Negative test case to confirm tuple cannot be stamped as object.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 3,
                "incompatible stamp type: type '(string,int)' cannot be stamped as type 'EmployeeObj'",
                62, 31);

        //Negative test case to confirm tuple cannot be stamped as map.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 4,
                "incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'map'",
                69, 25);
    }

    @Test
    public void testUnionStampNegativeTest() {

        Assert.assertEquals(unionNegativeTestCompileResult.getErrorCount(), 2);

        BAssertUtil.validateError(unionNegativeTestCompileResult, 0,
                "incompatible stamp type: type 'int|float|xml' cannot be stamped as type 'Employee'",
                28, 30);

        BAssertUtil.validateError(unionNegativeTestCompileResult, 1,
                "incompatible stamp type: type " +
                        "'int|float|(string,string)' cannot be stamped as type 'int|float|(string,int)'",
                34, 49);
    }

    @Test
    public void testAnydataStampNegativeTest() {

        Assert.assertEquals(anydataNegativeTestCompileResult.getErrorCount(), 2);

        //Negative test case to validate invalid stamp conversion from anydata to object.

        BAssertUtil.validateError(anydataNegativeTestCompileResult, 0,
                "incompatible types: expected 'anydata', found 'PersonObj'",
                29, 28);

        BAssertUtil.validateError(anydataNegativeTestCompileResult, 1,
                "incompatible stamp type: type 'anydata' cannot be stamped as type 'PersonObj'",
                30, 27);
    }
}

