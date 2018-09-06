/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.debugger;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.test.utils.debug.DebugPoint;
import org.ballerinalang.test.utils.debug.ExpectedResults;
import org.ballerinalang.test.utils.debug.Util;
import org.ballerinalang.test.utils.debug.VMDebuggerUtil;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.VariableDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.test.utils.debug.Step.RESUME;
import static org.ballerinalang.test.utils.debug.Step.STEP_IN;
import static org.ballerinalang.test.utils.debug.Step.STEP_OUT;
import static org.ballerinalang.test.utils.debug.Step.STEP_OVER;

/**
 * Test Cases for {@link Debugger}.
 */
public class VMDebuggerTest {

    private static final String FILE = "test-debug.bal";
    private PrintStream original;

    @BeforeClass
    public void setup() {
        original = System.out;
        // Hiding all test System outs.
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    @AfterClass
    public void tearDown() {
        System.setOut(original);
    }

    @Test(description = "Testing Resume with break points.")
    public void testResume() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", FILE,
                3, 9, 17, 29, 30, 33, 35, 37, 42, 43, 44, 45, 46, 47);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 3, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 17, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 30, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 37, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 42, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 43, RESUME, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("p", "10");
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 7, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Debugger with breakpoint in non executable and not reachable lines.")
    public void testNegativeBreakPoints() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", FILE, 4, 7, 51, 39);

        List<DebugPoint> debugPoints = new ArrayList<>();

