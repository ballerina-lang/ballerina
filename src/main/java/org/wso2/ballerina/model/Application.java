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

package org.wso2.ballerina.model;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Application} represents an application in Ballerina.
 *
 * Ballerina Application contains set of {@code BallerinaFile}s organized into {@code Package}s.
 */
public class Application {

    private String appName;
    private List<Package> packages = new ArrayList<Package>();

    /**
     *
     * @param appName Name of the application, not null
     */
    public Application(String appName) {
        if (appName == null) {
            //TODO: Throw Ballerina Deployment Exception
        }
        this.appName = appName;
    }

    /**
     * Add a {@code Package} to the application
     *
     * @param aPackage a Package
     */
    public void addPackage(Package aPackage) {
        packages.add(aPackage);
    }

    /**
     * Get list of {@code Packages}s in the Application
     *
     * @return Packages in the Application
     */
    public List<Package> getPackages() {
        return packages;
    }

    /**
     * Get the name of the Application
     *
     * @return name of the Application
     */
    public String getAppName() {
        return appName;
    }

}
