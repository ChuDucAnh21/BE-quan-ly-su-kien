package com.example.backend.dto.request;

import com.example.backend.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    // DTO dùng để cập nhật thông tin cá nhân.
    // Một số trường là bắt buộc (@NotBlank), một số là tùy chọn.

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName; // Tên đầy đủ

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ (Phải bắt đầu bằng 0 hoặc +84 và đủ 10 số)")
    private String phone; // Số điện thoại VN

    // Các trường tuỳ chọn (nullable) — người dùng có thể để trống nếu chưa muốn cung cấp
    private Gender gender; // Giới tính (Enum)
    private LocalDate dateOfBirth; // Ngày sinh
    private String address; // Địa chỉ thường trú
}
