/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testutils;


import org.ballerinalang.runtime.ServerConnectorMessageHandler;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * This contains test utils related to Ballerina service invocations.
 *
 * @since 0.8.0
 */
public class Services {

    public static CarbonMessage invoke(CarbonMessage cMsg) {

        TestCallback callback = new TestCallback();
        ServerConnectorMessageHandler.handleInbound(cMsg, callback);

        return callback.getResponse();  // This will only work for blocking behaviour
    }

}
