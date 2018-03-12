/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.query.output.ratelimit.time;

import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.ballerinalang.siddhi.core.util.Schedulable;
import org.ballerinalang.siddhi.core.util.Scheduler;
import org.ballerinalang.siddhi.core.util.parser.SchedulerParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Implementation of {@link OutputRateLimiter} which will collect pre-defined time period and the emit only last
 * event.
 */
public class LastPerTimeOutputRateLimiter extends OutputRateLimiter implements Schedulable {
    private final Long value;
    private String id;
    private ComplexEvent lastEvent = null;
    private ScheduledExecutorService scheduledExecutorService;
    private Scheduler scheduler;
    private long scheduledTime;
    private String queryName;


    public LastPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService,
                                        String queryName) {
        this.queryName = queryName;
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public OutputRateLimiter clone(String key) {
        LastPerTimeOutputRateLimiter instance = new LastPerTimeOutputRateLimiter(id + key, value,
                scheduledExecutorService, queryName);
        instance.setLatencyTracker(latencyTracker);
        return instance;
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        ArrayList<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        complexEventChunk.reset();
        synchronized (this) {
            complexEventChunk.reset();
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    if (event.getTimestamp() >= scheduledTime) {
                        if (lastEvent != null) {
                            ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>
                                    (complexEventChunk.isBatch());
                            outputEventChunk.add(lastEvent);
                            lastEvent = null;
                            outputEventChunks.add(outputEventChunk);
                        }
                        scheduledTime = scheduledTime + value;
                        scheduler.notifyAt(scheduledTime);
                    }
                } else if (event.getType() == ComplexEvent.Type.CURRENT || event.getType() == ComplexEvent.Type
                        .EXPIRED) {
                    complexEventChunk.remove();
                    lastEvent = event;
                }
            }
        }
        for (ComplexEventChunk eventChunk : outputEventChunks) {
            sendToCallBacks(eventChunk);
        }
    }

    @Override
    public void start() {
        scheduler = SchedulerParser.parse(scheduledExecutorService, this, siddhiAppContext);
        scheduler.setStreamEventPool(new StreamEventPool(0, 0, 0, 5));
        scheduler.init(lockWrapper, queryName);
        long currentTime = System.currentTimeMillis();
        scheduledTime = currentTime + value;
        scheduler.notifyAt(scheduledTime);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (this) {
            state.put("LastEvent", lastEvent);
        }
        return state;
    }

    @Override
    public synchronized void restoreState(Map<String, Object> state) {
        lastEvent = (ComplexEvent) state.get("LastEvent");
    }

}
