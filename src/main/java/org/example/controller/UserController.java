package org.example.controller;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller // @Controller để có thể trả về file HTML
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Trả về JSON thô (Giữ nguyên để sau này AI gọi)
    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsersApi() {
        return userRepository.findAll();
    }

    // Trả về file HTML
    @GetMapping("/users")
    public String viewUsersPage(Model model) {
        // Lấy danh sách user từ Database
        List<User> listUsers = userRepository.findAll();
        // Gói danh sách đó vào một biến tên là "danhSachUser" để gửi ra màn hình HTML
        model.addAttribute("danhSachUser", listUsers);

        // Trả về file giao diện có tên là "users.html"
        return "users";
    }

    // Hiển thị trang Form Đăng ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // Tạo một đối tượng User rỗng để form HTML có chỗ chứa dữ liệu người dùng gõ vào
        model.addAttribute("newUser", new User());
        return "register"; // Trả về file register.html
    }

    // Xử lý khi người dùng bấm nút "Đăng ký"
    @org.springframework.web.bind.annotation.PostMapping("/register")
    public String saveUser(@org.springframework.web.bind.annotation.ModelAttribute("newUser") User user) {
        // Lưu user mới vào MongoDB
        userRepository.save(user);

        // Lưu xong thì tự động chuyển hướng (redirect) về trang danh sách để xem kết quả
        return "redirect:/users";
    }

    // Hiển thị Form tạo hồ sơ Ứng viên (Gộp 4 trang vào 1 màn hình trước)
    @GetMapping("/candidate/profile")
    public String showCandidateForm(Model model) {
        User candidate = new User();
        candidate.setRole("CANDIDATE"); // Mặc định gán vai trò là ứng viên
        model.addAttribute("candidate", candidate);
        return "candidate-form";
    }

    // Xử lý lưu hồ sơ ứng viên
    @org.springframework.web.bind.annotation.PostMapping("/candidate/profile")
    public String saveCandidateProfile(@org.springframework.web.bind.annotation.ModelAttribute("candidate") User candidate) {
        userRepository.save(candidate);
        return "redirect:/users"; // Lưu xong quay về trang danh sách xem thành quả
    }
}