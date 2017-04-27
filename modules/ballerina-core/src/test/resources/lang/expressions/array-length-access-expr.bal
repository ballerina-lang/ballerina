function arrayLengthAccessTestAssignmentCase(int x, int y) (int) {
    int kevin = 4;
    string chiranga = "name";
    if (kevin.type == chiranga.type) {
        return 1;
    } else {
        return 2;
    }
}



function arrayLengthAccessTestAssignmentCase2(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arr.length;
    return length;
}

function arrayLengthAccessTestFunctionInvocationCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arrayLength(arr.length);
    return length;
}


function arrayLength(int x) (int) {
    return x;
}

function arrayLengthAccessTestVariableDefinitionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = arrayLength(arr.length);
    return length;
}

function arrayLengthAccessTestArrayInitializerCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [arr.length,(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map tempMap = {"length":arr.length};
    int length = (int) tempMap["length"];
    return length;
}

function arrayLengthAccessTestReturnStatementCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return arr.length;
}

function arrayLengthAccessTestTypeCastExpressionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = (int) arr.length;
    return length;
}

function arrayLengthAccessTestIfConditionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if( arr.length == 3 ) {
       return 3;
    } else{
       return 0;
    }
}

function arrayLengthAccessTestBinaryExpressionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if( arr.length == arr.length ) {
       return 3;
    } else{
       return 0;
    }
}

