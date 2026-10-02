package org.example.dto;

import org.example.model.Application;
import org.example.model.Job;
import org.example.model.User;

public class AppliedJobInfoDTO {
    private Application app;
    private Job job;
    private User employer;

    public AppliedJobInfoDTO(Application app, Job job, User employer) {
        this.app = app;
        this.job = job;
        this.employer = employer;
    }

    public Application getApp() { return app; }

    public void setApp(Application app) {
        this.app = app;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public User getEmployer() { return employer; }

    public void setEmployer(User employer) { this.employer = employer; }
}