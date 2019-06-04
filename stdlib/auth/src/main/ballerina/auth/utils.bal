// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/encoding;
import ballerina/log;

# Constant for the auth error code.
public const AUTH_ERROR_CODE = "{ballerina/auth}AuthError";

# Constant for empty string.
const string EMPTY_STRING = "";

# Default charset to be used with password hashing.
public const string DEFAULT_CHARSET = "UTF-8";

# Prefix used to denote special configuration values.
public const string CONFIG_PREFIX = "@";

# Prefix used to denote that the config value is a SHA-256 hash.
public const string CONFIG_PREFIX_SHA256 = "@sha256:";

# Prefix used to denote that the config value is a SHA-384 hash.
public const string CONFIG_PREFIX_SHA384 = "@sha384:";

# Prefix used to denote that the config value is a SHA-512 hash.
public const string CONFIG_PREFIX_SHA512 = "@sha512:";

# Extracts the username and password from credential value.
#
# + credential - Credential value
# + return - A `string` tuple with the extracted username and password or `error` occurred while extracting credentials
public function extractUsernameAndPassword(string credential) returns (string, string)|error {
    string decodedHeaderValue = encoding:byteArrayToString(check encoding:decodeBase64(credential));
    string[] decodedCredentials = decodedHeaderValue.split(":");
    if (decodedCredentials.length() != 2) {
        return prepareError("Incorrect credential format. Format should be username:password");
    } else {
        return (decodedCredentials[0], decodedCredentials[1]);
    }
}

# Log, prepare and return the `error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `error` instance
function prepareError(string message, error? err = ()) returns error {
    log:printError(message, err = err);
    error preparedError = error(AUTH_ERROR_CODE, { message: message, reason: err.reason() });
    return preparedError;
}
