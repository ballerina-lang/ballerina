package ballerina.net.http;

import ballerina.mime;

@Description { value:"Represents the HTTP server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct Connection {
	string remoteHost;
	int port;
}

@Description { value:"Sends response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: A response message" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <Connection conn> respond (Response res) (HttpConnectorError);

@Description { value:"Gets the Session struct for a valid session cookie from the connection. Otherwise creates a new Session struct." }
@Param { value:"conn: The server connector connection" }
@Return { value:"HTTP Session struct" }
public native function <Connection conn> createSessionIfAbsent () (Session);

@Description { value:"Gets the Session struct from the connection if it is present" }
@Param { value:"conn: The server connector connection" }
@Return { value:"The HTTP Session struct assoicated with the request" }
public native function <Connection conn> getSession () (Session);

@Description { value:"Represents an HTTP request message"}
@Field {value:"path: Resource path of request URI"}
@Field {value:"method: HTTP request method"}
@Field {value:"httpVersion: The version of HTTP"}
@Field {value:"userAgent: User-Agent request header"}
@Field {value:"headers: Request headers"}
public struct Request {
	string path;
	string method;
	string httpVersion;
	string userAgent;
    string restUriPostFix;
}

@Description { value:"Get the entity from the request"}
@Param { value:"req: The request message" }
@Param { value:"isEntityBodyRequired: Whether the entity body should be populated" }
@Return { value:"Entity of the request" }
public native function <Request req> getEntity (boolean isEntityBodyRequired) (mime:Entity);

@Description { value:"Set the entity to request"}
@Param { value:"req: The request message" }
@Return { value:"Entity of the request" }
public native function <Request res> setEntity (mime:Entity entity);

@Description { value:"Gets the request URL from the request"}
@Param { value:"req: The request message" }
@Return { value:"The request URL value" }
public native function <Request req> getRequestURL () (string);

@Description { value:"Gets the HTTP method from the request"}
@Param { value:"req: A request message" }
@Return { value:"The HTTP request method associated with the request" }
public native function <Request req> getMethod () (string);

@Description { value:"Gets the query parameters from the HTTP request as a map"}
@Param { value:"req: The request message" }
@Return { value:"The map of query params" }
public native function <Request req> getQueryParams () (map);

@Description { value:"Sets a request property"}
@Param { value:"req: A request message" }
@Param { value:"propertyName: The name of the property" }
@Param { value:"propertyValue: The value of the property" }
public native function <Request req> setProperty (string propertyName, string propertyValue);

@Description { value:"Retrieves the named property from the request"}
@Param { value:"req: A request message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"The property value" }
public native function <Request req> getProperty (string propertyName) (string);

@Description { value:"Represents an HTTP response message"}
@Field {value:"statusCode: The response status code"}
@Field {value:"reasonPhrase: The status code reason phrase"}
@Field {value:"server: The server header"}
public struct Response {
    int statusCode;
    string reasonPhrase;
    string server;
    map headers;
}

@Description { value:"Get the entity from the response"}
@Param { value:"req: The response message" }
@Return { value:"Entity of the response" }
public native function <Response res> getEntity (boolean isEntityBodyRequired) (mime:Entity);

@Description { value:"Set the entity to response"}
@Param { value:"req: The response message" }
@Return { value:"Entity of the response" }
public native function <Response res> setEntity (mime:Entity entity);

@Description { value:"Gets the HTTP status code from the response"}
@Param { value:"res: The response message" }
@Return { value:"HTTP status code of the response" }
public native function <Response res> getStatusCode () (int);

@Description { value:"Sets the HTTP status code of the response"}
@Param { value:"res: The response message" }
@Param { value:"statusCode: HTTP status code" }
public native function <Response res> setStatusCode (int statusCode);

@Description { value:"Sets a custom HTTP Reason phrase"}
@Param { value:"res: The response message" }
@Param { value:"reasonPhrase: Reason phrase value" }
public native function <Response res> setReasonPhrase (string reasonPhrase);

@Description { value:"Sets a response property"}
@Param { value:"res: The response message" }
@Param { value:"propertyName: The name of the property" }
@Param { value:"propertyValue: The value of the property" }
public native function <Response res> setProperty (string propertyName, string propertyValue);

@Description { value:"Retrieve a response property"}
@Param { value:"res: The response message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"The property value" }
public native function <Response res> getProperty (string propertyName) (string);

@Description { value:"Represents an HTTP Session"}
public struct Session {
}

@Description { value:"Gets the named session attribute" }
@Param { value:"session: A Session struct" }
@Param { value:"attributeKey: HTTP session attribute key" }
@Return { value:"HTTP session attribute value" }
public native function <Session session> getAttribute (string attributeKey) (any);

@Description { value:"Sets the specified key/value pair as a session attribute" }
@Param { value:"session: A Session struct" }
@Param { value:"attributeKey: Session attribute key" }
@Param { value:"attributeValue: Session attribute Value" }
public native function <Session session> setAttribute (string attributeKey, any attributeValue);

@Description { value:"Gets the session attribute names" }
@Param { value:"session: A Session struct" }
@Return { value:"Session attribute names array" }
public native function <Session session> getAttributeNames () (string[]);

@Description { value:"Gets the session attribute key value pairs as a map" }
@Param { value:"session: A session struct" }
@Return { value:"The map of session attributes key value pairs" }
public native function <Session session> getAttributes () (map);

@Description { value:"Invalidates the session and it will no longer be accessible from the request" }
@Param { value:"session: A Session struct" }
public native function <Session session> invalidate ();

@Description { value:"Remove the named session attribute" }
@Param { value:"session: A Session struct" }
@Param { value:"attributeKey: Session attribute key" }
public native function <Session session> removeAttribute (string attributeKey);

@Description { value:"Gets the session cookie ID" }
@Param { value:"session: A Session struct" }
@Return { value:"Session ID" }
public native function <Session session> getId () (string);

@Description { value:"Checks whether the given session is a newly created session or an existing session" }
@Param { value:"session: A Session struct" }
@Return { value:"Indicates if the session is a newly created session or not" }
public native function <Session session> isNew () (boolean);

@Description { value:"Gets the session creation time" }
@Param { value:"session: A Session struct" }
@Return { value:"Session creation time" }
public native function <Session session> getCreationTime () (int);

@Description { value:"Gets the last time the sessions was accessed" }
@Param { value:"session: A Session struct" }
@Return { value:"Last accessed time of the session" }
public native function <Session session> getLastAccessedTime () (int);

@Description { value:"Gets maximum inactive interval for the session. The session expires after this time period." }
@Param { value:"session: A Session struct" }
@Return { value:"Session max inactive interval" }
public native function <Session session> getMaxInactiveInterval () (int);

@Description { value:"Sets the maximum inactive interval for the session. The session expires after this time period." }
@Param { value:"session: A Session struct" }
@Param { value:"timeInterval: Session max inactive interval" }
public native function <Session session> setMaxInactiveInterval (int timeInterval);

@Description { value:"HttpConnectorError struct represents an error occured during the HTTP client invocation" }
@Field {value:"msg:  An error message explaining about the error"}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown"}
@Field {value:"stackTrace: Represents the invocation stack when HttpConnectorError is thrown"}
@Field {value:"statusCode: HTTP status code"}
public struct HttpConnectorError {
	string msg;
	error cause;
	StackFrame[] stackTrace;
	int statusCode;
}

@Description { value:"Retry struct represents retry related options for HTTP client invocation" }
@Field {value:"count: Number of retries"}
@Field {value:"interval: Retry interval in millisecond"}
struct Retry {
    int count;
    int interval;
}

@Description { value:"SSL struct represents SSL/TLS options to be used for HTTP client invocation" }
@Field {value:"trustStoreFile: File path to trust store file"}
@Field {value:"trustStorePassword: Trust store password"}
@Field {value:"keyStoreFile: File path to key store file"}
@Field {value:"keyStorePassword: Key store password"}
@Field {value:"sslEnabledProtocols: SSL/TLS protocols to be enabled. eg: TLSv1,TLSv1.1,TLSv1.2"}
@Field {value:"ciphers: List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"}
@Field {value:"sslProtocol: SSL Protocol to be used. eg: TLS1.2"}
struct SSL {
    string trustStoreFile;
    string trustStorePassword;
    string keyStoreFile;
    string keyStorePassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
}

@Description { value:"FollowRedirects struct represents HTTP redirect related options to be used for HTTP client invocation" }
@Field {value:"enabled: Enable redirect"}
@Field {value:"maxCount: Maximun number of redirects to follow"}
struct FollowRedirects {
    boolean enabled = false;
    int maxCount = 5;
}

@Description { value:"Proxy struct represents proxy server configurations to be used for HTTP client invocation" }
@Field {value:"proxyHost: host name of the proxy server"}
@Field {value:"proxyPort: proxy server port"}
@Field {value:"proxyUserName: Proxy server user name"}
@Field {value:"proxyPassword: proxy server password"}
struct Proxy {
    string host;
    int port;
    string userName;
    string password;
}

@Description { value:"Options struct represents options to be used for HTTP client invocation" }
@Field {value:"port: Port number of the remote service"}
@Field {value:"endpointTimeout: Endpoint timeout value in millisecond"}
@Field {value:"enableChunking: Enable/disable chunking"}
@Field {value:"keepAlive: Keep the connection or close it (default value: true)"}
@Field {value:"httpVersion: The version of HTTP outbound request"}
@Field {value:"followRedirects: Redirect related options"}
@Field {value:"ssl: SSL/TLS related options"}
@Field {value:"retryConfig: Retry related options"}
@Field {value:"proxy: Proxy server related options"}
public struct Options {
    int port;
    int endpointTimeout = 60000;
    boolean enableChunking = true;
    boolean keepAlive = true;
	string httpVersion;
    FollowRedirects followRedirects;
    SSL ssl;
    Retry retryConfig;
    Proxy proxy;
}

@Description { value:"Http client connector for outbound HTTP requests"}
@Param { value:"serviceUri: Url of the service" }
@Param { value:"connectorOptions: connector options" }
public connector HttpClient (string serviceUri, Options connectorOptions) {

	@Description { value:"The POST action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action post (string path, Request req) (Response, HttpConnectorError);

	@Description { value:"The HEAD action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action head (string path, Request req) (Response, HttpConnectorError);

	@Description { value:"The PUT action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action put (string path, Request req) (Response, HttpConnectorError);

	@Description { value:"Invokes an HTTP call with the specified HTTP verb."}
	@Param { value:"HTTPVerb: HTTP verb value" }
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action execute (string HTTPVerb, string path, Request req) (Response, HttpConnectorError);

	@Description { value:"The PATCH action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action patch (string path, Request req) (Response, HttpConnectorError);

	@Description { value:"The DELETE action implementation of the HTTP connector"}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action delete (string path, Request req) (Response, HttpConnectorError);

	@Description { value:"GET action implementation of the HTTP Connector"}
	@Param { value:"path: Request path" }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action get (string path, Request req) (Response, HttpConnectorError);

	@Description { value:"OPTIONS action implementation of the HTTP Connector"}
	@Param { value:"path: Request path" }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action options (string path, Request req) (Response, HttpConnectorError);

	@Description { value:"forward action can be used to invoke an HTTP call with incoming request HTTPVerb"}
	@Param { value:"path: Request path" }
	@Param { value:"req: An HTTP Request struct" }
	@Return { value:"The response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action forward (string path, Request req) (Response, HttpConnectorError);
}