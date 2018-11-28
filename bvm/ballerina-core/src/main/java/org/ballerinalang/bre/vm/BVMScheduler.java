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
package org.ballerinalang.bre.vm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.bre.vm.Strand.State;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangVMUtils;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * This represents the Ballerina worker scheduling functionality.
 *
 * @since 0.985.0
 */
public class BVMScheduler {

    private static AtomicInteger strandCount = new AtomicInteger(0);

    //TODO these are static vars, we may need to find a way to make this instance vars
    private static Semaphore strandsDoneSemaphore = new Semaphore(1);

    /**
     * Method to schedule a strand for execution.
     *
     * @param strand to be executed
     */
    public static void schedule(Strand strand) {
        ThreadPoolFactory.getInstance().getWorkerExecutor().submit(new CallableExecutor(strand));
    }

    /**
     * Method to execute the strand in the current thread.
     *
     * @param strand to be executed
     */
    public static void execute(Strand strand) {
        try {
            strandCountUp();
            BVM.execute(strand);
        } finally {
            //TODO Ideally we shouldn't need to handle errors or finally here. Remove if possible
            strandCountDown();
        }
    }

    /**
     * Method to schedule native call execution.
     *
     * @param nativeCallable    to be invoked
     * @param nativeCtx         to be used
     * @param callback          for notifications
     */
    public static void scheduleNative(NativeCallableUnit nativeCallable,
                                      Context nativeCtx, CallableUnitCallback callback) {
        ThreadPoolFactory.getInstance().getWorkerExecutor()
                .submit(new NativeCallableExecutor(nativeCallable, nativeCtx, callback));
    }

    /**
     * Method to execute native call in the current thread.
     *
     * @param nativeCallable    to be executed
     * @param nativeCtx         to be used
     * @param callback          for notifications
     */
    public static void executeNative(NativeCallableUnit nativeCallable,
                                     Context nativeCtx, CallableUnitCallback callback) {
        try {
            strandCountUp();
            nativeCallable.execute(nativeCtx, callback);
        } finally {
            //TODO Ideally we shouldn't need to handle errors or finally here. Remove if possible
            strandCountDown();
        }
    }


    /**
     * Method to change states of the strands.
     *
     * @param strand            to be changed
     * @param expectedStates    current states
     * @param newState          new state
     */
    public static void stateChange(Strand strand, List<State> expectedStates, State newState) {
        if (expectedStates == null || expectedStates.contains(strand.state)) {
            strand.state = newState;
            return;
        }
        throw new BLangRuntimeException("error: invalid strand state, expected "
                + expectedStates.toString() + " found - " + strand.state.toString()); //TODO error message?
    }

    /**
     * Method to change states of the strands.
     *
     * @param strand            to be changed
     * @param expectedState    current states
     * @param newState          new state
     */
    public static void stateChange(Strand strand, State expectedState, State newState) {
        if (expectedState == strand.state) {
            strand.state = newState;
            return;
        }
        throw new BLangRuntimeException("error: invalid strand state, expected "
                + expectedState + " found - " + strand.state.toString()); //TODO error message?
    }


    private static void strandCountUp() {
        if (strandCount.incrementAndGet() == 1) {
            try {
                strandsDoneSemaphore.acquire();
            } catch (InterruptedException e) {
                /* ignore */
            }
        }
    }

    private static void strandCountDown() {
        if (strandCount.decrementAndGet() == 0) {
            strandsDoneSemaphore.release();
        }
    }

    /**
     * Method to be used for all strand's execution completion.
     */
    public static void waitForStrandCompletion() {
        try {
            strandsDoneSemaphore.acquire();
            strandsDoneSemaphore.release();
        } catch (InterruptedException e) {
            /* ignore */
        }
    }

    /**
     * This represents the thread used to execute a runnable worker.
     */
    private static class CallableExecutor implements Runnable {

        private Strand strand;

        public CallableExecutor(Strand strand) {
            this.strand = strand;
        }

