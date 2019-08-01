import ballerina/java;

function testAcceptNothingAndReturnNothing(handle receiver) {
    increaseCounterByOne(receiver);
}

function testInteropFunctionWithDifferentName(handle receiver) {
    interopFunctionWithDifferentName(receiver);
}

function testAcceptNothingButReturnSomething(handle receiver) returns handle {
    return getCounter(receiver);
}

function testAcceptSomethingButReturnNothing(handle receiver, handle h) {
    setCounterValue(receiver, h);
}

function testAcceptSomethingAndReturnSomething(handle receiver, handle h) returns handle {
    return setAndGetCounterValue(receiver, h);
}

function testAcceptTwoParamsAndReturnSomething(handle receiver, handle h1, handle h2) returns handle {
    return setTwiceAndGetCounterValue(receiver, h1, h2);
}



// Interop functions

public function increaseCounterByOne(handle receiver) = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function interopFunctionWithDifferentName(handle receiver) = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods",
    name:"increaseCounterByOne"
} external;

public function getCounter(handle receiver) returns handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setCounterValue(handle receiver, handle h) = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setAndGetCounterValue(handle receiver, handle h) returns handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setTwiceAndGetCounterValue(handle receiver, handle h1, handle h2) returns handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

