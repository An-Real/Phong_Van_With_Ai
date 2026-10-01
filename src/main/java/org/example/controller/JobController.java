package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.model.Job;
import org.example.model.User;
import org.example.repository.JobRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class JobController {

    private final JobRepository jobRepository;

    public JobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
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
}