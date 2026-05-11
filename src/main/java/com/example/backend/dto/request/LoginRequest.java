package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    // DTO phục vụ chức năng Đăng nhập.

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9+_.-]+@gmail\\.com$", message = "Email Không đúng định dạng (phải là @gmail.com)")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
