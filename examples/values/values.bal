import ballerina/io;

// The types `int`, `float`, `decimal`, `string`, `boolean`, `byte` and `nil` are called simple basic types
// because they are basic types with only simple values. Simple values are always immutable.
public function main() {
    // The `int` type represents the set of 64-bit signed integers.
    // The implicit initial value of the `int` type is `0`.
    int i = 10;
    io:println(i);

    // The `float` type represents the set of double precision IEEE 754 floating point numbers.
    // The implicit initial value of the `float` type is `+0.0.`
    float f = 20.0;
    io:println(f);

    // `isNaN()`, `isInfinite()` and `isFinite()` builtin functions are supported for the `float` type.
    // `isNaN()` will return true if the `float` value is neither finite nor infinite.
    float nanVal = 0.0/0.0;
    io:println(nanVal.isNaN());

    // `isInfinite()` will return true if the `float` value is neither NaN nor finite.
    float infiniteVal = 12.0/0.0;
    io:println(infiniteVal.isInfinite());

    // `isFinite()` will return true if the `float` value is neither NaN nor infinite.
    float finiteVal = 6.0/3.0;
    io:println(finiteVal.isFinite());

    // The `decimal` type represents the set of 128-bits IEEE 754R decimal floating point numbers.
    // The implicit initial value of the `decimal` type is `+0.0.`
    decimal d = 27.5;
    io:println(d);

    // The `byte` type represents the set of 8-bit unsigned integers.
    // The implicit initial value of the `byte` type is `0`. Value space for `byte` is 0-255 both inclusive.
    byte c = 23;
    io:println(c);

    // The `string` type represents the set of sequences of Unicode code points.
    // The implicit initial value of the `string` type is `""` (empty string).
    string s = "Ballerina";
    io:println(s);

    // The `boolean` type has only two values named `true` and `false`.
    // The implicit initial value of the `boolean` type is `false`.
    boolean b = true;
    io:println(b);
}
