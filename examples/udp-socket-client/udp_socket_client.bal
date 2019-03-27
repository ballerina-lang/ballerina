// This is the client implementation for the UDP socket.
import ballerina/io;
import ballerina/socket;

public function main() {
    // Create a new socket client.
    // Optionally, you can provide port that this socket need to bind or
    // both interface and port as follows.
    // socket:UdpClient client = new(localAddress = { port: 48828 });
    // socket:UdpClient client = new(localAddress = { host: "localhost", port: 48828 });
    socket:UdpClient socketClient = new;
    string msg = "Hello from UDP client";
    byte[] c1 = msg.toByteArray("utf-8");
    // Send data to remote host.
    // Second parameter is the address of the remote host.
    var sendResult =
        socketClient->sendTo(c1, { host: "localhost", port: 48826 });
    if (sendResult is int) {
        io:println("Number of bytes written: ", sendResult);
    } else {
        panic sendResult;
    }
    // Wait until data receive from remote host.
    // This will block until receive at least a single byte.
    // Optionally, you can specify the length as below.
    // socketClient->receiveFrom(length = 30)
    // This will block until specified length of bytes receive from host.
    var result = socketClient->receiveFrom();
    if (result is (byte[], int, socket:Address)) {
        var (content, length, address) = result;
        io:ReadableByteChannel byteChannel =
            io:createReadableChannel(content);
        io:ReadableCharacterChannel characterChannel =
            new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var str = characterChannel.read(60);
        if (str is string) {
            io:println("Received: ", untaint str);
        } else {
            io:println("Error: ", str.detail().message);
        }
    } else {
        io:println("An error occured while receiving the data ",
            result);
    }
    // Close the client and release the bound port.
    var closeResult = socketClient->close();
    if (closeResult is error) {
        io:println("An error occured while closing the connection ",
            closeResult);
    }
}
