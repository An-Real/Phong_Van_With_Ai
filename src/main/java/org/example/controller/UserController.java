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
}