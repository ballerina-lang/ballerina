// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//------------ Testing a function with 'never' return type ---------

function functionWithNeverReturnType() returns never {
    string a = "hello";
    if (a == "a") {
        a = "b";
    } else {
        a = "c";
    }
}

function testNeverReturnTypedFunctionCall() {
    functionWithNeverReturnType();
}

//------------ Testing record type with 'never' field ---------

type InclusiveRecord record {
    int j;
    never p?;
};

type ExclusiveRecord record {|
    int j;
    never p?;
|};

function testInclusiveRecord() {
    InclusiveRecord inclusiveRecord = {j:0, "q":1};
}

function testExclusiveRecord() {
    ExclusiveRecord exclusiveRecord = {j:0};
}


//------------- Testing XML<never> -----------------

function testXMLWithNeverType() {
    xml<never> x = <xml<never>> 'xml:concat();  //generates an empty XML sequence and assign it to XML<never>
    xml<never> a = xml ``;
    xml<never> b = <xml<never>> 'xml:createText("");
    xml c = xml ``;
    'xml:Text d = xml ``;
    xml<'xml:Text> e = a;
    xml f = a;
    xml<xml<never>> g = xml ``;
    xml<xml<'xml:Text>> h = xml ``;
    string empty = "";
    'xml:Text j = xml `${empty}`;
    xml k = xml `${empty}`;
    xml<never>|'xml:Text l = xml ``;
    xml<never> & readonly m =  xml ``;
    string|'xml:Text n = a;

    xml<xml<'xml:Element>> t = xml ``;
    xml<'xml:Element> u = xml ``;
    xml<xml<'xml:Comment>> v = xml ``;
    xml<'xml:Comment> w = xml ``;
    xml<xml<'xml:ProcessingInstruction>> z = xml ``;
    xml<'xml:ProcessingInstruction> y = xml ``;
}

//---------------Test 'never' types with 'union-type' descriptors ------------
function testNeverWithUnionType1() {
    string|never j;
}

function testNeverWithUnionType2() {
    float|(int|never) j;
}

function testNeverWithUnionType3() {
    string|never j = "sample";
    string h = j;
}

// -------------Test 'never' with table key constraints --------------
type Person record {
  readonly string name;
  int age;
};

type PersonalTable table<Person> key<never>;

function testNeverWithKeyLessTable() {
    PersonalTable personalTable = table [
        { name: "DD", age: 33},
        { name: "XX", age: 34}
    ];
}

type SomePersonalTable table<Person> key<never|string>;

function testNeverInUnionTypedKeyConstraints() {
    SomePersonalTable somePersonalTable = table key(name) [
        { name: "MM", age: 33},
        { name: "PP", age: 34}
    ];
}

// --------------Test 'never' with 'future' type ----------------------

function testNeverAsFutureTypeParam() {
    future<never> someFuture;
}


// --------------Test 'never' with 'map' type ----------------------

function testNeverAsMappingTypeParam() {
    map<never> mp;
}

function testNeverWithCallStmt() {
    _ = foo();
}

function testNeverWithStartAction() {
    future<never> f = start foo();
    any result = wait f;
}

function testNeverWithTrapExpr() returns error? {
    error err = trap foo();
    return err;
}

function testNeverWithMethodCallExpr() {
    Bar bar = new (12);
    _ = bar.barFunc();
}

function foo() returns never {
  error e = error("Bad Sad!!");
  panic e;
}

class Bar {
    public int val;

    function init(int val) {
        self.val = val;
    }

    function barFunc() returns never {
        error e = error("Bad Sad!!");
        panic e;
    }
}

function testNeverWithIterator1() {
    map<never> x = {};
    record {| never value; |}? y = x.iterator().next();
    assertEquality((), y);
}

function testNeverWithIterator2() {
    map<never> x = {};
    var y = x.iterator().next();
    assertEquality((), y);
}

function testNeverWithIterator3() {
    never[] x = [];
    record {| never value; |}? y = x.iterator().next();
    assertEquality((), y);
}

function testNeverWithIterator4() {
    xml<never> x = xml ``;
    record {| never value; |}? y = x.iterator().next();
    assertEquality((), y);
}

type NeverTable table<map<never>>;

function testNeverWithIterator5() {
    NeverTable x = table [
    ];
    record {| map<never> value; |}? y = x.iterator().next();
    assertEquality((), y);
}

type Bunny record{|
    string name;
|};

type BunnyTable table<Bunny> key<never>;

function testNeverWithIterator6() {
    BunnyTable x = table [
            {"name": "ABC"},
            {"name": "DEF"}
    ];
    record {| Bunny value; |}? y = x.iterator().next();
    record {| Bunny value; |} z = {"value":{"name":"ABC"}};
    assertEquality(z, y);
}

function testNeverWithForeach1() {
    map<never> x = {};
    any y = "ABC";
    foreach never a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach2() {
    map<never> x = {};
    any y = "ABC";
    foreach var a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

type Foo record {|
|};

function testNeverWithForeach3() {
    Foo x = {};
    any y = "ABC";
    foreach never a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach4() {
    Foo x = {};
    any y = "ABC";
    foreach var a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach5() {
    never[] x = [];
    any y = "ABC";
    foreach never a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach6() {
    xml<never> x = xml ``;
    any y = "ABC";
    foreach never a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach7() {
    xml<never> x = <xml<never>> xml:createText("");
    any y = "ABC";
    foreach var a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach8() {
    NeverTable x = table [
    ];
    any y = "ABC";
    foreach var a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithFromClauseInQueryExpr1() {
    map<never> x = {};
    var y = from never a in x select a;
    assertEquality(0, y.length());
}

function testNeverWithFromClauseInQueryExpr2() {
    xml<never> x = xml ``;
    xml y = from never a in x select xml:concat(a);
    assertEquality(xml:concat(), y);
}

function testNeverWithFromClauseInQueryExpr3() {
    never[] x = [];
    never[] y = from never a in x select a;
    assertEquality(0, y.length());
}

function testNeverWithFromClauseInQueryExpr4() {
    never[] x = [];
    never[] y = [];
    never[] z = from var a in x
                join never b in y
                on a equals b
                select a;
    assertEquality(0, z.length());
}

function testNeverWithFromClauseInQueryExpr5() {
    NeverTable x = table [
    ];
    map<never>[] y = from var a in x select a;
    assertEquality(0, y.length());
}

type RestRecord record {|
    string someName;
    never...;
|};

function testNeverWithRestParamsAndFields() {
    RestRecord x = {someName:"ABC"};
    var y = testNeverWithRestParams({});
}

function testNeverWithRestParams(record {| never x; |}... rec) {
}

function testNeverWithServiceObjFunc() {
    service object {} object1 = service object {
        remote function invoke1(string a) returns never {
            error e = error(a);
            panic e;
        }
    };
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
