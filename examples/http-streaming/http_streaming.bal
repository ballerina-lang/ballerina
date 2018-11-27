import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

// Creating an endpoint for the client.
endpoint http:Client clientEndpoint {
    url: "http://localhost:9090"
};

@http:ServiceConfig { basePath: "/stream" }
service<http:Service> HTTPStreamingService bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/fileupload"
    }
    handleOutputStream(endpoint caller, http:Request clientRequest) {
        http:Request request = new;

        //Set the file as request payload.
        request.setFileAsPayload("./files/BallerinaLang.pdf",
            contentType = mime:APPLICATION_PDF);

        //Send the request to the client with file content.
        var response = clientEndpoint->post("/stream/receiver", request);

        http:Response res = new;
        match response {
            http:Response resp => {
                //Get the text payload received from the client endpoint.
                match resp.getTextPayload() {
                    string payload => {
                        //Set the response payload.
                        res.setTextPayload(untaint payload);

                    }
                    error err => {
                        log:printError(err.message, err = err);
                        setError(res, err.message);
                    }
                }
            }
            error err => { log:printError(err.message, err = err);
            setError(res, err.message);}
        }
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/receiver"
    }
    handleInputStream(endpoint caller, http:Request clientRequest) {
        http:Response response = new;

        //Get the payload as a byte channel.
        match clientRequest.getByteChannel() {
            io:ReadableByteChannel sourceChannel => {
                //Write the incoming stream to a file. First get the destination
                //channel by providing the file name the content should be
                //written to.
                io:WritableByteChannel destinationChannel =
                    io:openWritableFile("./files/ReceivedFile.pdf");

                try {
                    //Copy the incoming stream to the destination channel.
                    copy(sourceChannel, destinationChannel);

                    response.setTextPayload("File Received!");
                } catch (error err) {
                    log:printError("error occurred while saving file : "
                            + err.message);
                } finally {
                    var result = sourceChannel.close();
                    if (result is error) {
                       log:printError("Error closing sourceChannel", err = result);
                    }

                    result = destinationChannel.close();
                    if (result is error) {
                       log:printError("Error closing destinationChannel", err = result);
                    }
                }
            }
            error err => {
                setError(response, err.message);
            }
        }

        var result = caller->respond(response);
        if (result is error) {
           log:printError("Error sending response", err = result);
        }
    }
}

// This function reads a specified number of bytes from the given channel.
function readBytes(io:ReadableByteChannel byteChannel, int numberOfBytes)
    returns (byte[], int) {

    // Here is how the bytes are read from the channel.
    var result = byteChannel.read(numberOfBytes);
    match result {
        (byte[], int) content => {
            return content;
        }
        error readError => {
            throw readError;
        }
    }
}

// This function writes a byte content with the given offset to a channel.
function writeBytes(io:WritableByteChannel byteChannel, byte[] content, int startOffset = 0)
    returns (int) {

    // Here is how the bytes are written to the channel.
    var result = byteChannel.write(content, startOffset);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        error err => {
            throw err;
        }
    }
}

// This function copies content from the source channel to a
//destination channel.
function copy(io:ReadableByteChannel src, io:WritableByteChannel dst) {
    // Specifies the number of bytes that should be read from a
    //single read operation.
    int bytesChunk = 10000;
    
    int numberOfBytesWritten = 0;
    int readCount = 0;
    int offset = 0;
    byte[] readContent;
    boolean doneCopying = false;
    try {
        // Here is how to read all the content from
        // the source and copy it to the destination.
        while (!doneCopying) {
            (readContent, readCount) = readBytes(src, 1000);
            if (readCount <= 0) {
                //If no content is read, the loop is ended.
                doneCopying = true;
            }
            numberOfBytesWritten = writeBytes(dst, readContent);
        }
    } catch (error err) {
        throw err;
    }
}

function setError(http:Response response, string message) {
    response.setPayload(untaint message);
    response.statusCode = 500;
}
