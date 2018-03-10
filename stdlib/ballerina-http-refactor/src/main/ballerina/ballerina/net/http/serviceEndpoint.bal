package ballerina.net.http;

////////////////////////
/// Service Endpoint ///
////////////////////////
public struct ServiceEndpoint {
    // TODO : Make all field Read-Only
    string epName;
    ServiceEndpointConfiguration config;
}

@Description {value:"Configuration for HTTP service endpoint"}
@Field {value:"host: Host of the service"}
@Field {value:"port: Port number of the service"}
@Field {value:"httpsPort: HTTPS port number of service"}
@Field {value:"keyStoreFile: File path to keystore file"}
@Field {value:"keyStorePassword: The keystore password"}
@Field {value:"trustStoreFile: File path to truststore file"}
@Field {value:"trustStorePassword: The truststore password"}
@Field {value:"sslVerifyClient: The type of client certificate verification"}
@Field {value:"certPassword: The certificate password"}
@Field {value:"sslEnabledProtocols: SSL/TLS protocols to be enabled"}
@Field {value:"ciphers: List of ciphers to be used"}
@Field {value:"sslProtocol: The SSL protocol version"}
@Field {value:"validateCertEnabled: The status of validateCertEnabled {default value : false (disable)}"}
@Field {value:"cacheSize: Maximum size of the cache"}
@Field {value:"cacheValidityPeriod: Time duration of cache validity period"}
@Field {value:"exposeHeaders: The array of allowed headers which are exposed to the client"}
@Field {value:"keepAlive: The keepAlive behaviour of the connection for a particular port"}
@Field {value:"transferEncoding: The types of encoding applied to the response"}
@Field {value:"chunking: The chunking behaviour of the response"}
public struct ServiceEndpointConfiguration {
    string host;
    int port;
    KeepAlive keepAlive;
    TransferEncoding transferEncoding;
    Chunking chunking;
    SslConfiguration ssl;
}

public function <ServiceEndpointConfiguration config> ServiceEndpointConfiguration() {
    config.keepAlive = KeepAlive.AUTO;
    config.chunking = Chunking.AUTO;
    config.transferEncoding = TransferEncoding.CHUNKING;
}

public struct SslConfiguration {
    string keyStoreFile;
    string keyStorePassword;
    string trustStoreFile;
    string trustStorePassword;
    string sslVerifyClient;
    string certPassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
    boolean validateCertEnabled;
    int cacheSize;
    int cacheValidityPeriod;
}

public enum KeepAlive {
    AUTO, ALWAYS, NEVER
}


public enum Chunking {
    AUTO, ALWAYS, NEVER
}

public enum TransferEncoding {
    CHUNKING
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <ServiceEndpoint ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.epName = epName;
    ep.config = config;
    ep.initEndpoint();
}

public native function<ServiceEndpoint ep> initEndpoint();

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public native function <ServiceEndpoint ep> register (type serviceType);

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public native function <ServiceEndpoint ep> start ();

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public native function <ServiceEndpoint ep> getConnector () returns (ServerConnector repConn);

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public native function <ServiceEndpoint ep> stop ();


//////////////////////////////
/// Http Service Endpoint ///
/////////////////////////////
public struct HttpService {
    string epName;
    ServiceEndpointConfiguration config;
    ServiceEndpoint serviceEndpoint;
}

public function <HttpService ep> HttpService() {
    ep.serviceEndpoint = {};
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <HttpService ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.serviceEndpoint.init(epName, config);
}

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public function <HttpService ep> register (type serviceType) {
    ep.serviceEndpoint.register(serviceType);
}

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public function <HttpService ep> start () {
    ep.serviceEndpoint.start();
}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public function <HttpService ep> getConnector () returns (ServerConnector repConn) {
    return ep.serviceEndpoint.getConnector();
}

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public function <HttpService ep> stop () {
    ep.serviceEndpoint.stop();
}

//////////////////////////////////
/// WebSocket Service Endpoint ///
//////////////////////////////////
public struct WsService{
    string epName;
    ServiceEndpointConfiguration config;
    ServiceEndpoint serviceEndpoint;
}

public function <WsService ep> WsService() {
    ep.serviceEndpoint = {};
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <WsService ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.serviceEndpoint.init(epName, config);
}

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public function <WsService ep> register (type serviceType) {
    ep.serviceEndpoint.register(serviceType);
}

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public function <WsService ep> start () {
    ep.serviceEndpoint.start();
}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public function <WsService ep> getConnector () returns (ServerConnector repConn) {
    return ep.serviceEndpoint.getConnector();
}

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public function <WsService ep> stop () {
    ep.serviceEndpoint.stop();
}