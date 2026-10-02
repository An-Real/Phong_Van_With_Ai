package org.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "jobs")
public class Job {
    @Id
    private String id;
    private String employerId; // ID của công ty đăng bài

    private String title;
    private String roleType;
    private String location;
    private String workFormat;
    private String level;
    private String jobType;
    private int headcount;
    private String experienceRequired;
    private String educationRequired;
    private LocalDateTime createdAt; // Ngày tạo bài
    private String jobDescription;
    private String benefits;
    private String status = "OPEN";
    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setWorkFormat(String workFormat) {
        this.workFormat = workFormat;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public void setHeadcount(int headcount) {
        this.headcount = headcount;
    }

    public void setExperienceRequired(String experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    public void setEducationRequired(String educationRequired) {
        this.educationRequired = educationRequired;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public void setBenefits(String benefits) { this.benefits = benefits; }

    public void setStatus(String status) { this.status = status; }

    // Getters
    public String getId() {
        return id;
    }

    public String getEmployerId() {
        return employerId;
    }

    public String getTitle() {
        return title;
    }

    public String getRoleType() {
        return roleType;
    }

    public String getLocation() {
        return location;
    }

    public String getWorkFormat() {
        return workFormat;
    }

    public String getLevel() {
        return level;
    }

    public String getJobType() {
        return jobType;
    }

    public int getHeadcount() {
        return headcount;
    }

    public String getExperienceRequired() {
        return experienceRequired;
    }

    public String getEducationRequired() {
        return educationRequired;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getJobDescription() { return jobDescription; }

    public String getBenefits() { return benefits; }

    public String getStatus() { return status; }
}