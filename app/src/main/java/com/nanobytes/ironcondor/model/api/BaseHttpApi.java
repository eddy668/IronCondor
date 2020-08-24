package com.nanobytes.ironcondor.model.api;

import java.util.concurrent.atomic.AtomicInteger;

public class BaseHttpApi {
    private AtomicInteger processes_in_progress;

    protected BaseHttpApi() {
        this.processes_in_progress = new AtomicInteger(0);
    }

    public void wait_until_done() {
        //noinspection StatementWithEmptyBody
        while(!is_done());
    }

    private boolean is_done() {
        return this.processes_in_progress.get() <= 0;
    }

    protected void finished_a_process() {
        this.processes_in_progress.decrementAndGet();
    }

    protected void started_a_process() {
        this.processes_in_progress.incrementAndGet();
    }
}
