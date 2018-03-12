import ballerina.net.http;
import ballerina.io;

struct Student {
    string Name;
    int Grade;
    map Marks;
}

service<http> hello {

    @Description {value:"Body annotation represents the entity body of the inbound request."}
    @http:resourceConfig {
        methods:["POST"],
        body:"order"
    }
    resource bindJson (http:Connection conn, http:Request req, json order) {
        //Access JSON field values
        json details = order.Details;
        io:println(details);

        http:Response res = {};
        res.setJsonPayload(details);
        _ = conn.respond(res);
    }

    @Description {value:"Bind xml payload of the inbound request to variable store."}
    @http:resourceConfig {
        methods:["POST"],
        body:"store",
        consumes:["application/xml"]
    }
    resource bindXML (http:Connection conn, http:Request req, xml store) {
        //Access XML content.
        xml city = store.selectChildren("city");
        io:println(city);

        http:Response res = {};
        res.setXmlPayload(city);
        _ = conn.respond(res);
    }

    @Description {value:"Bind JSON payload into a custom struct. Payload content should match with struct."}
    @http:resourceConfig {
        methods:["POST"],
        body:"student",
        consumes:["application/json"]
    }
    resource bindStruct (http:Connection conn, http:Request req, Student student) {
        //Access Student struct fields
        string name = student.Name;
        io:println(name);

        int grade = student.Grade;
        io:println(grade);

        map marks = student.Marks;
        io:println(marks);

        http:Response res = {};
        res.setJsonPayload({Name:name, Grade:grade});
        _ = conn.respond(res);
    }
}
