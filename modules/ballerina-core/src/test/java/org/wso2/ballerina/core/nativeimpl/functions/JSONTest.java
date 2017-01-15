/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Test Native functions in ballerina.lang.json.
 */
@SuppressWarnings("javadoc")
public class JSONTest {

    private BallerinaFile bFile;
    private static final String json1 = "{'name':{'fname':'Jack','lname':'Taylor'}, 'state':'CA', 'age':20}";
    private static final String json2 = "{'item':{'price':3.54, 'available':true}}";
    private static final String jsonStringArray = "{'users':['Jack', 'Peter']}";
    private static final String jsonIntArray = "{'ages':[25, 28]}";
    private static final String jsonFloatArray = "{'prices':[3.12, 4.87]}";
    private static final String jsonBooleanArray = "{'availability':[true, false]}";
    private static final String jsonElementArray = "{'persons':[{'fname':'Jack','lname':'Taylor'}, {'fname':'Peter'," +
            "'lname':'Roy'}]}";

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.system:print_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/jsonTest.bal", symScope);
    }

    
    /*
     * Test Get-Functions 
     */

    @Test(description = "Get a string in a valid jsonpath")
    public void testGetString() {
        BValue[] args = {new BJSON(json1), new BString("$.name.fname")};
        BValue[] returns = Functions.invoke(bFile, "getString", args);

        Assert.assertEquals(returns[0].stringValue(), "Jack");
    }

    @Test(description = "Get an integer in a valid jsonpath")
    public void testGetInt() {
        BValue[] args = {new BJSON(json1), new BString("$.age")};
        BValue[] returns = Functions.invoke(bFile, "getInt", args);

        Assert.assertEquals(((BValueType) returns[0]).intValue(), 20);
    }

    @Test(description = "Get a json element in a valid jsonpath")
    public void testGetJSON() {
        BValue[] args = {new BJSON(json1), new BString("$.name")};
        BValue[] returns = Functions.invoke(bFile, "getJson", args);

        Assert.assertEquals(getJsonAsString(returns[0]), "{\"fname\":\"Jack\",\"lname\":\"Taylor\"}");
    }

    @Test(description = "Get a float in a valid jsonpath")
    public void testGetFloat() {
        BValue[] args = {new BJSON(json2), new BString("$.item.price")};
        BValue[] returns = Functions.invoke(bFile, "getFloat", args);

        Assert.assertEquals(((BValueType) returns[0]).floatValue(), (float) 3.54);
    }

    @Test(description = "Get a double in a valid jsonpath")
    public void testGetDouble() {
        BValue[] args = {new BJSON(json2), new BString("$.item.price")};
        BValue[] returns = Functions.invoke(bFile, "getDouble", args);

        Assert.assertEquals(((BValueType) returns[0]).doubleValue(), 3.54);
    }

    @Test(description = "Get a float in a valid jsonpath")
    public void testGetBoolean() {
        BValue[] args = {new BJSON(json2), new BString("$.item.available")};
        BValue[] returns = Functions.invoke(bFile, "getBoolean", args);

        Assert.assertEquals(((BValueType) returns[0]).booleanValue(), true);
    }

    @Test(description = "Get json array length")
    public void testGetArrayLength() {
        BValue[] args = {new BJSON(jsonStringArray), new BString("$.users.length()")};
        BValue[] returns = Functions.invoke(bFile, "getInt", args);

        final int expected = 2;
        Assert.assertEquals(((BValueType) returns[0]).intValue(), expected);
    }

    @Test(description = "Get json array min value")
    public void testGetArrayMin() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.min()")};
        BValue[] returns = Functions.invoke(bFile, "getDouble", args);

        final double expected = 25;
        Assert.assertEquals(((BValueType) returns[0]).doubleValue(), expected);
    }

    @Test(description = "Get json array max value")
    public void testGetArrayMax() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.max()")};
        BValue[] returns = Functions.invoke(bFile, "getDouble", args);

        final double expected = 28;
        Assert.assertEquals(((BValueType) returns[0]).doubleValue(), expected);
    }

    @Test(description = "Get json array average value")
    public void testGetArrayAvg() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.avg()")};
        BValue[] returns = Functions.invoke(bFile, "getDouble", args);

        final double expected = 26.5;
        Assert.assertEquals(((BValueType) returns[0]).doubleValue(), expected);
    }

    @Test(description = "Get json array standard deviation of population")
    public void testGetArrayStdDevP() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.stddev()")};
        BValue[] returns = Functions.invoke(bFile, "getDouble", args);

        final double expected = 1.5;
        Assert.assertEquals(((BValueType) returns[0]).doubleValue(), expected);
    }

    @Test(description = "Get a value of incorrect type", expectedExceptions = {BallerinaException.class})
    public void testGetValueOfIncorrectType() {
        BValue[] args = {new BJSON(json1), new BString("$.name")};
        Functions.invoke(bFile, "getInt", args);
    }

    @Test(description = "Get a value in a non-existing jsonpath", expectedExceptions = BallerinaException.class)
    public void testGetNonExistingValue() {
        BValue[] args = {new BJSON(json1), new BString("$.name.surname")};
        BValue[] returns = Functions.invoke(bFile, "getString", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Get a value using a malformed jsonpath", expectedExceptions = {BallerinaException.class})
    public void testGetValueFromInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        Functions.invoke(bFile, "getString", args);
    }


    /*
     * Test Set-Functions
     */

    @Test(description = "Set a string to a valid jsonpath")
    public void testSetString() {
        final String val = "Paul";
        BValue[] args = {new BJSON(json1), new BString("$.name.fname"), new BString(val)};
        BValue[] returns = Functions.invoke(bFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), val);
    }

    @Test(description = "Set an int to a valid jsonpath")
    public void testSetInteger() {
        final int val = 25;
        BValue[] args = {new BJSON(json1), new BString("$.age"), new BInteger(val)};
        BValue[] returns = Functions.invoke(bFile, "setInt", args);

        Assert.assertEquals(((BValueType) returns[0]).intValue(), val);
    }

    @Test(description = "Set a double to a valid jsonpath")
    public void testSetDouble() {
        final double val = 4.78;
        BValue[] args = {new BJSON(json2), new BString("$.item.price"), new BDouble(val)};
        BValue[] returns = Functions.invoke(bFile, "setDouble", args);

        Assert.assertEquals(((BValueType) returns[0]).doubleValue(), val);
    }

    @Test(description = "Set a float to a valid jsonpath")
    public void testSetFloat() {
        final float val = (float) 4.78;
        BValue[] args = {new BJSON(json2), new BString("$.item.price"), new BFloat(val)};
        BValue[] returns = Functions.invoke(bFile, "setFloat", args);

        Assert.assertEquals(((BValueType) returns[0]).floatValue(), val);
    }

    @Test(description = "Set a boolean to a valid jsonpath")
    public void testSetBoolean() {
        final boolean val = false;
        BValue[] args = {new BJSON(json2), new BString("$.item.available"), new BBoolean(val)};
        BValue[] returns = Functions.invoke(bFile, "setBoolean", args);

        Assert.assertEquals(((BValueType) returns[0]).booleanValue(), val);
    }

    @Test(description = "Set a json element to a valid jsonpath")
    public void testSetJSON() {
        final String val = "{\"id\":\"item123\"}";
        BValue[] args = {new BJSON(json2), new BString("$.item.available"), new BJSON(val)};
        BValue[] returns = Functions.invoke(bFile, "setJson", args);

        Assert.assertEquals(getJsonAsString(returns[0]), val);
    }

    @Test(description = "Set a value to a non-existing jsonpath", expectedExceptions = BallerinaException.class)
    public void testSetNonExistingValue() {
        BValue[] args = {new BJSON(json1), new BString("$.name.surname"), new BString("Paul")};
        BValue[] returns = Functions.invoke(bFile, "setString", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(args[0].stringValue(), expected);
    }

    @Test(description = "Set a value using a malformed jsonpath", expectedExceptions = {BallerinaException.class})
    public void testSetValueToInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        Functions.invoke(bFile, "getString", args);
    }

    /*
     * Test Add-to-object Functions
     */

    @Test(description = "Add a string to a valid json object")
    public void testAddStringToObject() {
        BValue[] args = {new BJSON(json1), new BString("$.name"), new BString("nickName"),
                new BString("Paul")};
        BValue[] returns = Functions.invoke(bFile, "addStringToObject", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\",\"nickName\":\"Paul\"}," +
                "\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an integer to a valid json object")
    public void testAddIntToObject() {
        BValue[] args = {new BJSON(json1), new BString("$"), new BString("zipCode"),
                new BInteger(90001)};
        BValue[] returns = Functions.invoke(bFile, "addIntToObject", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20," +
                "\"zipCode\":90001}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a double to a valid json object")
    public void testAddDoubleToObject() {
        BValue[] args = {new BJSON(json2), new BString("$.item"), new BString("discount"),
                new BDouble(0.15)};
        BValue[] returns = Functions.invoke(bFile, "addDoubleToObject", args);

        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"discount\":0.15}}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a float to a valid json object")
    public void testAddFloatToObject() {
        BValue[] args = {new BJSON(json2), new BString("$.item"), new BString("discount"),
                new BFloat((float) 0.15)};
        BValue[] returns = Functions.invoke(bFile, "addFloatToObject", args);

        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"discount\":0.15}}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a boolean to a valid json object")
    public void testAddBooleanToObject() {
        BValue[] args = {new BJSON(json2), new BString("$.item"), new BString("vegi"),
                new BBoolean(true)};
        BValue[] returns = Functions.invoke(bFile, "addBooleanToObject", args);

        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"vegi\":true}}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an element to a valid json object")
    public void testAddElementToObject() {
        BValue[] args = {new BJSON(json2), new BString("$.item"), new BString("expires"),
                new BJSON("{\"year\":2020,\"month\":12}")};
        BValue[] returns = Functions.invoke(bFile, "addElementToObject", args);

        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"expires\":{\"year\":2020,\"month\"" +
                ":12}}}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to a non-existing object")
    public void testAddToNonExistingObject() {
        BValue[] args = {new BJSON(json1), new BString("$.name.surname"), new BString("nickName"),
                new BString("Paul")};
        BValue[] returns = Functions.invoke(bFile, "addStringToObject", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to object, using a malformed jsonpath",
            expectedExceptions = {BallerinaException.class})
    public void testAddToInvalidJsonpathObject() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path."), new BString("nickName"),
                new BString("Paul")};
        Functions.invoke(bFile, "addStringToObject", args);
    }

    /*
     * Test Add-to-array Functions
     */

    @Test(description = "Add a string to a valid json array")
    public void testAddStringToArray() {
        BValue[] args = {new BJSON(jsonStringArray), new BString("$.users"), new BString("Jos")};
        BValue[] returns = Functions.invoke(bFile, "addStringToArray", args);

        final String expected = "{\"users\":[\"Jack\",\"Peter\",\"Jos\"]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an integer to a valid json array")
    public void testAddIntToArray() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages"), new BInteger(23)};
        BValue[] returns = Functions.invoke(bFile, "addIntToArray", args);

        final String expected = "{\"ages\":[25,28,23]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a float to a valid json array")
    public void testAddFloatToArray() {
        BValue[] args = {new BJSON(jsonFloatArray), new BString("$.prices"),
                new BFloat((float) 5.96)};
        BValue[] returns = Functions.invoke(bFile, "addFloatToArray", args);

        final String expected = "{\"prices\":[3.12,4.87,5.96]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a double to a valid json array")
    public void testAddDoubleToArray() {
        BValue[] args = {new BJSON(jsonFloatArray), new BString("$.prices"), new BDouble(5.96)};
        BValue[] returns = Functions.invoke(bFile, "addDoubleToArray", args);

        final String expected = "{\"prices\":[3.12,4.87,5.96]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a boolean to a valid json array")
    public void testAddBooleanToArray() {
        BValue[] args = {new BJSON(jsonBooleanArray), new BString("$.availability"),
                new BBoolean(true)};
        BValue[] returns = Functions.invoke(bFile, "addBooleanToArray", args);

        final String expected = "{\"availability\":[true,false,true]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an element to a valid json array")
    public void testAddElementToArray() {
        BValue[] args = {new BJSON(jsonElementArray), new BString("$.persons"),
                new BJSON("{'fname':'Jos','lname':'Allen'}")};
        BValue[] returns = Functions.invoke(bFile, "addElementToArray", args);

        final String expected = "{\"persons\":[{\"fname\":\"Jack\",\"lname\":\"Taylor\"},{\"fname\":\"Peter\"," +
                "\"lname\":\"Roy\"},{\"fname\":\"Jos\",\"lname\":\"Allen\"}]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to a non-existing array")
    public void testAddToNonExistingArray() {
        BValue[] args = {new BJSON(jsonStringArray), new BString("$.persons"), new BString("Jos")};
        BValue[] returns = Functions.invoke(bFile, "addStringToArray", args);

        final String expected = "{\"users\":[\"Jack\",\"Peter\"]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to array, using a malformed jsonpath",
            expectedExceptions = {BallerinaException.class})
    public void testAddToInvalidJsonpathArray() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$/wrong/path."), new BInteger(23)};
        Functions.invoke(bFile, "addIntToArray", args);
    }


    /*
     * Test Remove-Function.
     */

    @Test(description = "Remove an element in a valid jsonpath")
    public void testRemove() {
        BValue[] args = {new BJSON(json1), new BString("$.name")};
        BValue[] returns = Functions.invoke(bFile, "remove", args);

        final String expected = "{\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Remove an element in a non-existing jsonpath")
    public void testRemoveNonExistingElement() {
        BValue[] args = {new BJSON(json1), new BString("$.user")};
        BValue[] returns = Functions.invoke(bFile, "remove", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Remove an element in a malformed jsonpath",
            expectedExceptions = {BallerinaException.class})
    public void testRemoveElementFromInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        Functions.invoke(bFile, "remove", args);
    }


    /*
     * Test Rename-Function.
     */

    @Test(description = "Rename an element in a valid jsonpath")
    public void testRename() {
        BValue[] args = {new BJSON(json1), new BString("$.name"), new BString("fname"), new BString("firstName")};
        BValue[] returns = Functions.invoke(bFile, "rename", args);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Rename an element in a non-existing jsonpath", expectedExceptions = BallerinaException.class)
    public void testRenameNonExistingElement() {
        BValue[] args = {new BJSON(json1), new BString("$.user"), new BString("fname"), new BString("firstName")};
        BValue[] returns = Functions.invoke(bFile, "rename", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(args[0].stringValue(), expected);
    }

    @Test(description = "Rename an element in a malformed jsonpath",
            expectedExceptions = {BallerinaException.class})
    public void testRenameElementFromInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        Functions.invoke(bFile, "remove", args);
    }


    /*
     * Test toString-Function.
     */
    @Test(description = "Get string representation of json")
    public void testToString() {
        BValue[] args = {new BJSON(json1)};
        BValue[] returns = Functions.invoke(bFile, "toString", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    private String getJsonAsString(BValue bValue) {
        return bValue.stringValue().replace("\\r|\\n|\\t| ", "");
    }

}
