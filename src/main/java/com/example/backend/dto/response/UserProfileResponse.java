package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    // DTO (Data Transfer Object): Dùng để truyền dữ liệu từ Server về cho Client (Frontend)
    // Các trường này quy định cấu trúc JSON mà Frontend sẽ nhận được

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String role;
    private String status;
    private String gender; // Giới tính (String để dễ hiển thị)
    private LocalDate dateOfBirth; // Ngày sinh
    private String address; // Địa chỉ
    private LocalDateTime createdAt; // Ngày tham gia hệ thống
}
