package org.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Khai báo class này map với bảng "users" trong DB
@Document(collection = "users")
public class User {

    @Id // Khai báo đây là khóa chính (_id)
    private String id;
    private String role;
    private String email;

    // Tạm thời lấy 3 trường này để test web.
    // (Các trường nested như page1_basicInfo  sẽ map chi tiết sau)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}