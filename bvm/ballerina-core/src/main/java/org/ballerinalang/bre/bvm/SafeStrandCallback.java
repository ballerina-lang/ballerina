/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.bvm.Strand.State;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BError;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Safe BVM callback implementation.
 *
 * @since 0.985.0
 */
public class SafeStrandCallback extends StrandCallback {
    private static PrintStream errStream = System.err;

    private volatile boolean done;

    private CallbackWaitHandler callbackWaitHandler;

    String[] sendIns;

    SafeStrandCallback(BType retType, WDChannels parentChannels) {
        super(retType);
        this.callbackWaitHandler = new CallbackWaitHandler();
        this.done = false;
        this.parentChannels = parentChannels;
    }

    @Override
    public void signal() {
        super.signal();
        try {
            this.callbackWaitHandler.dataLock.lock();
            this.done = true;
            if (this.getErrorVal() != null) {
                for (int i = 0; i < sendIns.length; i++) {
                    this.parentChannels.getWorkerDataChannel(sendIns[i]).setPanic(this.getErrorVal());
                }
            }
            if (getRefRetVal() != null && getRefRetVal().getType().getTag() == TypeTags.ERROR_TAG) {
                for (int i = 0; i < sendIns.length; i++) {
                    this.parentChannels.getWorkerDataChannel(sendIns[i]).setError(getRefRetVal());
                }
            }
            if (this.callbackWaitHandler.waitingStrand == null) {
                return;
            }
            if (this.callbackWaitHandler.waitingStrand.strandWaitHandler.waitCompleted) {
                return;
            }
            if (this.callbackWaitHandler.waitForAll) {
                List<WaitMultipleCallback> callbackList = new ArrayList<>();
                callbackList.add(new WaitMultipleCallback(this.callbackWaitHandler.keyReg, this));
                Strand resultStrand = WaitCallbackHandler.handleReturnInWaitMultiple(this.callbackWaitHandler
                                                                                             .waitingStrand,
                                                                                     this.callbackWaitHandler.retReg,
                                                                                     callbackList);
                if (resultStrand != null) {
                    BVMScheduler.stateChange(resultStrand, State.PAUSED, State.RUNNABLE);
                    BVMScheduler.schedule(resultStrand);
                }
                return;
            }
            Strand resultStrand = WaitCallbackHandler.handleReturnInWait(this.callbackWaitHandler.waitingStrand,
                                                                         this.callbackWaitHandler.expType,
                                                                         this.callbackWaitHandler.retReg, this);
            if (resultStrand != null) {
                BVMScheduler.stateChange(resultStrand, State.PAUSED, State.RUNNABLE);
                BVMScheduler.schedule(resultStrand);
            }
        } finally {
            this.callbackWaitHandler.dataLock.unlock();
        }
    }

    public void setError(BError error) {
        super.setError(error);
        //Printing current stack trace for strand callback
        //This will be printed regardless of the parent strand handling this error or not.
        //This may be improved to log the error only if parent strand doesn't handle it.
        errStream.println("error: " + BLangVMErrors.getPrintableStackTrace(error));
    }

    void acquireDataLock() {
        this.callbackWaitHandler.dataLock.lock();
    }

    void releaseDataLock() {
        this.callbackWaitHandler.dataLock.unlock();
    }

    void configureWaitHandler(Strand waitingStrand, boolean waitForAll, BType expType, int retReg, int keyReg) {
        this.callbackWaitHandler.waitingStrand = waitingStrand;
        this.callbackWaitHandler.waitForAll = waitForAll;
        this.callbackWaitHandler.expType = expType;
        this.callbackWaitHandler.retReg = retReg;
        this.callbackWaitHandler.keyReg = keyReg;
    }

    public boolean isDone() {
        return this.done;
    }

    /**
     * This class holds relevant data for callback wait handling related to callback side.
     */
    public static class CallbackWaitHandler {
        private Lock dataLock;
        Strand waitingStrand;
        boolean waitForAll;
        BType expType;
        int retReg;

        //WaitForAll
        int keyReg;

        public CallbackWaitHandler() {
            dataLock = new ReentrantLock();
        }
    }

    /**
     * This class holds relevant data for wait for all callbacks.
     */
    public static class WaitMultipleCallback {
        private int keyRegIndex;
        private SafeStrandCallback callback;

        public WaitMultipleCallback(int keyRegIndex, SafeStrandCallback callback) {
            this.keyRegIndex = keyRegIndex;
            this.callback = callback;
        }

        public int getKeyRegIndex() {
            return keyRegIndex;
        }

        public SafeStrandCallback getCallback() {
            return callback;
        }
    }

}
