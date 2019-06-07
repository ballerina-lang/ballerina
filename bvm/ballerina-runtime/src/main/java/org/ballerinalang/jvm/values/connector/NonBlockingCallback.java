/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jvm.values.connector;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The callback implementation to handle non-blocking function behaviour.
 *
 * @since 0.995.0
 */
public class NonBlockingCallback {

    private static final Logger log = LoggerFactory.getLogger(NonBlockingCallback.class);
    private final Strand strand;
    private Object returnValue = null;

    public NonBlockingCallback(Strand strand) {
        strand.yield = true;
        strand.blocked = true;
        strand.blockedOnExtern = true;
        this.strand = strand;
    }

    public void notifySuccess() {
        this.strand.scheduler.unblockStrand(strand);
    }

    public void notifyFailure(ErrorValue error) {
        this.returnValue = error;
        strand.setReturnValues(getReturnValue());
        this.strand.scheduler.unblockStrand(strand);
    }

    public void setReturnValues(Object returnValue) {
        this.returnValue = returnValue;
        //TODO : Replace following with callback.setReturnValues() once strand non-blocking support is given
        strand.setReturnValues(returnValue);
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
