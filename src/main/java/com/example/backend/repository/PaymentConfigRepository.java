package com.example.backend.repository;

import com.example.backend.entity.PaymentConfig;
import com.example.backend.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentConfigRepository extends JpaRepository<PaymentConfig, Long> {
    // Kiểm tra xem User này đã cấu hình PayOS chưa và đang active hay không
    boolean existsByUserAndIsActiveTrue(User user);

    // Tìm cấu hình payment của 1 User
    Optional<PaymentConfig> findByUserId(Long userId);
}
