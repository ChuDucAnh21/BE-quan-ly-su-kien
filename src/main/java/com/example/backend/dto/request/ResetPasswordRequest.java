package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    // DTO phục vụ chức năng điền mã và đặt mật khẩu mới (Bước cuối của Quên mật khẩu)

    @NotBlank(message = "Token không được để trống")
    private String token; // Mã khôi phục nhận được từ Email

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword; // Mật khẩu mới muốn thiết lập
}