        ExpectedResults expRes = new ExpectedResults(debugPoints, 0, 0, new ArrayList<>(), false);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step In.")
    public void testStepIn() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", FILE, 5, 8, 41);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 5, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 12, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 13, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 14, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 15, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 16, STEP_IN, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("x", "15");
        expMap1.put("y", "5");
        expMap1.put("z", "0");
        expMap1.put("a", "6");
        debugPoints.add(Util.createDebugPoint(".", FILE, 20, STEP_IN, 1, expMap1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 14, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 8, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 41, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 25, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 26, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 27, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 28, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 30, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 31, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 37, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 38, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 42, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 43, STEP_IN, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap2 = new HashMap<>();
        expMap2.put("p", "10");
        expMap2.put("r", "100");
        expMap2.put("s", "large");
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, RESUME, 1, expMap2));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 21, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step Out.")
    public void testStepOut() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", FILE, 26);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 26, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 41, STEP_OUT, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("p", "10");
        expMap1.put("q", "5");
        expMap1.put("r", "100");
        expMap1.put("s", "large");
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 3, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step Over.")
    public void testStepOver() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", FILE, 3);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 3, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 5, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 6, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 8, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 10, RESUME, 1, null));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 6, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step over in IfCondition.")
    public void testStepOverIfStmt() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", FILE, 26);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 26, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 27, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 28, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 30, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 31, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 37, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 38, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 41, RESUME, 1, null));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 8, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step over in WhileStmt.")
    public void testStepOverWhileStmt() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", FILE, 13, 14, 20, 22);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 13, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 14, RESUME, 5, null));
        debugPoints.add(Util.createDebugPoint(".", FILE, 20, RESUME, 4, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("x", "15");
        expMap1.put("y", "5");
        expMap1.put("z", "100");
        expMap1.put("a", "10");
        debugPoints.add(Util.createDebugPoint(".", FILE, 22, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 11, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing while statement resume")
    public void testWhileStatementResume() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".",
                "while-statement.bal", 5);

        List<DebugPoint> debugPoints = new ArrayList<>();
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("args", "Array[2] [\"Hello\", \"World\"]");
        debugPoints.add(Util.createDebugPoint(".", "while-statement.bal", 5, RESUME, 5, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("i", "Local", new BInteger(4)));
        variables.add(Util.createVariable("args", "Local", new BStringArray(new String[]{"Hello", "World"})));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 5, 0, variables, true);

        VMDebuggerUtil.startDebug("test-src/debugger/while-statement.bal", breakPoints, expRes);
    }

    @Test(description = "Testing try catch finally scenario for path")
    public void testTryCatchScenarioForPath() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".",
                "try-catch-finally.bal", 19);

        String file = "try-catch-finally.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 19, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 27, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 29, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 31, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 32, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 33, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 34, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 43, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 44, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 45, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 50, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 55, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 56, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 58, STEP_OVER, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("path", "start insideTry insideInnerTry onError innerTestErrorCatch:test innerFinally " +
                "TestErrorCatch Finally ");
        debugPoints.add(Util.createDebugPoint(".", file, 60, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 16, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/try-catch-finally.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug paths in workers")
    public void testDebuggingWorkers() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".",
                "test-worker.bal", 3, 9, 10, 18, 19, 23, 48);

        String file = "test-worker.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 3, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 9, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 10, STEP_OVER, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("a", "Cannot find local variable 'a'");
        expMap1.put("p", "15");
        expMap1.put("q", "5");
        debugPoints.add(Util.createDebugPoint(".", file, 12, STEP_IN, 1, expMap1));
        debugPoints.add(Util.createDebugPoint(".", file, 30, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 31, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 13, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 18, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 19, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 48, RESUME, 5, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap2 = new HashMap<>();
        expMap2.put("a", "0");
        expMap2.put("p", "15");
        expMap2.put("q", "5");
        expMap2.put("b", "100");
        debugPoints.add(Util.createDebugPoint(".", file, 23, RESUME, 1, expMap2));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 15, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-worker.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug paths in package init")
    public void testDebuggingPackageInit() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".",
                "test-package-init.bal", 3, 9);

        String file = "test-package-init.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 3, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 5, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 13, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 14, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 15, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 16, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 17, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 21, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 15, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 16, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 17, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 21, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 15, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 23, RESUME, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("val1", "60");
        expMap1.put("val2", "20");
        expMap1.put("cal", "80");
        debugPoints.add(Util.createDebugPoint(".", file, 9, RESUME, 1, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("val1", "Global", new BInteger(60)));
        variables.add(Util.createVariable("val2", "Global", new BInteger(20)));
        variables.add(Util.createVariable("cal", "Local", new BInteger(80)));
        variables.add(Util.createVariable("args", "Local", new BStringArray(new String[]{"Hello", "World"})));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 15, 2, variables, true);

        VMDebuggerUtil.startDebug("test-src/debugger/test-package-init.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug match statement and objects")
    public void testDebuggingMatchAndObject() {
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".",
                "test_object_and_match.bal", 3, 48, 66, 54);

        String file = "test_object_and_match.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 3, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 7, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 29, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 23, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 26, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 30, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 31, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 32, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 8, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 36, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 37, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 51, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 9, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 29, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 10, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 36, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 39, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 40, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 11, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 12, STEP_IN, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 36, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 39, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 42, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 43, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 44, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 13, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 14, STEP_OVER, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 15, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 48, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 16, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 66, RESUME, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 54, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 67, STEP_OUT, 1, null));
        debugPoints.add(Util.createDebugPoint(".", file, 4, RESUME, 1, null));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 37, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test_object_and_match.bal", breakPoints, expRes);
    }

    @Test(description = "Testing global variables availability in debug hit message")
    public void testGlobalVarAvailability() {
        String file = "test_variables.bal";
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", file, 10);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 10, STEP_OVER, 1, null));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("gInt", "5");
        expMap1.put("gBool", "true");
        expMap1.put("y", "25");
        expMap1.put("foo", "Record Foo {count:5, last:\"last\"}");
        debugPoints.add(Util.createDebugPoint(".", file, 11, RESUME, 1, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("gInt", "Global", new BInteger(5)));
        variables.add(Util.createVariable("gStr", "Global", new BString("str")));
        variables.add(Util.createVariable("gBool", "Global", new BBoolean(true)));
        variables.add(Util.createVariable("gByte", "Global", new BByte((byte) 255)));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 2, 4, variables, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test_variables.bal", breakPoints, expRes);
    }

    @Test(description = "Testing local variables scopes")
    public void testLocalVarScope() {
        String file = "test_variables.bal";
        BreakPointDTO[] breakPoints = Util.createBreakNodeLocations(".", file, 9);

        List<DebugPoint> debugPoints = new ArrayList<>();
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        expMap1.put("gStr", "str");
        expMap1.put("gByte", "255");
        expMap1.put("x", "10");
        expMap1.put("z", "15");
        expMap1.put("args", "Array[2] [\"Hello\", \"World\"]");
        debugPoints.add(Util.createDebugPoint(".", file, 9, RESUME, 1, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("gInt", "Global", new BInteger(5)));
        variables.add(Util.createVariable("gStr", "Global", new BString("str")));
        variables.add(Util.createVariable("gBool", "Global", new BBoolean(true)));
        variables.add(Util.createVariable("gByte", "Global", new BByte((byte) 255)));
        variables.add(Util.createVariable("args", "Local", new BStringArray(new String[]{"Hello", "World"})));
        variables.add(Util.createVariable("x", "Local", new BInteger(10)));
        variables.add(Util.createVariable("z", "Local", new BInteger(15)));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 1, 4, variables, true);

        VMDebuggerUtil.startDebug("test-src/debugger/test_variables.bal", breakPoints, expRes);
    }
}
