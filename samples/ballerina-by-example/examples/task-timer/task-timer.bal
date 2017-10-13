import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function main (string[] args) {

    int schedulerTaskId;
    any schedulerError;
    int delay;
    int interval;
    any e1;
    any e2;
    if (args.length == 1) {
        interval, e2 = <int>args[0];
    } else if (args.length > 1) {
        delay, e1 = <int>args[0];
        interval, e2 = <int>args[1];
    }
    task:TimerScheduler ts = {delay:delay, interval:interval};
    function () returns (json) onTriggerFunction;
    onTriggerFunction = returnDummyMessage;
    function () returns (any) onErrorFunction;
    onErrorFunction = null;

    schedulerTaskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, ts);
    var timerSchedulerErrorMessage, castErrorTS = (string)schedulerError;
    if (timerSchedulerErrorMessage != "null" && timerSchedulerErrorMessage != "") {
        logger:error("Timer scheduling failed: " + timerSchedulerErrorMessage);
    }

    system:sleep(100000);
}

function returnDummyMessage () returns (json) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    json dummyJSON = {name:"Foo", address:"Bar"};
    logger:info("Sample JSON object is returned:");
    logger:info(dummyJSON);
    return dummyJSON;
}
