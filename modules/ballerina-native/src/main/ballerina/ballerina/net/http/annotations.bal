package ballerina.net.http;

annotation GET attach resource {
}

annotation POST attach resource {
}

annotation Path attach resource {
    string value;
}

annotation BasePath attach service, resource {
    string value;
}

annotation PathParam attach parameter {
    string value;
}