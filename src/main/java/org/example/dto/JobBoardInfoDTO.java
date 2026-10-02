package org.example.dto;

import org.example.model.Application;
import org.example.model.Job;
import org.example.model.User;

public class JobBoardInfoDTO {
    private Job job;
    private User employer;
    private Application application; // Có thể null nếu chưa ứng tuyển

    public JobBoardInfoDTO(Job job, User employer, Application application) {
        this.job = job;
        this.employer = employer;
        this.application = application;
    }

    public Job getJob() { return job; }
    public User getEmployer() { return employer; }
    public Application getApplication() { return application; }
}