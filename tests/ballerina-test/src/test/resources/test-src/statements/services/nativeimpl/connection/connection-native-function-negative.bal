import ballerina.net.http;

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/10"
    }
    resource echo10 (http:Connection conn, http:Request req) {
        http:Response resp = {};
        _ = conn.respond(null);
    }

    @http:resourceConfig {
        path:"/11"
    }
    resource echo11 (http:Connection conn, http:Request req) {
        http:Response resp = {};
        http:Connection connn = {};
        _ = connn.respond(resp);
    }

    @http:resourceConfig {
        path:"/20"
    }
    resource echo20 (http:Connection conn, http:Request req) {
        _ = conn.forward(null);
    }

    @http:resourceConfig {
        path:"/21"
    }
    resource echo21 (http:Connection conn, http:Request req) {
        http:Response resp = {};
        http:Connection connn = {};
        _ = connn.forward(resp);
    }

}
