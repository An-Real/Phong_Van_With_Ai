package org.example.dto;

import org.example.model.Application;
import org.example.model.Job;
import org.example.model.User;

public class ApplicantInfoDTO {

    private Application app;
    private Job job;
    private User candidate;

    public ApplicantInfoDTO(
            Application app,
            Job job,
            User candidate
    ) {
        this.app = app;
        this.job = job;
        this.candidate = candidate;
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

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }
}