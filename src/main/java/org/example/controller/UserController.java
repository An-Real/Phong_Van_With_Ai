package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // API - LẤY DANH SÁCH USER
    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsersApi() {
        return userRepository.findAll();
    }


    // TRANG HIỂN THỊ DANH SÁCH USER
    @GetMapping("/users")
    public String viewUsersPage(Model model) {

        List<User> listUsers = userRepository.findAll();

        model.addAttribute("danhSachUser", listUsers);

        return "users";
    }

    // CANDIDATE
    // Hiển thị profile Candidate
    @GetMapping("/candidate/profile")
    public String showCandidateProfile(
            HttpSession session,
            Model model) {

        // Kiểm tra đăng nhập
        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra role
        if (!"CANDIDATE".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // Lấy dữ liệu mới nhất từ MongoDB
        User userInDb =
                userRepository
                        .findById(loggedInUser.getId())
                        .orElse(null);

        if (userInDb == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // Đưa User ra HTML
        model.addAttribute("candidate", userInDb);

        return "candidate-form";
    }


    // Xử lý update profile Candidate
    @PostMapping("/candidate/profile")
    public String updateCandidateProfile(
            @ModelAttribute("candidate") User submittedData,
            HttpSession session) {

        // Lấy user đang đăng nhập
        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra role
        if (!"CANDIDATE".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // Tìm user thật trong Database
        User userInDb =
                userRepository
                        .findById(loggedInUser.getId())
                        .orElse(null);

        if (userInDb == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // CHỈ cập nhật thông tin hồ sơ
        userInDb.setPage1_basicInfo(
                submittedData.getPage1_basicInfo()
        );

        userInDb.setPage2_competency(
                submittedData.getPage2_competency()
        );

        userInDb.setPage3_experience(
                submittedData.getPage3_experience()
        );

        userInDb.setPage4_orientation(
                submittedData.getPage4_orientation()
        );

        // Lưu vào MongoDB
        userRepository.save(userInDb);

        return "redirect:/candidate/profile?success";
    }


    // EMPLOYER
    // Hiển thị profile Employer
    @GetMapping("/employer/dashboard")
    public String showEmployerDashboard(
            HttpSession session,
            Model model) {

        // Kiểm tra đăng nhập
        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra role
        if (!"EMPLOYER".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // Lấy dữ liệu mới nhất từ Database
        User userInDb =
                userRepository
                        .findById(loggedInUser.getId())
                        .orElse(null);

        if (userInDb == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("employer", userInDb);

        return "employer-form";
    }


    // Xử lý update profile Employer
    @PostMapping("/employer/profile")
    public String updateEmployerProfile(
            @ModelAttribute("employer") User submittedData,
            HttpSession session) {

        // Kiểm tra đăng nhập
        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra role
        if (!"EMPLOYER".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // Tìm user trong Database
        User userInDb =
                userRepository
                        .findById(loggedInUser.getId())
                        .orElse(null);

        if (userInDb == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // Chỉ cập nhật companyInfo
        userInDb.setCompanyInfo(
                submittedData.getCompanyInfo()
        );

        // Lưu Database
        userRepository.save(userInDb);

        return "redirect:/employer/dashboard?success";
    }
}