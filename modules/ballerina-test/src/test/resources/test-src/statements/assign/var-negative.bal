error ex;

function test1 () {
    var x, y = (Foo) bar;
    string result1 = x + ex;
    string result2 = y + ex;
}

function test2(){
    int foo = 10;
    var x, y = (Float) foo;
    string result1 = x + ex;
    string result2 = y + ex;
}

function test3(){
    var x, y = (string) foo;
    string result1 = x + ex;
    string result2 = y + ex;
}

function test4 () {
    var x, y = <Foo> bar;
    string result1 = x + ex;
    string result2 = y + ex;
}

function test5(){
    int foo = 10;
    var x, y = <Float> foo;
    string result1 = x + ex;
    string result2 = y + ex;
}

function test6(){
    var x, y = <string> foo;
    string result1 = x + ex;
    string result2 = y + ex;
}

function test7 () {
    var p, q, r = (Float) fooo;
    var x = q + ex;
}

function test8 () {
    var p, q, r = (float) 10;
}
