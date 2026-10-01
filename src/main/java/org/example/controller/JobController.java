package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.dto.AppliedJobInfoDTO;
import org.example.dto.ApplicantInfoDTO;
import org.example.model.Application;
import org.example.model.Job;
import org.example.model.User;
import org.example.repository.ApplicationRepository;
import org.example.repository.JobRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class JobController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public JobController(
            JobRepository jobRepository,
            ApplicationRepository applicationRepository,
            UserRepository userRepository
    ) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // HELPER
    // =========================================================

    /**
     * Lấy user đang đăng nhập từ session.
     */
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    /**
     * Kiểm tra user có đúng role hay không.
     */
    private boolean hasRole(User user, String role) {
        return user != null && role.equals(user.getRole());
    }

    /**
     * Tạo Map<JobId, Job> để tìm Job nhanh hơn.
     */
    private Map<String, Job> createJobMap(List<Job> jobs) {
        return jobs.stream()
                .collect(Collectors.toMap(
                        Job::getId,
                        Function.identity()
                ));
    }


    // =========================================================
    // EMPLOYER - QUẢN LÝ BÀI ĐĂNG
    // =========================================================

    /**
     * Hiển thị danh sách bài đăng của nhà tuyển dụng.
     */
    @GetMapping("/employer/jobs")
    public String manageJobs(
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "EMPLOYER")) {
            return "redirect:/login";
        }

        // Lấy các Job do nhà tuyển dụng hiện tại đăng
        List<Job> myJobs = jobRepository.findByEmployerId(
                loggedInUser.getId()
        );

        model.addAttribute("myJobs", myJobs);

        // Job rỗng để form tạo bài đăng mới
        model.addAttribute("newJob", new Job());

        return "employer-jobs";
    }


    /**
     * Xử lý tạo bài đăng mới.
     */
    @PostMapping("/employer/jobs/add")
    public String addJob(
            @ModelAttribute("newJob") Job newJob,
            HttpSession session
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "EMPLOYER")) {
            return "redirect:/login";
        }

        // Không cho client tự truyền employerId khác
        newJob.setEmployerId(loggedInUser.getId());

        // Thời gian tạo bài đăng
        newJob.setCreatedAt(LocalDateTime.now());

        jobRepository.save(newJob);

        return "redirect:/employer/jobs";
    }


    // =========================================================
    // CANDIDATE - BẢNG TIN VIỆC LÀM
    // =========================================================

    /**
     * Hiển thị tất cả việc làm.
     */
    @GetMapping("/candidate/job-board")
    public String showJobBoard(
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "CANDIDATE")) {
            return "redirect:/login";
        }

        // Tất cả Job
        List<Job> allJobs = jobRepository.findAll();

        // Các đơn ứng tuyển của ứng viên hiện tại
        List<Application> myApps =
                applicationRepository.findByCandidateId(
                        loggedInUser.getId()
                );

        // Danh sách Job ID đã nộp
        List<String> appliedJobIds = myApps.stream()
                .map(Application::getJobId)
                .collect(Collectors.toList());

        model.addAttribute("jobs", allJobs);
        model.addAttribute("appliedJobIds", appliedJobIds);

        return "candidate-job-board";
    }


    /**
     * Ứng viên nộp CV vào một Job.
     */
    @PostMapping("/candidate/apply")
    public String applyForJob(
            @RequestParam("jobId") String jobId,
            HttpSession session
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "CANDIDATE")) {
            return "redirect:/login";
        }

        // Kiểm tra Job có tồn tại hay không
        Job job = jobRepository.findById(jobId).orElse(null);

        if (job == null) {
            return "redirect:/candidate/job-board";
        }

        // Kiểm tra ứng viên đã nộp Job này chưa
        List<Application> myApps =
                applicationRepository.findByCandidateId(
                        loggedInUser.getId()
                );

        boolean alreadyApplied = myApps.stream()
                .anyMatch(app -> jobId.equals(app.getJobId()));

        if (alreadyApplied) {
            return "redirect:/candidate/job-board";
        }

        // Tạo Application mới
        Application app = new Application();

        app.setCandidateId(loggedInUser.getId());
        app.setJobId(jobId);
        app.setStatus("APPLIED");
        app.setAppliedAt(LocalDateTime.now());

        applicationRepository.save(app);

        return "redirect:/candidate/job-board";
    }


    // =========================================================
    // EMPLOYER - DANH SÁCH ỨNG VIÊN
    // =========================================================

    /**
     * Hiển thị toàn bộ ứng viên đã ứng tuyển vào Job của công ty.
     */
    @GetMapping("/employer/applicants")
    public String viewApplicants(
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "EMPLOYER")) {
            return "redirect:/login";
        }

        // Lấy Job của nhà tuyển dụng
        List<Job> myJobs = jobRepository.findByEmployerId(
                loggedInUser.getId()
        );

        if (myJobs.isEmpty()) {
            model.addAttribute(
                    "applicantList",
                    new ArrayList<ApplicantInfoDTO>()
            );

            return "employer-applicants";
        }

        // Lấy danh sách Job ID
        List<String> jobIds = myJobs.stream()
                .map(Job::getId)
                .collect(Collectors.toList());

        // Map Job ID -> Job
        Map<String, Job> jobMap = createJobMap(myJobs);

        // Lấy tất cả Application thuộc các Job của công ty
        List<Application> apps =
                applicationRepository.findByJobIdIn(jobIds);

        List<ApplicantInfoDTO> applicantList = new ArrayList<>();

        for (Application app : apps) {

            Job job = jobMap.get(app.getJobId());

            User candidate =
                    userRepository.findById(app.getCandidateId())
                            .orElse(null);

            if (job != null && candidate != null) {

                applicantList.add(
                        new ApplicantInfoDTO(
                                app,
                                job,
                                candidate
                        )
                );
            }
        }

        model.addAttribute("applicantList", applicantList);

        return "employer-applicants";
    }


    /**
     * Cập nhật trạng thái Application.
     */
    @PostMapping("/employer/applicants/status")
    public String updateAppStatus(
            @RequestParam("appId") String appId,
            @RequestParam("status") String status,
            @RequestParam(
                    value = "source",
                    defaultValue = "applicants"
            ) String source,
            HttpSession session
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "EMPLOYER")) {
            return "redirect:/login";
        }

        Application app =
                applicationRepository.findById(appId)
                        .orElse(null);

        if (app == null) {
            return "redirect:/employer/applicants";
        }

        // Kiểm tra Job của Application
        Job job =
                jobRepository.findById(app.getJobId())
                        .orElse(null);

        if (job == null ||
                !loggedInUser.getId().equals(job.getEmployerId())) {

            return "redirect:/employer/applicants";
        }

        // Cập nhật trạng thái
        app.setStatus(status);
        applicationRepository.save(app);

        // Nếu thao tác từ trang phỏng vấn
        if ("interviews".equals(source)) {
            return "redirect:/employer/interviews";
        }

        return "redirect:/employer/applicants";
    }


    // =========================================================
    // EMPLOYER - CHI TIẾT ỨNG VIÊN
    // =========================================================

    /**
     * Xem chi tiết hồ sơ ứng viên.
     */
    @GetMapping("/employer/applicants/detail")
    public String viewApplicantDetail(
            @RequestParam("appId") String appId,
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "EMPLOYER")) {
            return "redirect:/login";
        }

        // Tìm Application
        Application app =
                applicationRepository.findById(appId)
                        .orElse(null);

        if (app == null) {
            return "redirect:/employer/applicants";
        }

        // Tìm Job
        Job job =
                jobRepository.findById(app.getJobId())
                        .orElse(null);

        if (job == null) {
            return "redirect:/employer/applicants";
        }

        // Bảo mật:
        // Application phải thuộc Job của Employer hiện tại
        if (!loggedInUser.getId().equals(job.getEmployerId())) {
            return "redirect:/employer/applicants";
        }

        // Tìm ứng viên
        User candidate =
                userRepository.findById(app.getCandidateId())
                        .orElse(null);

        if (candidate == null) {
            return "redirect:/employer/applicants";
        }

        // Đưa dữ liệu sang HTML
        model.addAttribute("app", app);
        model.addAttribute("job", job);
        model.addAttribute("candidate", candidate);

        return "employer-applicant-detail";
    }


    // =========================================================
    // EMPLOYER - DANH SÁCH PHỎNG VẤN
    // =========================================================

    /**
     * Hiển thị những Application đang ở vòng phỏng vấn.
     */
    @GetMapping("/employer/interviews")
    public String viewInterviews(
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "EMPLOYER")) {
            return "redirect:/login";
        }

        // Job của công ty
        List<Job> myJobs = jobRepository.findByEmployerId(
                loggedInUser.getId()
        );

        if (myJobs.isEmpty()) {
            model.addAttribute(
                    "interviewList",
                    new ArrayList<ApplicantInfoDTO>()
            );

            return "employer-interviews";
        }

        List<String> jobIds = myJobs.stream()
                .map(Job::getId)
                .collect(Collectors.toList());

        Map<String, Job> jobMap = createJobMap(myJobs);

        List<Application> apps =
                applicationRepository.findByJobIdIn(jobIds);

        // Các trạng thái thuộc vòng phỏng vấn
        List<String> interviewStatuses = Arrays.asList(
                "PENDING_INTERVIEW",
                "HIRED",
                "INTERVIEW_FAILED"
        );

        List<ApplicantInfoDTO> interviewList = new ArrayList<>();

        for (Application app : apps) {

            if (!interviewStatuses.contains(app.getStatus())) {
                continue;
            }

            Job job = jobMap.get(app.getJobId());

            User candidate =
                    userRepository.findById(app.getCandidateId())
                            .orElse(null);

            if (job != null && candidate != null) {

                interviewList.add(
                        new ApplicantInfoDTO(
                                app,
                                job,
                                candidate
                        )
                );
            }
        }

        model.addAttribute("interviewList", interviewList);

        return "employer-interviews";
    }


    // =========================================================
    // EMPLOYER - CHI TIẾT PHỎNG VẤN
    // =========================================================

    /**
     * Xem chi tiết một ứng viên trong vòng phỏng vấn.
     */
    @GetMapping("/employer/interviews/detail")
    public String viewInterviewDetail(
            @RequestParam("appId") String appId,
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "EMPLOYER")) {
            return "redirect:/login";
        }

        Application app =
                applicationRepository.findById(appId)
                        .orElse(null);

        if (app == null) {
            return "redirect:/employer/interviews";
        }

        // Tìm Job
        Job job =
                jobRepository.findById(app.getJobId())
                        .orElse(null);

        if (job == null) {
            return "redirect:/employer/interviews";
        }

        // Bảo mật:
        // Job phải thuộc Employer đang đăng nhập
        if (!loggedInUser.getId().equals(job.getEmployerId())) {
            return "redirect:/employer/interviews";
        }

        // Tìm Candidate
        User candidate =
                userRepository.findById(app.getCandidateId())
                        .orElse(null);

        if (candidate == null) {
            return "redirect:/employer/interviews";
        }

        model.addAttribute("app", app);
        model.addAttribute("job", job);
        model.addAttribute("candidate", candidate);

        return "employer-interview-detail";
    }


    // =========================================================
    // CANDIDATE - CÁC CÔNG TY / JOB ĐÃ NỘP
    // =========================================================

    /**
     * Hiển thị các Job mà ứng viên đã nộp.
     */
    @GetMapping("/candidate/applied-jobs")
    public String viewAppliedJobs(
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "CANDIDATE")) {
            return "redirect:/login";
        }

        // Application của candidate hiện tại
        List<Application> myApps =
                applicationRepository.findByCandidateId(
                        loggedInUser.getId()
                );

        List<AppliedJobInfoDTO> appliedList = new ArrayList<>();

        for (Application app : myApps) {

            Job job =
                    jobRepository.findById(app.getJobId())
                            .orElse(null);

            if (job != null) {

                appliedList.add(
                        new AppliedJobInfoDTO(
                                app,
                                job
                        )
                );
            }
        }

        model.addAttribute("appliedList", appliedList);

        return "candidate-applied-jobs";
    }


    // =========================================================
    // CANDIDATE - CHI TIẾT JOB ĐÃ ỨNG TUYỂN
    // =========================================================

    /**
     * Xem chi tiết Job đã ứng tuyển.
     */
    @GetMapping("/candidate/applied-jobs/detail")
    public String viewAppliedJobDetail(
            @RequestParam("appId") String appId,
            HttpSession session,
            Model model
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "CANDIDATE")) {
            return "redirect:/login";
        }

        Application app =
                applicationRepository.findById(appId)
                        .orElse(null);

        if (app == null) {
            return "redirect:/candidate/applied-jobs";
        }

        // Bảo mật:
        // Application phải thuộc candidate hiện tại
        if (!loggedInUser.getId().equals(app.getCandidateId())) {
            return "redirect:/candidate/applied-jobs";
        }

        Job job =
                jobRepository.findById(app.getJobId())
                        .orElse(null);

        if (job == null) {
            return "redirect:/candidate/applied-jobs";
        }

        model.addAttribute("app", app);
        model.addAttribute("job", job);

        return "candidate-applied-job-detail";
    }


    // =========================================================
    // CANDIDATE - HỦY ỨNG TUYỂN
    // =========================================================

    /**
     * Hủy một Application.
     */
    @PostMapping("/candidate/applied-jobs/cancel")
    public String cancelApplication(
            @RequestParam("appId") String appId,
            HttpSession session
    ) {

        User loggedInUser = getLoggedInUser(session);

        if (!hasRole(loggedInUser, "CANDIDATE")) {
            return "redirect:/login";
        }

        Application app =
                applicationRepository.findById(appId)
                        .orElse(null);

        if (app != null &&
                loggedInUser.getId().equals(app.getCandidateId())) {

            applicationRepository.delete(app);
        }

        return "redirect:/candidate/applied-jobs";
    }
}