        @Override
        public void run() {
            try {
                strandCountUp();
                BVM.execute(this.strand);
            } finally {
                //TODO Ideally we shouldn't need to handle errors or finally here. Remove if possible
                strandCountDown();
            }
        }

    }

    /**
     * This represents the thread used to run a blocking native call in async mode.
     */
    private static class NativeCallableExecutor implements Runnable {

        private NativeCallableUnit nativeCallable;

        private Context nativeCtx;

        private CallableUnitCallback callback;

        public NativeCallableExecutor(NativeCallableUnit nativeCallable,
                                      Context nativeCtx, CallableUnitCallback callback) {
            this.nativeCallable = nativeCallable;
            this.nativeCtx = nativeCtx;
            this.callback = callback;
        }

        @Override
        public void run() {
            strandCountUp();
            BError error;
            Strand strand = this.nativeCtx.getStrand();
            CallableUnitInfo cui = this.nativeCtx.getCallableUnitInfo();
            BType retType = cui.getRetParamTypes()[0];
            try {
                this.nativeCallable.execute(this.nativeCtx, callback);
                if (strand.fp > 0) {
                    strand.popFrame();
                    StackFrame retFrame = strand.currentFrame;
                    BLangVMUtils.populateWorkerDataWithValues(retFrame, this.nativeCtx.getDataFrame().retReg,
                            this.nativeCtx.getReturnValue(), retType);
                    execute(strand);
                    return;
                }
                strand.respCallback.signal();
                return;
            } catch (BLangNullReferenceException e) {
                error = BLangVMErrors.createNullRefException(this.nativeCtx.getStrand());
            } catch (Throwable e) {
                error = BLangVMErrors.createError(this.nativeCtx.getStrand(), e.getMessage());
            } finally {
                strandCountDown();
            }
            strand.setError(error);
            strand.popFrame();
            BVM.handleError(strand);
            execute(strand);
        }

    }

    /**
     * This class represents the scheduler statistics.
     */
    public static class SchedulerStats {

        private LongAdder[] stateCounts;

        public SchedulerStats() {
            this.stateCounts = new LongAdder[6];
            for (int i = 0; i < this.stateCounts.length; i++) {
                this.stateCounts[i] = new LongAdder();
            }
        }

        public long getReadyWorkerCount() {
            return this.stateCounts[0].longValue();
        }

        public long getRunningWorkerCount() {
            return this.stateCounts[1].longValue();
        }

        public long getExceptedWorkerCount() {
            return this.stateCounts[2].longValue();
        }

        public long getWaitingForResponseWorkerCount() {
            return this.stateCounts[3].longValue();
        }

        public long getPausedWorkerCount() {
            return this.stateCounts[4].longValue();
        }

        public long getWaitingForLockWorkerCount() {
            return this.stateCounts[5].longValue();
        }

        public void stateTransition(WorkerExecutionContext currentCtx, WorkerState newState) {
//            if (!schedulerStatsEnabled || currentCtx.isRootContext()) {
//                return;
//            }
//            WorkerState oldState = currentCtx.state;
//            /* we are not considering CREATED state */
//            if (oldState != WorkerState.CREATED) {
//                this.stateCounts[oldState.ordinal()].decrement();
//            }
//            /* we are not counting the DONE state, since it is an ever increasing value */
//            if (newState != WorkerState.DONE) {
//                this.stateCounts[newState.ordinal()].increment();
//            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Worker Status:- \n");
            builder.append("\tREADY: " + this.getReadyWorkerCount() + "\n");
            builder.append("\tRUNNING: " + this.getRunningWorkerCount() + "\n");
            builder.append("\tEXCEPTED: " + this.getExceptedWorkerCount() + "\n");
            builder.append("\tWAITING FOR RESPONSE: " + this.getWaitingForResponseWorkerCount() + "\n");
            builder.append("\tPAUSED: " + this.getPausedWorkerCount() + "\n");
            builder.append("\tWAITING FOR LOCK: " + this.getWaitingForLockWorkerCount() + "\n");
            return builder.toString();
        }

    }

}
