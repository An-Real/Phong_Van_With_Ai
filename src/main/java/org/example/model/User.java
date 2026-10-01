package org.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String role;
    private String email;
    private String password;

    // Các biến đại diện cho 4 trang hồ sơ của ứng viên
    private BasicInfo page1_basicInfo;
    private Competency page2_competency;
    private Experience page3_experience;
    private Orientation page4_orientation;

    // Constructor: Khởi tạo sẵn các object để tránh lỗi NullPointerException khi lên Web
    public User() {
        this.page1_basicInfo = new BasicInfo();
        this.page2_competency = new Competency();
        this.page3_experience = new Experience();
        this.page4_orientation = new Orientation();
    }

    // --- CÁC LỚP LỒNG NHAU (NESTED CLASSES) ĐẠI DIỆN CHO TỪNG TRANG ---
    // Trang 1: BasicInfo
    public static class BasicInfo {
        private String fullName;
        private String phone;
        private String dob;
        private String gender;
        private String socialLinks; // Chuyển Array thành String cách nhau bằng dấu phẩy cho dễ nhập trên Web

        //Setters cho BasicInfo
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public void setSocialLinks(String socialLinks) {
            this.socialLinks = socialLinks;
        }

        // Getters cho BasicInfo
        public String getFullName() {
            return fullName;
        }

        public String getPhone() {
            return phone;
        }

        public String getDob() {
            return dob;
        }

        public String getGender() {
            return gender;
        }

        public String getSocialLinks() {
            return socialLinks;
        }
    }

    // Trang 2: Competency
    public static class Competency {
        private String education;
        private String languages;
        private String softSkills;
        private String hardSkills;
        private String strengths;

        //Setters cho Competency
        public void setEducation(String education) {
            this.education = education;
        }

        public void setLanguages(String languages) {
            this.languages = languages;
        }

        public void setSoftSkills(String softSkills) {
            this.softSkills = softSkills;
        }

        public void setHardSkills(String hardSkills) {
            this.hardSkills = hardSkills;
        }

        public void setStrengths(String strengths) {
            this.strengths = strengths;
        }

        // Getters cho Competency
        public String getEducation() {
            return education;
        }

        public String getLanguages() {
            return languages;
        }

        public String getSoftSkills() {
            return softSkills;
        }

        public String getHardSkills() {
            return hardSkills;
        }

        public String getStrengths() {
            return strengths;
        }
    }

    // Trang 3: Experience
    public static class Experience {
        private String projects;
        private String workHistory;

        // Setters cho Experience
        public void setProjects(String projects) {
            this.projects = projects;
        }

        public void setWorkHistory(String workHistory) {
            this.workHistory = workHistory;
        }

        // Getters cho Experience
        public String getProjects() {
            return projects;
        }

        public String getWorkHistory() {
            return workHistory;
        }
    }

    // Trang 4: Orientation
    public static class Orientation {
        private String desiredPosition;
        private String reason;
        private String careerGoal;

        // Setters cho Orientation
        public void setDesiredPosition(String desiredPosition) {
            this.desiredPosition = desiredPosition;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public void setCareerGoal(String careerGoal) {
            this.careerGoal = careerGoal;
        }

        // Getters cho Orientation
        public String getDesiredPosition() {
            return desiredPosition;
        }

        public String getReason() {
            return reason;
        }

        public String getCareerGoal() {
            return careerGoal;
        }
    }

    // SETTERS cho id, role, email, password, page1_basicInfo, page2_competency, page3_experience, page4_orientation
    public void setId(String id) {
        this.id = id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPage1_basicInfo(BasicInfo page1_basicInfo) {
        this.page1_basicInfo = page1_basicInfo;
    }

    public void setPage2_competency(Competency page2_competency) {
        this.page2_competency = page2_competency;
    }

    public void setPage3_experience(Experience page3_experience) {
        this.page3_experience = page3_experience;
    }

    public void setPage4_orientation(Orientation page4_orientation) {
        this.page4_orientation = page4_orientation;
    }

    // Getters cho id, role, email, password, page1_basicInfo, page2_competency, page3_experience, page4_orientation
    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public BasicInfo getPage1_basicInfo() {
        return page1_basicInfo;
    }

    public Competency getPage2_competency() {
        return page2_competency;
    }

    public Experience getPage3_experience() {
        return page3_experience;
    }

    public Orientation getPage4_orientation() {
        return page4_orientation;
    }
}