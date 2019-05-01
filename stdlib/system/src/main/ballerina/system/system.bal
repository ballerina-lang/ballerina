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


# Returns the environment variable value associated with the provided name.
#
# + name - Name of the environment variable
# + return - Environment variable value if it exists, otherwise an empty string
public function getEnv(@sensitive string name) returns string = external;

# Returns the current working directory.
#
# + return - Current working directory or an empty string if the current working directory cannot be determined
public function getCurrentDirectory() returns string = external;

# Returns the current user's name.
#
# + return - Current user's name if it can be determined, an empty string otherwise
public function getUsername() returns string = external;

# Returns the current user's home directory path.
#
# + return - Current user's home directory if it can be determined, an empty string otherwise
public function getUserHome() returns string = external;

# Returns a random UUID string.
#
# + return - The random string
public function uuid() returns string = external;
