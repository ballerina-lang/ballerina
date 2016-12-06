/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.runtime.core.dispatching;

import org.wso2.ballerina.model.Service;
import org.wso2.ballerina.runtime.core.BalCallback;
import org.wso2.ballerina.runtime.core.BalContext;

/**
 * {@code ServiceDispatcher} represents the service level dispatcher interface.
 * <p>
 * Need to have a protocol specific dispatcher
 */
public interface ServiceDispatcher {


    /**
     * Dispatch the message to a service
     *
     * @param context  Ballerina Context
     * @param callback Ballerina Callback
     * @return whether dispatching is successful or not
     */
    boolean dispatch(BalContext context, BalCallback callback);

    /**
     * Get the protocol of the dispatcher
     *
     * @return protocol
     */
    String getProtocol();

    /**
     * This is getting triggered when a new Service belongs to this protocol added to the Ballerina engine
     *
     * @param service Service
     */
    void serviceRegistered(Service service);

    /**
     * This is getting triggered when Service belongs to this protocol removed from Ballerina engine
     *
     * @param service Service
     */
    void serviceUnregistered(Service service);

}
