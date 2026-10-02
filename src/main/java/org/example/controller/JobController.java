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

    private boolean isProfileComplete(User user) {
        if (user == null) {
            return false;
        }

        // CANDIDATE
        if ("CANDIDATE".equals(user.getRole())) {
            // Mục 1
            if (user.getPage1_basicInfo() == null) {
                return false;
            }

            if (!isNotBlank(user.getPage1_basicInfo().getFullName())) {
                return false;
            }

            if (!isNotBlank(user.getPage1_basicInfo().getPhone())) {
                return false;
            }

            if (!isNotBlank(user.getPage1_basicInfo().getDob())) {
                return false;
            }

            if (!isNotBlank(user.getPage1_basicInfo().getGender())) {
                return false;
            }

            if (!isNotBlank(user.getPage1_basicInfo().getSocialLinks())) {
                return false;
            }

            // Mục 2
            if (user.getPage2_competency() == null) {
                return false;
            }

            if (!isNotBlank(user.getPage2_competency().getEducation())) {
                return false;
            }

            if (!isNotBlank(user.getPage2_competency().getLanguages())) {
                return false;
            }

            if (!isNotBlank(user.getPage2_competency().getSoftSkills())) {
                return false;
            }

            if (!isNotBlank(user.getPage2_competency().getHardSkills())) {
                return false;
            }

            if (!isNotBlank(user.getPage2_competency().getStrengths())) {
                return false;
            }


            // Mục 4
            if (user.getPage4_orientation() == null) {
                return false;
            }

            if (!isNotBlank(user.getPage4_orientation().getDesiredPosition())) {
                return false;
            }

            if (!isNotBlank(user.getPage4_orientation().getReason())) {
                return false;
            }

            if (!isNotBlank(user.getPage4_orientation().getCareerGoal())) {
                return false;
            }

            return true;
        }

        // EMPLOYER
        if ("EMPLOYER".equals(user.getRole())) {
            if (!isNotBlank(user.getEmail())) {
                return false;
            }

            if (user.getCompanyInfo() == null) {
                return false;
            }

            if (!isNotBlank(
                    user.getCompanyInfo().getCompanyName()
            )) {
                return false;
            }

            if (!isNotBlank(
                    user.getCompanyInfo().getIndustry()
            )) {
                return false;
            }

            if (!isNotBlank(
                    user.getCompanyInfo().getDescription()
            )) {
                return false;
            }

            if (!isNotBlank(
                    user.getCompanyInfo().getWebsite()
            )) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // =========================================================
    // EMPLOYER - QUẢN LÝ BÀI ĐĂNG
    // =========================================================

    // Hiển thị danh sách bài đăng
    @GetMapping("/employer/jobs")
    public String manageJobs(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (!hasRole(loggedInUser, "EMPLOYER")) return "redirect:/login";

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
        }

        List<Job> myJobs = jobRepository.findByEmployerId(loggedInUser.getId());
        model.addAttribute("myJobs", myJobs);
        return "employer-jobs";
    }

    // Mở trang Form Tạo bài đăng mới
    @GetMapping("/employer/jobs/create")
    public String showCreateJobForm(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (!hasRole(loggedInUser, "EMPLOYER")) return "redirect:/login";

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
        }

        model.addAttribute("newJob", new Job());
        return "employer-job-create"; // Sẽ tạo file HTML mới
    }

    // Xử lý Lưu bài đăng (Dùng chung cho cả Tạo mới và Cập nhật)
    @PostMapping("/employer/jobs/add")
    public String addJob(@ModelAttribute("newJob") Job newJob, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (!hasRole(loggedInUser, "EMPLOYER")) return "redirect:/login";

        newJob.setEmployerId(loggedInUser.getId());

        // Sửa lõi ghi đè
        // Nếu ID gửi lên là một chuỗi rỗng "", ta ép nó về null để MongoDB hiểu là Tạo mới.
        if (newJob.getId() != null && newJob.getId().trim().isEmpty()) {
            newJob.setId(null);
        }

        if (newJob.getId() == null) {
            // Tạo mới
            newJob.setCreatedAt(LocalDateTime.now());
            newJob.setStatus("OPEN");
        } else {
            // Cập nhật
            Job oldJob = jobRepository.findById(newJob.getId()).orElse(null);
            if (oldJob != null) {
                // Phục hồi lại ngày tạo cũ (đề phòng HTML gửi lên bị null)
                newJob.setCreatedAt(oldJob.getCreatedAt());

                // Nếu đang CLOSED/HIDDEN mà số lượng mới > 0 -> Mở lại và renew ngày đăng
                if (!"OPEN".equals(oldJob.getStatus()) && newJob.getHeadcount() > 0) {
                    newJob.setStatus("OPEN");
                    newJob.setCreatedAt(LocalDateTime.now()); // Đăng lại với ngày mới nhất
                } else {
                    newJob.setStatus(oldJob.getStatus()); // Giữ nguyên trạng thái
                }

                // Nếu sửa mà số lượng = 0 thì tự đóng bài đăng
                if (newJob.getHeadcount() <= 0) {
                    newJob.setStatus("CLOSED");
                }
            }
        }

        jobRepository.save(newJob);
        return "redirect:/employer/jobs?success=true";
    }

    // Mở trang Chi tiết Bài đăng
    @GetMapping("/employer/jobs/detail")
    public String viewJobDetail(@RequestParam String jobId, HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (!hasRole(loggedInUser, "EMPLOYER")) return "redirect:/login";

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
        }

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null || !loggedInUser.getId().equals(job.getEmployerId())) return "redirect:/employer/jobs";

        model.addAttribute("job", job);
        return "employer-job-detail"; // Sẽ tạo file HTML mới
    }

    // Mở trang Sửa bài đăng (Tái sử dụng giao diện tạo mới)
    @GetMapping("/employer/jobs/edit")
    public String editJobForm(@RequestParam String jobId, HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (!hasRole(loggedInUser, "EMPLOYER")) return "redirect:/login";

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
        }

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null || !loggedInUser.getId().equals(job.getEmployerId())) return "redirect:/employer/jobs";

        model.addAttribute("newJob", job); // Đưa data cũ vào form
        return "employer-job-create";
    }

    // Xóa bài đăng
    @PostMapping("/employer/jobs/delete")
    public String deleteJob(@RequestParam String jobId, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (!hasRole(loggedInUser, "EMPLOYER")) return "redirect:/login";

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job != null && loggedInUser.getId().equals(job.getEmployerId())) {
            jobRepository.delete(job);
        }
        return "redirect:/employer/jobs?deleted=true";
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

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
        }

        // Tất cả Job
        List<Job> allJobs = jobRepository.findAll().stream()
                .filter(j -> "OPEN".equals(j.getStatus()))
                .collect(Collectors.toList());

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

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
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

        List<String> cvStatuses = Arrays.asList("APPLIED", "REJECTED");
        List<ApplicantInfoDTO> applicantList = new ArrayList<>();
        for (Application app : apps) {
            // CHỈ THÊM VÀO LIST NẾU LÀ APPLIED HOẶC REJECTED
            if (cvStatuses.contains(app.getStatus())) {
                Job job = jobMap.get(app.getJobId());
                User candidate = userRepository.findById(app.getCandidateId()).orElse(null);
                if (job != null && candidate != null) {
                    applicantList.add(new ApplicantInfoDTO(app, job, candidate));
                }
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

        // Chuyển từ trạng thái khác -> HIRED
        if ("HIRED".equals(status)
                && !"HIRED".equals(app.getStatus())) {

            if (job.getHeadcount() > 0) {
                // giảm số lượng tuyển 1
                job.setHeadcount(job.getHeadcount() - 1);

                // Nếu đã tuyển đủ người
                if (job.getHeadcount() == 0) {
                    job.setStatus("CLOSED");
                }

                jobRepository.save(job);
            }
        }

        // Chuyển từ HIRED -> trạng thái khác
        if ("HIRED".equals(app.getStatus())
                && !"HIRED".equals(status)) {
            // tăng số lượng tuyển 1
            job.setHeadcount(job.getHeadcount() + 1);

            // Nếu Job trước đó đã CLOSED vì đủ người
            // thì mở lại
            if ("CLOSED".equals(job.getStatus())) {
                job.setStatus("OPEN");
            }

            jobRepository.save(job);
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

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
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

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
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

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
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

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
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

        // ÉP BUỘC CHUYỂN HƯỚNG NẾU HỒ SƠ TRỐNG
        if (!isProfileComplete(loggedInUser)) {
            return "redirect:/candidate/profile?edit=true&msg=require_profile";
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