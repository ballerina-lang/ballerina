// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/system;

function testValidEnv() returns string {
    return system:getEnv("JAVA_HOME");
}

function testEmptyEnv() returns string {
    return system:getEnv("JAVA_XXXX");
}

function testGetCurrentDirectory() returns string {
    return system:getCurrentDirectory();
}

function testGetUserHome() returns string {
    return system:getUserHome();
}

function testGetUsername() returns string {
    return system:getUsername();
}

function testRandomString() returns (string) {
    return system:uuid();
}

function toString(io:ReadableByteChannel input) returns string {
    string result = "";
    io:ReadableCharacterChannel charIn = new(input, "UTF-8");
    while (true) {
        var x = charIn.read(1);
        if (x is error) { break; }
        else { result = result + x; }
    }
    return <@untainted> result;
}

function testExecInUnixLike1() returns [string, int, int]|error {
    system:Process x1 = check system:exec("env", { "BAL_EXEC_TEST_VAR":"X" });
    system:Process x2 = check system:exec("grep", {}, (), "BAL_EXEC_TEST_VAR");
    system:Process x3 = check system:exec("wc", {}, (), "-l");
    var x3out = x1.pipe(x2).pipe(x3).stdout();
    var ec1 = check x3.waitForExit();
    var ec2 = check x3.exitCode();
    var result = toString(x3out);
    return [result, ec1, ec2];
}

function testExecInUnixLike2() returns int|error {
    system:Process x1 = check system:exec("sleep", {}, (), "5");
    x1.destroy();
    var ec = check x1.exitCode();
    return ec;
}

function testExecInUnixLike3() returns string|error {
    system:Process x1 = check system:exec("pwd", {}, "/");
    var x1out = x1.stdout();
    var result = toString(x1out);
    return result;
}

function testExecInUnixLike4() returns string|error {
    system:Process x1 = check system:exec("grep", {}, (), "BAL_TEST");
    io:WritableDataChannel ch = new(x1.stdin());
    check ch.writeString("BAL_TEST", "UTF-8");
    check ch.close();
    var result = toString(x1.stdout());
    return result;
}