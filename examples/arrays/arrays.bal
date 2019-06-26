import ballerina/io;

public function main() {
    // Creates an `int` array of length 0.
    int[] a = [];
    io:println(a.length());

    // Creates an array specifying an array literal.
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    io:println(b[0]);
    io:println(b.length());

    // Unless the length is explicitly specified or is expected to be inferred, arrays are unbounded in length.
    // They can grow up to any length based on the given index.
    // In this example, the length of the array is 1000.
    b[999] = 23;
    io:println(b[999]);
    io:println(b.length());

    // Initializes a two dimensional `int` array.
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    io:println(iarray.length());
    io:println(iarray[0].length());

    // Initializes the outermost array to an empty array.
    iarray = [];
    // Adds a new array as the first element.
    int[] d = [9];
    iarray[0] = d;

    // Prints the first value of the two-dimensional array.
    io:println(iarray[0][0]);

    // Creates an `int` array with the fixed length of five.
    int[5] e = [1, 2, 3, 4, 5];
    io:println(e.length());

    // To infer the size of the array from the array literal, use the following syntax.
    // The length of the array is set to four here.
    int[*] g = [1, 2, 3, 4];
    io:println(g.length());
}
