
import ballerina/internal;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

@final int MAX_INT_VALUE = 2147483647;
@final string VERSION_REGEX = "(\\d+\\.)(\\d+\\.)(\\d+)";

documentation {
    This function pulls a package from ballerina central.

    P{{definedEndpoint}} Endpoint defined with the proxy configurations
    P{{url}} Url to be invoked
    P{{dirPath}} Path of the directory to save the pulled package
    P{{pkgPath}} Package path
    P{{fileSeparator}} File separator based on the operating system
    P{{terminalWidth}} Width of the terminal
    P{{versionRange}} Supported version range
    P{{isBuild}} Package pull for build or pull command
}
function pullPackage (http:Client definedEndpoint, string url, string dirPath, string pkgPath, string fileSeparator, 
                        string terminalWidth, string versionRange, boolean isBuild) {
    endpoint http:Client httpEndpoint = definedEndpoint;
    string fullPkgPath = pkgPath;
    string destDirPath = dirPath;
    http:Request req = new;
    req.addHeader("Accept-Encoding", "identity");

    http:Response httpResponse = new;
    var result = httpEndpoint -> get(untaint versionRange, message=req);

    match result {
        http:Response response => httpResponse = response;
        error e => {
            io:println(formatMessage("connection to the remote host failed : " + e.message, isBuild));
            return;
        }
    }

    http:Response res = new;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        io:println(formatMessage("remote registry failed for url :" + url, isBuild));
    } else if (statusCode != "200") {
        var jsonResponse = httpResponse.getJsonPayload();
        match jsonResponse {
            json resp => {
                string message = resp.message.toString();
                io:println(formatMessage(message, isBuild));
            }
            error err => {
                io:println(formatMessage("error occurred when pulling the package", isBuild));
            }
        }
    } else {
        string contentLengthHeader;
        int pkgSize = MAX_INT_VALUE;

        if (httpResponse.hasHeader("content-length")) {
            contentLengthHeader = httpResponse.getHeader("content-length");
            pkgSize = check <int> contentLengthHeader;
        } else {
            io:println(formatMessage("warning: package size information is missing from remote repository", isBuild));
        }

        io:ByteChannel sourceChannel = check (httpResponse.getByteChannel());

        string resolvedURI = httpResponse.resolvedRequestedURI;
        if (resolvedURI == "") {
            resolvedURI = url;
        }
        
        string [] uriParts = resolvedURI.split("/");
        string pkgVersion = uriParts[lengthof uriParts - 2];
        boolean valid = check pkgVersion.matches(VERSION_REGEX);

        if (valid) { 
            string pkgName = fullPkgPath.substring(fullPkgPath.lastIndexOf("/") + 1, fullPkgPath.length());
            string archiveFileName = pkgName + ".zip";

            fullPkgPath = fullPkgPath + ":" + pkgVersion;
            destDirPath = destDirPath + fileSeparator + pkgVersion;        
            string destArchivePath = destDirPath  + fileSeparator + archiveFileName;

            if (!createDirectories(destDirPath)) {
                internal:Path pkgArchivePath = new(destArchivePath);
                if (pkgArchivePath.exists()){
                    io:println(formatMessage("package already exists in the home repository", isBuild));
                    return;                              
                }        
            }

            io:ByteChannel destDirChannel = getFileChannel(destArchivePath, io:WRITE);
            string toAndFrom = " [central.ballerina.io -> home repo]";
            int rightMargin = 3;
            int width = (check <int> terminalWidth) - rightMargin;
            copy(pkgSize, sourceChannel, destDirChannel, fullPkgPath, toAndFrom, width, isBuild);
                                
            closeChannel(destDirChannel, isBuild);
            closeChannel(sourceChannel, isBuild);
        } else {
            io:println(formatMessage("package version could not be detected", isBuild));
        }
    }
}

documentation {
    This function will invoke the method to pull the package.
}
function main(string... args){
    http:Client httpEndpoint;
    string host = args[4];
    string port = args[5];
    boolean isBuild = <boolean>args[10];
    if (host != "" && port != "") {
        try {
          httpEndpoint = defineEndpointWithProxy(args[0], host, port, args[6], args[7]);
        } catch (error err) {
          io:println(formatMessage("failed to resolve host : " + host + " with port " + port, isBuild));
          return;
        }
    } else  if (host != "" || port != "") {
        io:println(formatMessage("both host and port should be provided to enable proxy", isBuild));     
        return;   
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[0]);
    }
    pullPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[8], args[9], isBuild);
}

documentation {
    This function defines an endpoint with proxy configurations.

    P{{url}} URL to be invoked
    P{{hostname}} Host name of the proxy
    P{{port}} Port of the proxy
    P{{username}} Username of the proxy
    P{{password}} Password of the proxy
    R{{}} Endpoint defined
}
function defineEndpointWithProxy (string url, string hostname, string port, string username, string password) returns http:Client{
    endpoint http:Client httpEndpoint {
        url: url,
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
        followRedirects: { enabled: true, maxCount: 5 },
        proxy : getProxyConfigurations(hostname, port, username, password)
    };
    return httpEndpoint;
}

documentation {
    This function defines an endpoint without proxy configurations.

    P{{url}} URL to be invoked
    R{{}} Endpoint defined
}
function defineEndpointWithoutProxy (string url) returns http:Client{
    endpoint http:Client httpEndpoint {
        url: url,
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
        followRedirects: { enabled: true, maxCount: 5 }
    };
    return httpEndpoint;
}

documentation {
    This function will get the file channel.

    P{{filePath}} File path
    P{{permission}} Permissions provided
    R{{}} `ByteChannel` of the file content
}
function getFileChannel (string filePath, io:Mode permission) returns (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(untaint filePath, permission);
    return channel;
}

