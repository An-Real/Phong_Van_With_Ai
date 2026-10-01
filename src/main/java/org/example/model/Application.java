package org.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "applications")
public class Application {
    @Id
    private String id;
    private String candidateId; // Ai nộp?
    private String jobId;       // Nộp vào đâu?
    private String status;      // Trạng thái: "APPLIED", "ACCEPTED", "REJECTED"
    private LocalDateTime appliedAt; // Nộp lúc nào?

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public String getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
}