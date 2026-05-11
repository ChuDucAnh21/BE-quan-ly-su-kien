package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    // DTO phục vụ chức năng yêu cầu mã khôi phục mật khẩu (Quên mật khẩu)

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9+_.-]+@gmail\\.com$", message = "Email phải có định dạng @gmail.com")
    private String email; // Nhập Email tài khoản cần khôi phục
}