documentation {
    This function will read the bytes from the byte channel.

    P{{channel}} Byte channel
    P{{numberOfBytes}} Number of bytes to be read
    R{{}} Bytes read as a blob along with the number of bytes read.
}
function readBytes (io:ByteChannel channel, int numberOfBytes) returns (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    (bytes, numberOfBytesRead) = check (channel.read(numberOfBytes));
    return (bytes, numberOfBytesRead);
}

documentation {
    This function will write the bytes from the byte channel.

    P{{channel}} Byte channel
    P{{content}} Content to be written as a blob
    P{{startOffset}} Offset
    R{{}} number of bytes written.
}
function writeBytes (io:ByteChannel channel, blob content, int startOffset) returns (int) {
    int numberOfBytesWritten = check (channel.write(content, startOffset));
    return numberOfBytesWritten;
}

documentation {
    This function will copy files from source to the destination path.

    P{{pkgSize}} Size of the package pulled
    P{{src}} Byte channel of the source file
    P{{dest}} Byte channel of the destination folder
    P{{fullPkgPath}} Full package path
    P{{toAndFrom}} Pulled package details
    P{{width}} Width of the terminal
    P{{isBuild}} Invoked for build or pull command
}
function copy (int pkgSize, io:ByteChannel src, io:ByteChannel dest, string fullPkgPath, string toAndFrom, int width, boolean isBuild) {
    int terminalWidth = width;
    if (isBuild) {
        int tabLength = 10;
        terminalWidth = terminalWidth - tabLength;
    }
    int bytesChunk = 8;
    blob readContent;
    int readCount = -1;
    float totalCount = 0.0;
    int numberOfBytesWritten = 0;
    string noOfBytesRead;
    string equals = "==========";
    string tabspaces = "          ";
    boolean completed = false;
    int rightMargin = 5;
    int totalVal = 10;
    int startVal = 0;
    int rightpadLength = terminalWidth - equals.length() - tabspaces.length() - rightMargin;
    try {
        while (!completed) {
            (readContent, readCount) = readBytes(src, bytesChunk);
            if (readCount <= startVal) {
                completed = true;
            }
            if (dest != null) {
                numberOfBytesWritten = writeBytes(dest, readContent, startVal);
            }
            totalCount = totalCount + readCount;
            float percentage = totalCount / pkgSize;
            noOfBytesRead = totalCount + "/" + pkgSize;
            string bar = equals.substring(startVal, <int> (percentage * totalVal));
            string spaces = tabspaces.substring(startVal, totalVal - <int>(percentage * totalVal));   
            string size = "[" + bar + ">" + spaces + "] " + <int>totalCount + "/" + pkgSize;            
            string msg = truncateString(fullPkgPath + toAndFrom, terminalWidth - size.length());
            io:print("\r" + formatMessage(rightPad(msg, rightpadLength) + size, isBuild));
        }
    } catch (error err) {
        io:println("");
    }
    io:println("\r" + formatMessage(rightPad(fullPkgPath + toAndFrom, terminalWidth), isBuild));
}

documentation {
    This function adds the right pad.

    P{{logMsg}} Log message to be printed
    P{{logMsgLength}} Length of the log message
    R{{}} The log message to be printed after adding the right pad
}
function rightPad (string logMsg, int logMsgLength) returns (string) {
    string msg = logMsg;
    int length = logMsgLength;
    int i = -1;
    length = length - msg.length();
    string char = " ";
    while (i < length) {
        msg = msg + char;
        i = i + 1;
    }
    return msg;
}

documentation {
    This function truncates the string.

    P{{text}} String to be truncated
    P{{maxSize}} Maximum size of the log message printed
    R{{}} Truncated string.
}
function truncateString (string text, int maxSize) returns (string) {
    int lengthOfText = text.length();
    if (lengthOfText > maxSize) {
        int endIndex = 3;
        if (maxSize > endIndex) {
            endIndex = maxSize - endIndex;
        }
        string truncatedStr = text.substring(0, endIndex);
        return truncatedStr + "…";
    }
    return text;
}

documentation {
    This function creates directories.

    P{{directoryPath}} Directory path to be created
    R{{}} If the directories were created or not
}
function createDirectories(string directoryPath) returns (boolean) {
    internal:Path dirPath = new(directoryPath);
    if (!dirPath.exists()){
        match dirPath.createDirectory() {
            () => {
                return true;
            }
            error => {
                return false;
            }
        }
    } else {
        return false;
    }
}

documentation {
    This function will close the byte channel.

    P{{channel}} Byte channel to be closed
    P{{isBuild}} Invoked for build or pull command
}
function closeChannel(io:ByteChannel channel, boolean isBuild) {
    match channel.close() {
        error channelCloseError => {
            io:println(formatMessage("Error occured while closing the channel: " + channelCloseError.message, isBuild));
        }
        () => return;
    }
}

documentation {
    This function formats the output printed to std out.

    P{{msg}} message to be formatted
    P{{isBuild}} If it is invoked for the build or pull command
    R{{}} Formatted output
}
function formatMessage(string msg, boolean isBuild) returns string {
    string formattedMsg = msg;
    if (isBuild) {
        formattedMsg = "\t" + formattedMsg;
    }
    return formattedMsg;
}
documentation {
    This function sets the proxy configurations for the endpoint.

    P{{hostName}} Host name of the proxy
    P{{port}} Port of the proxy
    P{{username}} Username of the proxy
    P{{password}} Password of the proxy
    R{{}} Proxy configurations for the endpoint
}
function getProxyConfigurations(string hostName, string port, string username, string password) returns http:ProxyConfig {
    int portInt = check <int> port;
    http:ProxyConfig proxy = { host : hostName, port : portInt , userName: username, password : password };
    return proxy;
}
