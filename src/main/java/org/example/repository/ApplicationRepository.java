package org.example.repository;

import org.example.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ApplicationRepository extends MongoRepository<Application, String> {
    // Tìm tất cả đơn ứng tuyển của 1 ứng viên
    List<Application> findByCandidateId(String candidateId);

    // Tìm tất cả đơn ứng tuyển thuộc danh sách các Job của doanh nghiệp
    List<Application> findByJobIdIn(List<String> jobIds);
}