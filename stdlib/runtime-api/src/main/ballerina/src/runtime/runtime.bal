// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# Halts the current strand for a predefined amount of time.
# ```ballerina
# runtime:sleep(1000);
# ```
#
# + millis - Amount of time to sleep in milliseconds
public function sleep(int millis) = @java:Method {
    class: "org.ballerinalang.stdlib.runtime.nativeimpl.Sleep"
} external;

# Represents the `runtime:Module`.
#
# + orgName - Module organization name
# + name - Module name
# + version - Module version
public type Module record {|
    string orgName;
    string name;
    string 'version;
|};

# Provides the current module information.
# ```ballerina
# runtime:Module module = runtime:getCurrentModule();
# ```
# + return - The `runtime:Module` instance
public function getCurrentModule() returns Module = @java:Method {
    class: "org.ballerinalang.stdlib.runtime.nativeimpl.GetCurrentModule",
    name: "getCurrentModule"
} external;

