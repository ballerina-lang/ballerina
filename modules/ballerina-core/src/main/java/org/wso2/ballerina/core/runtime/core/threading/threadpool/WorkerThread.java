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

package org.wso2.ballerina.core.runtime.core.threading.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.errors.handler.DefaultErrorHandler;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandler;
import org.wso2.ballerina.core.runtime.internal.ServiceContextHolder;

/**
 * Worker Thread which is executable through the worker pool
 */
public abstract class WorkerThread implements Runnable {

    protected Context context;
    protected BalCallback callback;

    private static final Logger log = LoggerFactory.getLogger(WorkerThread.class);

    public WorkerThread(Context context, BalCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public Context getContext() {
        return context;
    }

    public BalCallback getCallback() {
        return callback;
    }

    protected void handleError(Throwable throwable) {
        log.error("Error while executing ballerina program. " + throwable.getMessage());

        ErrorHandler errorHandler;
        Object protocol = context.getProperty(Constants.PROTOCOL);
        if (protocol != null) {
            errorHandler = ServiceContextHolder.getInstance().getErrorHandler((String) protocol);
        } else {
            errorHandler = DefaultErrorHandler.getInstance();
        }
        errorHandler.handleError(new Exception(throwable.getMessage(), throwable.getCause()), context, callback);
    }

}
