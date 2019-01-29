import ballerina/test;
import ballerina/http;

channel<string> serviceReply = new;
string msg = "hey";

@test:Config
function testText() {
    http:WebSocketClient wsClient = new("ws://localhost:9090/chat/bruce?age=30", config = {callbackService:callback});
    string wsReply = <- serviceReply;
    test:assertEquals(wsReply, "Hi bruce! You have successfully connected to the chat", msg = "Received message should be equal to the expected message");
    _ = wsClient->pushText(msg);
    wsReply = <- serviceReply;
    test:assertEquals(wsReply, "bruce: " + msg, msg = "Received message should be equal to the expected message");
}

service callback = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        text -> serviceReply;
    }
};
