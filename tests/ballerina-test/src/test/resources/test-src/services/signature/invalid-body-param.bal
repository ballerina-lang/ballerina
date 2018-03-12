import ballerina.net.http;

@http:configuration {basePath:"/signature"}
service<http> echo {
    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource echo1 (http:Connection conn, http:Request req, string key, int person) {
        http:Response res = {};
        _ = conn.respond(res);
    }
}
