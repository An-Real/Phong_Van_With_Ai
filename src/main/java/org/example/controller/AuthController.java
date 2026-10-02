package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Trang Đăng ký (chỉ cần Email, Pass, Role)
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("newUser", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(User newUser) {
        userRepository.save(newUser);
        return "redirect:/login"; // Đăng ký xong bắt qua trang Đăng nhập
    }

    // Trang Đăng nhập
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        // Vào DB tìm user có email và pass này
        User user = userRepository.findByEmailAndPassword(email, password);

        if (user != null) {
            // Nếu đúng, lưu thông tin user vào Session
            session.setAttribute("loggedInUser", user);

            // Điều hướng dựa theo vai trò
            if ("EMPLOYER".equals(user.getRole())) {
                return "redirect:/employer/jobs";
            } else {
                return "redirect:/candidate/job-board";
            }
        } else {
            // Nếu sai, báo lỗi
            model.addAttribute("error", "Email hoặc Mật khẩu không đúng!");
            return "login";
        }
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa thẻ thành viên
        return "redirect:/login";
    }
}