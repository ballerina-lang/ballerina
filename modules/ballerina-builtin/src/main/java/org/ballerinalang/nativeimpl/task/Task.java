package org.ballerinalang.nativeimpl.task;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Task object
 */
public class Task {
    private ScheduledExecutorService executorService;
    private Long lifeTime;

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public Long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(Long lifeTime) {
        this.lifeTime = lifeTime;
    }
}
