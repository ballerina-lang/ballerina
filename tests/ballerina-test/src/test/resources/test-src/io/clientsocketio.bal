import ballerina.io;

io:Socket socket;

function openSocketConnection (string host, int port) {
    socket, _ = io:openSocket(host, port, {});
}
function openSocketConnectionWithProps (string host, int port, io:SocketProperties prop) (io:Socket) {
    io:Socket socketProp;
    socketProp, _ = io:openSocket(host, port, prop);
    return socketProp;
}

function closeSocket () {
    _ = socket.closeSocket();
}

function write (byte[] content, int size) (int) {
    int numberOfBytesWritten;
    io:ByteChannel channel = socket.channel;
    numberOfBytesWritten, _ = channel.write(content, size, 0);
    return numberOfBytesWritten;
}

function read (int size) (byte[], int) {
    io:ByteChannel channel = socket.channel;
    byte[] readContent = [];
    int readSize;
    readSize, _ = channel.read(readContent, size, 0);
    return readContent, readSize;
}

function close (io:Socket socket) {
    _ = socket.closeSocket();
}
