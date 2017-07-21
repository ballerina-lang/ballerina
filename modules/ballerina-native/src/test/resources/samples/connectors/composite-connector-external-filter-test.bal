import ballerina.lang.system;

function testCompositeConnector()(string, string) {
    TestConnector t1 = create TestConnector("URI1");
    TestConnector t2 = create TestConnector("URI2");
    TestConnector[] tArray = [t1, t2];
    TestLBConnector testLB = create TestLBConnector (tArray, "RoundRobin") with FilterConnector("Filter");
    message request = {};
    string value1;
    string value2;
    value1 = TestLBConnector.action1(testLB, request);
    system:println("Value returned from action invocation is " + value1);
    value2 = TestLBConnector.action1(testLB, request);
    system:println("Value returned from action invocation is " + value2);
    return value1, value2;

}

connector FilterConnector<TestLBConnector t>(string param1) {

    action action1(FilterConnector testConnector, message msg) (string){
          system:println("Action1 in filter connector started " + param1);
          string x;
          x = TestLBConnector.action1(t, msg);
          system:println(x);
          string y;
          y = TestLBConnector.action2(t, msg);
          system:println(y);
          return x;
    }

    action action2(FilterConnector testConnector, message msg) (string) {
    	  system:println("Starting action 2 within filter connector");
          return "value within filter";
    }

}

connector TestConnector(string param1) {

    action action1(TestConnector testConnector, message msg) (string){
        system:println("Action1 in test connector started " + param1);
        return param1;
    }

    action action2(TestConnector testConnector, message msg) (string) {
        system:println("Starting action 2 " + param1);
        return "value from action2";
    }

}

connector TestLBConnector(TestConnector[] testConnectorArray, string algorithm) {

    int count = 0;

    action action1(TestLBConnector testConnector, message msg) (string){
        system:println("Action1 in LB connector started ");
        int index = count % testConnectorArray.length;
        TestConnector t1 = testConnectorArray[index];
        count = count + 1;
        string retValue = TestConnector.action1(t1, msg);
        return retValue;
    }

    action action2(TestLBConnector testConnector, message msg) (string) {
        system:println("Starting action 2 " + algorithm);
        return "value from action2";
    }
}
