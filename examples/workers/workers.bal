import ballerina/io;

// In Ballerina, each function consists of one or more workers, which are 
// independent parallel execution paths called strands. If explicit workers are not 
// mentioned with worker blocks, the function code will belong to a single implicit
// default worker. The default worker in each function wil be excuteced in the same 
// strand as the caller function.
public function main() {
    io:println("Worker execution started");
    worker w1 {
        // Calculate sum(n)
        int n = 10000000;
        int sum = 0;
        foreach var i in 1...n {
            sum += i;
        }
        io:println("sum of first ", n, " positive numbers = ", sum);
    }
    worker w2 {
        // Calculate sum(n^2)
        int n = 10000000;
        int sum = 0;
        foreach var i in 1...n {
            sum += i * i;
        }
        io:println("sum of squares of first ", n,
            " positive numbers = ", sum);
    }

    // Wait for both workers to finish
    _ = wait {w1, w2};

    io:println("Worker execution finished");
}
