package org.example.dto;

import org.example.model.Application;
import org.example.model.Job;

public class AppliedJobInfoDTO {

    private Application app;
    private Job job;

    public AppliedJobInfoDTO(
            Application app,
            Job job
    ) {
        this.app = app;
        this.job = job;
    }

    public Application getApp() {
        return app;
    }

    public void setApp(Application app) {
        this.app = app;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}