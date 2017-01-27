/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.launcher;

import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.http.server.HTTPErrorHandler;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.internal.ServiceContextHolder;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;
import org.wso2.carbon.messaging.handler.HandlerExecutor;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.sender.HTTPSender;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Set;

/**
 * Starts Ballerina services
 *
 * @since 0.8.0
 */
class BServiceRunner {

    private static PrintStream outStream = System.err;

    static void start(Path[] serviceFilePaths) {
        for (Path serviceFilePath : serviceFilePaths) {
            try {
                start(serviceFilePath);
            } catch (BLauncherException e) {
                outStream.println("error: fail to deploy service(s) in " + Utils.getFileName(serviceFilePath));
                Utils.printLauncherException(e, outStream);
                // Continuing service deployment
            }
        }

        // Starting http transport listener and sender
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        Set<TransportProperty> transportProperties = trpConfig.getTransportProperties();
        Set<SenderConfiguration> senderConfigurations = trpConfig.getSenderConfigurations();

        HTTPTransportContextHolder httpTransportContextHolder = HTTPTransportContextHolder.getInstance();
        httpTransportContextHolder.setHandlerExecutor(new HandlerExecutor());
        httpTransportContextHolder.setMessageProcessor(new MessageProcessor());

        HTTPTransportListener listener = new HTTPTransportListener(transportProperties, listenerConfigurations);

        HTTPSender sender = new HTTPSender(senderConfigurations, transportProperties);
        ServiceContextHolder.getInstance().addTransportSender(sender);
        ServiceContextHolder.getInstance().registerErrorHandler(new HTTPErrorHandler());

        listener.start();

        for (ListenerConfiguration listenerConfig : listenerConfigurations) {
            outStream.println("ballerina: started listener " +
                    listenerConfig.getScheme() + "-" + listenerConfig.getPort());
        }

        // TODO
        //outStream.println("ballerina: server startup in 500 ms");
    }

    private static void start(Path serviceFilePath) {
        BallerinaFile bFile = Utils.buildLangModel(serviceFilePath);
        String fileName = Utils.getFileName(serviceFilePath);

        if (bFile.getServices().size() == 0) {
            throw Utils.createLauncherException("error: no service(s) found in " + fileName);
        }

        try {
            // Create a runtime environment for this Ballerina application
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bFile);

            // Get the existing application associated with this ballerina config
            Application app = ApplicationRegistry.getInstance().getApplication(fileName);
            if (app == null) {
                // Create a new application with ballerina file name, if there is no app currently exists.
                app = new Application(fileName);
                app.setRuntimeEnv(runtimeEnv);
                ApplicationRegistry.getInstance().registerApplication(app);
            }

            Package aPackage = app.getPackage(fileName);
            if (aPackage == null) {
                // check if package name is null
                if (bFile.getPackageName() != null) {
                    aPackage = new Package(bFile.getPackageName());
                } else {
                    aPackage = new Package("default");
                }
                app.addPackage(aPackage);
            }
            aPackage.addFiles(bFile);

            // Here we need to link all the resources with this application. We execute the matching resource
            // when a request is made. At that point, we need to access runtime environment to execute the resource.
            for (Service service : bFile.getServices()) {
                for (Resource resource : service.getResources()) {
                    resource.setApplication(app);
                }
            }

            ApplicationRegistry.getInstance().updatePackage(aPackage);
            outStream.println("ballerina: deployed service(s) in " + Utils.getFileName(serviceFilePath));
        } catch (Throwable e) {
            throw Utils.createLauncherException(Utils.getFileName(serviceFilePath) + ": " +
                    Utils.makeFirstLetterUpperCase(e.getMessage()));
        }
    }
}
