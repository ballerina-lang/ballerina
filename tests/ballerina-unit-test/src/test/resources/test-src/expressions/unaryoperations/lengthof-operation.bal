function arrayLengthAccessTestAssignmentCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal;
    lengthVal = (lengthof arr);
    return lengthVal;
}

function arrayLengthAccessTestFunctionInvocationCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal;
    lengthVal = arrayLength(lengthof arr);
    return lengthVal;
}

function arrayLength (int x) returns (int) {
    return x;
}

function arrayLengthAccessTestVariableDefinitionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal = arrayLength(lengthof arr);
    return lengthVal;
}

function arrayLengthAccessTestArrayInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [(lengthof arr),(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map tempMap = {"length":(lengthof arr)};
    int lengthVal;
    lengthVal =check <int> tempMap.length;
    return lengthVal;
}

function arrayLengthAccessTestReturnStatementCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return (lengthof arr);
}

function arrayLengthAccessTestMultiReturnStatementCase (int x, int y) returns (int,int,int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] brr = [];
    brr[0] = 1;
    int[] crr = [];
    crr[0] = 1;
    crr[1] = x + y;
    return ((lengthof arr), (lengthof brr), (lengthof crr));
}

function arrayLengthAccessTestTypeCastExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal = <int> (lengthof arr);
    return lengthVal;
}

function arrayLengthAccessTestIfConditionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if ((lengthof arr) == 3) {
       return 3;
    } else{
       return 0;
    }
}

function arrayLengthAccessTestBinaryExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if ((lengthof arr) == (lengthof arr)) {
       return 3;
    } else {
       return 0;
    }
}

function arrayLengthAccessTestStructFieldAccessCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    Person jack = {name:"Jack", days:arr};

    if ((lengthof jack.days) == 3) {
        return 3;
    } else {
        return 0;
    }
}

type Person record {
    string name;
    int[] days;
};

function arrayLengthAccessTestJSONArrayCase (int x, int y) returns (int) {
    json arr = [x,y,5,5,6,6];
    int lengthVal;
    lengthVal = (lengthof arr);
    return lengthVal;
}

function lengthOfMap (int x, int y) returns (int) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    int lengthVal = lengthof namesMap;
    return lengthVal;
}

function lengthOfMapEmpty (int x, int y) returns (int) {
    map namesMap;
    int lengthVal = lengthof namesMap;
    return lengthVal;
}

function lengthOfString() returns (int, int, int) {
    string foo = "hello world";
    int l1 = lengthof foo;
    int l2 = lengthof "John";
    int l3 = lengthof string `Hello {{"John"}}`;
    return (l1, l2, l3);
}

function lengthOfBlob() returns (int, int) {
    string s1 = "Hello";
	byte[] b1 = s1.toByteArray("UTF-8");
    int l1 = lengthof b1;
    
    string s2 = "";
    byte[] b2 = s2.toByteArray("UTF-8");
    int l2 = lengthof b2;
    
    return (l1, l2);
}

function lengthOfNullString() returns (int) {
    string foo;
    return lengthof foo;
}

function lengthOfJSONObject() returns (int) {
    json j = {"a":"A", "b":"B"};
    return lengthof j;
}
