/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io;

import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.ballerinalang.stdlib.common.CommonTestUtils.getAbsoluteFilePath;

/**
 * Tests I/O related functions.
 */
public class IOTest {

    private CompileResult bytesInputOutputProgramFile;
    private CompileResult characterInputOutputProgramFile;
    private CompileResult recordsInputOutputProgramFile;
    private CompileResult stringInputOutputProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        bytesInputOutputProgramFile = BCompileUtil.compile("test-src/io/bytes_io.bal");
        characterInputOutputProgramFile = BCompileUtil.compile("test-src/io/char_io.bal");
        recordsInputOutputProgramFile = BCompileUtil.compile("test-src/io/record_io.bal");
        stringInputOutputProgramFile = BCompileUtil.compile("test-src/io/string_io.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    @Test(description = "Test 'readBytes' function in ballerina/io package")
    public void testReadBytes() throws URISyntaxException {
        int numberOfBytesToRead = 3;
        String resourceToRead = "datafiles/io/text/6charfile.txt";
        BValueArray readBytes;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead))};
        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "initReadableChannel", args);

        //Reads the 1st three bytes "123"
        byte[] expectedBytes = "123".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        BValue[] returns = BRunUtil.invokeStateful(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.getBytes());

        //Reads the next three bytes "456"
        expectedBytes = "456".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invokeStateful(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.getBytes());

        //Request for a get, the bytes will be empty
        expectedBytes = new byte[0];
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invokeStateful(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.getBytes());

        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'readCharacters' function in ballerina/io package")
    public void testReadCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        int numberOfCharactersToRead = 3;
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initReadableChannel", args);

        String expectedCharacters = "aaa";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "bbǊ";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'readCharacters' function in ballerina/io package")
    public void testReadAllCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/fileThatExceeds2MB.txt";
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initReadableChannel", args);

        int expectedNumberOfCharacters = 2265223;
        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readAllCharacters");
        readCharacters = (BString) returns[0];

        //getting the result string and filtering the CR characters which were added when running in windows
        String returnedString = readCharacters.stringValue().replaceAll("\r", "");
        Assert.assertEquals(returnedString.length(), expectedNumberOfCharacters);
    }

    @Test(description = "Test 'readCharacters' function in ballerina/io package")
    public void testReadAllCharactersFromEmptyFile() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/emptyFile.txt";
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initReadableChannel", args);

        int expectedNumberOfCharacters = 0;
        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readAllCharacters");
        readCharacters = (BString) returns[0];

        String returnedString = readCharacters.stringValue();
        Assert.assertEquals(returnedString.length(), expectedNumberOfCharacters);
    }

    @Test(description = "Test 'readRecords' function in ballerina/io package")
    public void testReadRecords() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"),
                new BString("\n"), new BString(",")};
        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "initReadableChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        BError error = (BError) returns[0];
        Assert.assertTrue(IOConstants.IO_EOF.equals(((BMap) error.getDetails()).getMap().get("message").toString()));
        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "closeReadableChannel");
    }


    @Test(description = "Test 'writeBytes' function in ballerina/io package")
    public void testWriteBytes() {
        byte[] content = {-1, 46, 77, 90, 38};
        String sourceToWrite = currentDirectoryPath + "/bytesFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite)};
        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[]{new BValueArray(content), new BInteger(0)};
        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "writeBytes", args);

        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test 'writeCharacters' function in ballerina/io package")
    public void testWriteCharacters() {
        String content = "The quick brown fox jumps over the lazy dog";
        String sourceToWrite = currentDirectoryPath + "/characterFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[]{new BString(content), new BInteger(0)};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeCharacters", args);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test 'write' function with `append = true` in ballerina/io package")
    public void appendCharacters() throws IOException {
        String initialContent = "Hi, I'm the initial content. ";
        String appendingContent = "Hi, I was appended later. ";
        String sourceToWrite = currentDirectoryPath + "/appendCharacterFile.txt";
        //Will initialize the writable channel
        BValue[] args = {new BString(sourceToWrite), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initWritableChannel", args);

        // Write chars to file
        args = new BValue[]{new BString(initialContent), new BInteger(0)};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeCharacters", args);

        //Will initialize the writable channel to append characters
        args = new BValue[]{new BString(sourceToWrite), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initWritableChannelToAppend", args);

        // Append chars to file
        args = new BValue[]{new BString(appendingContent), new BInteger(0)};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "appendCharacters", args);

        Assert.assertEquals(readFile(sourceToWrite), initialContent + appendingContent);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeWritableChannel");
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeWritableChannelToAppend");
        deleteFile(sourceToWrite);
    }

    @Test(description = "Test 'writeRecords' function in ballerina/io package")
    public void testWriteRecords() {
        String[] content = {"Name", "Email", "Telephone"};
        BValueArray record = new BValueArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsFile.csv";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("UTF-8"), new BString("\n"), new
                BString(",")};
        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[]{record};
        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "writeRecord", args);

        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test 'readJson' function in ballerina/io package")
    public void testJsonCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/web-app.json";

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initReadableChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readJson");
        Assert.assertNotNull(returns[0].stringValue());
        Assert.assertEquals(returns[0].stringValue().replace(", ", ","), readFileContent(resourceToRead),
                "JSON content mismatch.");

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'writeJson' function in ballerina/io package")
    public void testWriteJsonCharacters() {
        String content = "{\n" +
                "  \"test\": { \"name\": \"Foo\" }\n" +
                "}";

        String sourceToWrite = currentDirectoryPath + "/jsonCharsFile.json";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[]{JsonParser.parse(content)};
        BValue[] result = BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeJson", args);

        //Assert if there's no error return
        Assert.assertNull(result[0]);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test double byte unicode write function in ballerina/io package")
    public void testWriteHigherUnicodeRangeJsonCharacters() {
        String sourceToWrite = currentDirectoryPath + "/unicode.json";
        //Will initialize the channel
        BValue[] args = { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initWritableChannel", args);
        BValue[] result = BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeJsonWithHigherUnicodeRange");
        //Assert if there's no error return
        Assert.assertNull(result[0]);
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeWritableChannel");
        try {
            String content = "{\"loop\":\"É\"}";
            Assert.assertEquals(content,
                    new String(Files.readAllBytes(Paths.get(sourceToWrite)), StandardCharsets.UTF_8).trim());
        } catch (IOException e) {
            Assert.fail("Unable to read from file", e);
        }
    }

    @Test(description = "Test 'writeXml' function in ballerina/io package")
    public void testWriteXmlCharacters() {
        String content = "\t<test>\n" +
                "\t\t<name>Foo</name>\n" +
                "\t</test>";

        String sourceToWrite = currentDirectoryPath + "/xmlCharsFile.xml";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[]{new BXMLItem(content)};
        BValue[] result = BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeXml", args);

        //Assert if there's no error return
        Assert.assertNull(result[0]);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeWritableChannel");
    }


    @Test(description = "Test 'readXml' function in ballerina/io package")
    public void testXmlCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/cd_catalog.xml";

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initReadableChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readXml");
        Assert.assertNotNull(returns[0].stringValue());
        Assert.assertEquals(returns[0].stringValue(), readFileContent(resourceToRead), "XML content mismatch.");

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test function to convert string to json")
    public void convertStringToJsonTest() throws URISyntaxException {
        String content = "{\n" +
                "  \"test\": { \"name\": \"Foo\" }\n" +
                "}";
        BValue[] args = {new BString(content), new BString("UTF-8")};
        BValue[] result = BRunUtil.invokeStateful(stringInputOutputProgramFile, "getJson", args);
        Assert.assertTrue(result[0].stringValue().contains("Foo"));
    }

    @Test(description = "Test function to convert xml to string")
    public void convertStringToXmlTest() throws URISyntaxException {
        String content = "\t<test>\n" +
                "\t\t<name>Foo</name>\n" +
                "\t</test>";
        BValue[] args = {new BString(content), new BString("UTF-8")};
        BValue[] result = BRunUtil.invokeStateful(stringInputOutputProgramFile, "getXml", args);
        Assert.assertTrue(result[0].stringValue().contains("Foo"));
    }

    private String readFileContent(String filePath) throws URISyntaxException {
        Path path = Paths.get(getAbsoluteFilePath(filePath));
        StringBuilder data = new StringBuilder();
        Stream<String> lines;
        try {
            lines = Files.lines(path);
        } catch (IOException e) {
            return "";
        }
        lines.forEach(line -> data.append(line.trim()));
        lines.close();
        return data.toString();
    }

    private String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
