/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.filepath;

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

/**
 * A utility class for OS Path related tasks.
 *
 * @since 0.995.0
 */
public class Utils {

    static final String UNKNOWN_MESSAGE = "Unknown Error";
    static final String UNKNOWN_REASON = "UNKNOWN";

    /**
     * Returns error record for input reason.
     * Error type is generic ballerina error type. This utility to construct error struct from message.
     *
     * @param reason    Reason for creating the error object. If the reason is null, "UNKNOWN" sets by
     *                  default.
     * @param error     Java throwable object to capture description of error struct. If throwable object is null,
     *                  "Unknown Error" sets to message by default.
     * @return      Ballerina error object.
     */
    public static BError getPathError(String reason, Throwable error) {
        String errorMsg = error != null && error.getMessage() != null ? error.getMessage() : UNKNOWN_MESSAGE;
        return getPathError(reason, errorMsg);
    }

    /**
     * Returns error record for input reason and details.
     * Error type is generic ballerina error type. This utility to construct error struct from message.
     *
     * @param reason    Reason for creating the error object. If the reason is null, value "UNKNOWN" is set by
     *                  default.
     * @param details     Java throwable object to capture description of error struct. If throwable object is null,
     *                  "Unknown Error" is set to message by default.
     * @return      Ballerina error object.
     */
    public static BError getPathError(String reason, String details) {
        BMap<String, BValue> refData = new BMap<>(BTypes.typeError.detailType);
        if (reason != null) {
            reason = Constants.ERROR_REASON_PREFIX + reason;
        } else {
            reason = Constants.ERROR_REASON_PREFIX + UNKNOWN_REASON;
        }
        if (details != null) {
            refData.put("message", new BString(details));
        } else {
            refData.put("message", new BString(UNKNOWN_MESSAGE));
        }
        return new BError(BTypes.typeError, reason, refData);
    }
}
