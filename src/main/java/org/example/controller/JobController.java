package org.example.controller;

import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.example.model.Job;
import org.example.model.User;
import org.example.model.Application;
import org.example.repository.JobRepository;
import org.example.repository.ApplicationRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class JobController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public JobController(JobRepository jobRepository, ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    // LUỒNG CỦA DOANH NGHIỆP: QUẢN LÝ BÀI ĐĂNG

    // Hiển thị danh sách bài đăng của công ty
    @GetMapping("/employer/jobs")
    public String manageJobs(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"EMPLOYER".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // Lấy danh sách các Job do công ty này đăng
        List<Job> myJobs = jobRepository.findByEmployerId(loggedInUser.getId());
        model.addAttribute("myJobs", myJobs);
        model.addAttribute("newJob", new Job()); // Chuẩn bị sẵn 1 Job rỗng để form tạo mới sử dụng

        return "employer-jobs";
    }

    // Xử lý khi công ty bấm "Đăng bài"
    @PostMapping("/employer/jobs/add")
    public String addJob(Job newJob, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Gắn ID của công ty vào bài đăng để biết ai là chủ
        newJob.setEmployerId(loggedInUser.getId());
        newJob.setCreatedAt(LocalDateTime.now()); // Lưu thời gian đăng bài

        jobRepository.save(newJob);

        return "redirect:/employer/jobs?success";
    }

    // --- LUỒNG CỦA ỨNG VIÊN: BẢNG TIN & NỘP CV ---

    // Hiển thị Bảng tin việc làm (Tất cả job của mọi công ty)
    @GetMapping("/candidate/job-board")
    public String showJobBoard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CANDIDATE".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // Lấy danh sách các Job mà ứng viên này đã nộp
        List<Job> allJobs = jobRepository.findAll();
        List<Application> myApps = applicationRepository.findByCandidateId(loggedInUser.getId());
        List<String> appliedJobIds = myApps.stream().map(Application::getJobId).collect(java.util.stream.Collectors.toList());

        model.addAttribute("jobs", allJobs);
        model.addAttribute("appliedJobIds", appliedJobIds); // Gửi mảng ID này ra HTML
        return "candidate-job-board";
    }

    // Xử lý khi ứng viên bấm nút "Nộp CV"
    @PostMapping("/candidate/apply")
    public String applyForJob(@RequestParam String jobId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Tạo một đơn ứng tuyển mới
        Application app = new Application();
        app.setCandidateId(loggedInUser.getId());
        app.setJobId(jobId);
        app.setStatus("APPLIED"); // Trạng thái ban đầu luôn là Đã nộp
        app.setAppliedAt(LocalDateTime.now());

        applicationRepository.save(app);

        return "redirect:/candidate/job-board?success";
    }

    // DTO: Gói dữ liệu chứa cả Đơn ứng tuyển + Công việc + Ứng viên
    public static class ApplicantInfo {
        public Application app; public Job job; public User candidate;
        public ApplicantInfo(Application app, Job job, User candidate) { this.app = app; this.job = job; this.candidate = candidate; }
        public Application getApp() { return app; } public Job getJob() { return job; } public User getCandidate() { return candidate; }
    }

    // Hiển thị danh sách ứng viên
    @GetMapping("/employer/applicants")
    public String viewApplicants(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"EMPLOYER".equals(loggedInUser.getRole())) return "redirect:/login";

        // Tìm tất cả Job của công ty
        List<Job> myJobs = jobRepository.findByEmployerId(loggedInUser.getId());
        List<String> jobIds = myJobs.stream().map(Job::getId).collect(java.util.stream.Collectors.toList());

        // Tìm tất cả Đơn ứng tuyển chui vào các Job đó
        List<Application> apps = applicationRepository.findByJobIdIn(jobIds);

        // Lắp ghép dữ liệu
        java.util.List<ApplicantInfo> applicantList = new java.util.ArrayList<>();
        for (Application app : apps) {
            Job job = myJobs.stream().filter(j -> j.getId().equals(app.getJobId())).findFirst().orElse(null);
            User candidate = userRepository.findById(app.getCandidateId()).orElse(null);
            if (job != null && candidate != null) applicantList.add(new ApplicantInfo(app, job, candidate));
        }

        model.addAttribute("applicantList", applicantList);
        return "employer-applicants";
    }

    // Xử lý nút Cho Đậu / Từ chối
    @PostMapping("/employer/applicants/status")
    public String updateAppStatus(@org.springframework.web.bind.annotation.RequestParam String appId, @org.springframework.web.bind.annotation.RequestParam String status) {
        Application app = applicationRepository.findById(appId).orElse(null);
        if (app != null) {
            app.setStatus(status);
            applicationRepository.save(app);
        }
        return "redirect:/employer/applicants";
    }

    // Hiển thị chi tiết Hồ sơ ứng viên khi click vào hàng
    @GetMapping("/employer/applicants/detail")
    public String viewApplicantDetail(@org.springframework.web.bind.annotation.RequestParam String appId, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"EMPLOYER".equals(loggedInUser.getRole())) return "redirect:/login";

        // Tìm đơn ứng tuyển, công việc và ứng viên
        Application app = applicationRepository.findById(appId).orElse(null);
        if (app == null) return "redirect:/employer/applicants";

        Job job = jobRepository.findById(app.getJobId()).orElse(null);
        User candidate = userRepository.findById(app.getCandidateId()).orElse(null);

        // Gửi dữ liệu ra trang HTML chi tiết
        model.addAttribute("app", app);
        model.addAttribute("job", job);
        model.addAttribute("candidate", candidate);

        return "employer-applicant-detail";
    }
}