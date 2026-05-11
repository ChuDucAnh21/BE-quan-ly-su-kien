package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // Kích hoạt tính năng tự động điền ngày tạo (createdAt) và ngày cập nhật (updatedAt) của JPA
public class JpaAuditingConfig {
    // Lớp này cho phép Spring-Data-JPA tự động theo dõi thời gian tạo/sửa bản ghi
}
