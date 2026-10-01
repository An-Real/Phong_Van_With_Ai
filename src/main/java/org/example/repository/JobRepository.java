package org.example.repository;

import org.example.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface JobRepository extends MongoRepository<Job, String> {
    // Hàm này giúp tìm toàn bộ bài đăng CỦA RIÊNG 1 CÔNG TY
    List<Job> findByEmployerId(String employerId);
}