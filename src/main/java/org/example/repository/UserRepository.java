package org.example.repository;

import org.example.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

// Kế thừa MongoRepository, Spring Boot sẽ tự động viết các hàm lấy dữ liệu (findAll, findById)
public interface UserRepository extends MongoRepository<User, String> {
    User findByEmailAndPassword(String email, String password);
}