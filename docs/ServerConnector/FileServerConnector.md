#File Server Connector

File Server Connector is used to process files in the specified source directory. Note that files cannot remain in the source directory after processing or they will be processed again. Due to this reason, after processing a file, it will be deleted.

## How to define a File Service?

### Step 1: Defining a Service

Create a service with unique name.
```
service orderProcessService {

}
```

### Step 2: Specifying Service-Parameters

Add a service level annotation named "Source" and add the key-value pairs to specify the parameters. Following section describes the each key that can be used with a file service. An example is provided after following tables.

<table>
  <tr>
    <td>Key</td>
    <td>Description</td>
    <td>Required</td>
    <td>Expected Value</td>
    <td>Default value</td>
  </tr>
  <tr>
    <td>protocol</td>
    <td>The protocol to which this service is bound to.</td>
    <td>Yes</td>
    <td>file</td>
    <td>-</td>
  </tr>
  <tr>
    <td>fileURI</td>
    <td>The URI where the files you want to process are located. This can be a URI to a folder. When it is a folder, all the files in the folder will be processed, one at a time. When the URI points to a single file, once the file is available at that location, it will be processed.</td>
    <td>Yes</td>
    <td>A valid file URI</td>
    <td>-</td>
  </tr>
  <tr>
    <td>pollingInterval</td>
    <td>The polling interval at which the file URI location is polled for the file. The value is expressed in milli seconds.</td>
    <td>No</td>
    <td>A positive integer.</td>
    <td>10000</td>
  </tr>
  <tr>
    <td>acknowledgementTimeOut</td>
    <td>The expected time in milliseconds that a file needs to keep opened until the consumer has finished consuming it. When you develop the Ballerina service, if you are doing time consuming operations which requires the file to be kept open (for example streaming the file content to a slow backend), then this value needs to be set to a higher value accordingly.</td>
    <td>No</td>
    <td>A positive integer.</td>
    <td>30000</td>
  </tr>
</table>


When the fileURI parameter points to a folder, user has the option to sort the files, which are currently in the said folder, before processing starts. Following are the parameters which can be used to configure sorting operation.

<table>
  <tr>
    <td>Key</td>
    <td>Description</td>
    <td>Required</td>
    <td>Expected Value</td>
    <td>Default value</td>
  </tr>
  <tr>
    <td>fileSortAttribute</td>
    <td>The attribute of the file by which the files will be sorted.</td>
    <td>No</td>
    <td>One of the following:
name, size, lastModifiedTimestamp</td>
    <td>-</td>
  </tr>
  <tr>
    <td>fileSortAscending</td>
    <td>A boolean parameter which indicates whether to sort files in ascending order. If set to "true", files will be sorted in ascending order. If set to “false”, files will be sorted in descending order.</td>
    <td>No</td>
    <td>true or false</td>
    <td>true</td>
  </tr>
</table>


#### Example: 

```
@Source (
  protocol = "file",
  fileURI = "file:///home/user/orders",
  pollingInterval = "20000",
  fileSortAttribute = "Size",
  fileSortAscending = "false"
  )
service orderProcessService {
}
```

### Step 3: Adding a Resource

Add a resource under the File service as below:
```
@Source (
    protocol = "file",
    fileURI = "file:///home/user/orders",
    pollingInterval = "20000",
    fileSortAttribute = "size",
    fileSortAscending = "false"
    )
service orderProcessService { 
    resource processOrder (message m) {
        // file processing logic here.
    }
}
```

In general, a service may have multiple resources. However, a service of type File is **required to have one and only one service**. 

### Step 4: Adding File-processing Logic

Within the `resource` block, specify file processing logic. In the example given below, Ballerina functions (`system:println`, `messages:getStringPayload(m)` and `file:acknowledge(m)`) have being used to process the file.

```
import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.file;

@Source (
  protocol = "file",
  fileURI = "file:///home/user/orders",
  pollingInterval = "20000",
  fileSortAttribute = "size",
  fileSortAscending = "false"
  )
service orderProcessService {
    resource processOrder (message m) {
        system:println(messages:getStringPayload(m));
        file:acknowledge(m);
    }
}
```

**Note:**
Here, `file:acknowledge(m)` is a function which is exclusive for file processing. Refer following section for more information on the same function. 

## Step 5: Adding Dependency Jars

When the `fileURI` parameter refers to a location in the local file system, it is not required to add any additional jars for the file service to work.

However, in other cases (for example, when the `fileURI` refers to a file or a folder, located remotely that needs to be accessed via FTP) it may be required to add specific jars to <Ballerina_home>/bre/lib folder. 

Following table lists down which dependency-jars are required for which file-access protocol.



|  Dependency                | Required For                       |
 ----------------- | ----------------------------  
| [Commons Compress][1] Version 1.9. | TAR, Bzip2            | 
| [Commons Net][2] Version 3.3.           | FTP           | 
| [Commons Httpclient][3] Version 3.1. Requires [Commons Codec][4] Version 1.2.           | HTTP, URI Utils| 
| [JSch][5] Version 0.1.51.           |SFTP| 

## Native Ballerina Functions for File Processing

### Acknowledge

**Function name:** acknowledge

**Package name:** ballerina.net.file

**Arguments:** message

Acknowledge function sends an acknowledgement to the sender of the message, saying that file processing has being finished. 

It is important to note that this function **must** be called after the service has finished consuming the message (that is the file). See below for an example.

```
import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.file;

@Source (
  protocol = "file",
  fileURI = "file:///home/user/orders",
  pollingInterval = "20000",
  fileSortAttribute = "size",
  fileSortAscending = "false"
  )
service orderProcessService {
    resource processOrder (message m) {
        system:println(messages:getStringPayload(m));
        file:acknowledge(m);
    }
}
```

In the above example, once a file is found at the given URI,  orderProcessService will receive a message. This message contains a reference to the input stream of the file, so the service can process the file.

The service will then execute the statements given within the resource block. 

```system:println(messages:getStringPayload(m));```


Above line will read the file content as a String and then print it on the console.

```file:acknowledge(m);```


Above line sends an acknowledgment to the sender of the message (this sender has the control to close the file input stream). As a result, the file input stream will be closed; and then the file will be deleted. 

Since this function makes the message sender to close the input stream; and delete the file, this function needs to be called only after message processing is done. 

In case the service does not call the `acknowledge` function, the message sender will wait for 30 seconds (30 seconds is the default wait time. This value can be overridden by specifying a different value as the `acknowledgementTimeOut` service parameter) and will delete the file. 



[1]: http://commons.apache.org/compress/
[2]: http://commons.apache.org/net/
[3]: http://commons.apache.org/httpclient/
[4]: http://commons.apache.org/proper/commons-codec/
[5]: http://www.jcraft.com/jsch/