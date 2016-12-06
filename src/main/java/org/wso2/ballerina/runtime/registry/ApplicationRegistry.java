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

package org.wso2.ballerina.runtime.registry;

import org.wso2.ballerina.model.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * The place where all Ballerina Applications are stored
 *
 * @since 1.0.0
 */
public class ApplicationRegistry {

    private Map<String, Application> applications = new HashMap<String, Application>();

    private static ApplicationRegistry instance = new ApplicationRegistry();

    private ApplicationRegistry() {}

    public ApplicationRegistry getInstance() {
        return instance;
    }

    public void registerApplication(Application application) {
        applications.put(application.getAppName(), application);

        // Notify source dispatchers
        application.getPackages().forEach(aPackage -> {
            aPackage.getServices().forEach(service -> {
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) -> {
                    dispatcher.serviceRegistered(service);
                });
            });
        });
    }

    public void unregisterApplication(Application application) {
        applications.remove(application.getAppName());

        // Notify source dispatchers
        application.getPackages().forEach(aPackage -> {
            aPackage.getServices().forEach(service -> {
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) -> {
                    dispatcher.serviceUnregistered(service);
                });
            });
        });
    }

    public Application getApplication(String appName) {
        return applications.get(appName);
    }

}
