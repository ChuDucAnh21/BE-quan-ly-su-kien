package com.example.backend.repository;

import com.example.backend.entity.Registration;
import com.example.backend.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Đếm số lượng vé đã nằm trong trạng thái chỉ định (Ví dụ: CONFIRMED hoặc
    // CHECKED_IN)
    long countByEventIdAndStatusIn(Long eventId, List<RegistrationStatus> statuses);

    // Kiểm tra xem User đã đăng ký Event này chưa
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    // Tìm kiếm đăng ký theo EventId và UserId
    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);
}
