int[] data = [1, -3, 5, -30, 4, 11, 25, 10];
int sum = 0;
int negSum = 0;
string output;

function add(int i){
    sum = sum + i;
}

function addNeg(int i){
    negSum = negSum + i;
}

function concatInt(int index, int value){
    output = output + index + ":" + value + " ";
}

function concatFloat(int index, float value){
    output = output + index + ":" + value + " ";
}

function concatString(int index, string value){
    output = output + index + ":" + value + " ";
}

function concatBoolean(int index, boolean value){
    output = output + index + ":" + value + " ";
}

function concatJSON(int index, json value){
    output = output + index + ":" + value.toString() + " ";
}

struct person {
    string name;
    int age;
}

function concatPerson(int index, person value){
    output = output + index + ":" + "name=" +  value.name + ",age=" + value.age + " ";
}

function testIntArrayWithArityOne ()(int) {
    sum = 0;
    foreach i in data {
        add(i);
    }
    return sum;
}

function testIntArrayWithArityTwo ()(string) {
    int[] ldata = [1, -3, 5, -30, 4, 11, 25, 10];
    output = "";
    foreach i, v in ldata {
        concatInt(i, v);
    }
    return output;
}

function testIntArrayComplex ()(int, int, string) {
    int[] ldata = [1, -3, 5, -30, 4, 11, 25, 10];
    output = "";
    sum = 0;
    negSum = 0;
    foreach i, v in ldata {
        if(v > 0){
            add(v);
        } else {
            addNeg(v);
        }
        int x = 0;
        while (x < i) {
            concatInt(i,v);
            x = x + 1;
        }
    }
    return sum, negSum, output;
}

function testFloatArrayWithArityOne()(string){
    float[] fdata = [1.123, -3.35244, 5.23, -30.45, 4.32, 11.56, 25.967, 10.345];
    output = "";
    foreach v in fdata {
        concatFloat(0, v);
    }
    return output;
}

function testFloatArrayWithArityTwo()(string){
    float[] fdata = [1.123, -3.35244, 5.23, -30.45, 4.32, 11.56, 25.967, 10.345];
    output = "";
    foreach i, v in fdata {
        concatFloat(i, v);
    }
    return output;
}

function testStringArrayWithArityOne()(string){
    string[] sdata = ["foo", "bar", "bax" , "baz"];
    output = "";
    foreach v in sdata {
        concatString(0, v);
    }
    return output;
}

function testStringArrayWithArityTwo()(string){
    string[] sdata = ["foo", "bar", "bax" , "baz"];
    output = "";
    foreach i, v in sdata {
        concatString(i, v);
    }
    return output;
}

function testBooleanArrayWithArityOne()(string){
    boolean[] bdata = [true, false, false, false, true, false];
    output = "";
    foreach v in bdata {
        concatBoolean(0, v);
    }
    return output;
}

function testBooleanArrayWithArityTwo()(string){
    boolean[] bdata = [true, false, false, false, true, false];
    output = "";
    foreach i, v in bdata {
        concatBoolean(i, v);
    }
    return output;
}

function testJSONArrayWithArityOne()(string){
    json[] jdata = [{ name : "bob", age : 10}, { name : "tom", age : 16}];
    output = "";
    foreach v in jdata {
        concatJSON(0, v);
    }
    return output;
}

function testJSONArrayWithArityTwo()(string){
    json[] jdata = [{ name : "bob", age : 10}, { name : "tom", age : 16}];
    output = "";
    foreach i, v in jdata {
        concatJSON(i, v);
    }
    return output;
}

function testStructArrayWithArityOne()(string){
    person bob = { name : "bob", age : 10};
    person tom = { name : "tom", age : 16};
    person[] tdata = [bob, tom];
    output = "";
    foreach v in tdata {
        concatPerson(0, v);
    }
    return output;
}

function testStructArrayWithArityTwo()(string){
    person bob = { name : "bob", age : 10};
    person tom = { name : "tom", age : 16};
    person[] tdata = [bob, tom];
    output = "";
    foreach i, v in tdata {
        concatPerson(i, v);
    }
    return output;
}
