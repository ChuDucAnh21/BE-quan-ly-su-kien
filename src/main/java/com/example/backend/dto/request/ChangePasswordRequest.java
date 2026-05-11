package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    // DTO phục vụ chức năng đổi mật khẩu khi đã đăng nhập
    
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword; // Nhập mật khẩu hiện tại để xác thực chính chủ

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword; // Nhập mật khẩu muốn thay đổi
}
