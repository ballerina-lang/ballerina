import ballerina/system;

type AB A|B;

const A = "a";
const string B = "b";

final var name = "Ballerina";
final var age = 10;
final var val = A;

final AB a = A;

final string envVar = system:getEnv("env_var");
final string varFunc = dummyStringFunction();
final string str = "ballerina is $$$";
final string varNativeFunc = str.replace("$$$", "awesome");
final int varIntExpr = 10 + 10 + 10;
final string varConcat = envVar + varFunc + varNativeFunc;

function accessConstant() returns (string) {
    return envVar;
}

function accessConstantViaFunction() returns (string) {
    return varFunc;
}

function dummyStringFunction() returns (string) {
    return "dummy";
}

function accessConstantViaNativeFunction() returns (string) {
    return varNativeFunc;
}


function accessConstantEvalIntegerExpression() returns (int) {
    return varIntExpr;
}

function accessConstantEvalWithMultipleConst() returns (string) {
    return varConcat;
}
