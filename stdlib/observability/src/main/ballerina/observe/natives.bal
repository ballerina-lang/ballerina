// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

documentation {
    Start a span with no parent span.

    P{{spanName}} name of the span
    P{{tags}} tags to be associated to the span
    R{{spanId}} spanId of the started span
}
public native function startRootSpan(string spanName, map<string>? tags = ()) returns int;

documentation {
    Start a span and create child relationship to current active span or user specified span.

    P{{spanName}} name of the span
    P{{tags}} tags to be associated to the span
    P{{parentSpanId}} id of the parent span or -1 if parent span should be taken from system trace
    R{{spanId}} spanId of the started span
}
public native function startSpan(string spanName, map<string>? tags = (), int parentSpanId = -1) returns int|error;

documentation {
        Add a key value pair as a tag to the span.

        P{{spanId}} id of span to which the tags should be added
        P{{tagKey}} key of the tag
        P{{tagValue}} value of the tag
        R{{error}} An error if an error occured while attaching tag to the span
    }
public native function addTagToSpan(int spanId, string tagKey, string tagValue) returns error?;

documentation {
        Finish the current span.

        P{{spanId}} id of span to finish
    }
public native function finishSpan(int spanId) returns error?;
