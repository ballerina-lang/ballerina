package ballerina.net.http;

@Description { value:"Represents an HTTP Session"}
public struct Session {
}

@Description {value:"Gets the Session struct for a valid session cookie from the request. Otherwise creates a new Session struct."}
@Param {value:"request: The request message"}
@Return {value:"HTTP Session struct"}
public native function <Request request> createSessionIfAbsent () (Session);

@Description {value:"Gets the Session struct from the request if it is present"}
@Param {value:"request: The request message"}
@Return {value:"The HTTP Session struct assoicated with the request"}
public native function <Request request> getSession () (Session);

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