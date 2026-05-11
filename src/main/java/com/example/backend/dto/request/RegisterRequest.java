package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    // Luôn bắt đầu bằng DTO để nhận dữ liệu từ Client gửi lên.
    // Các Annotation ở trên đầu mỗi trường dùng để kiểm tra tính đúng đắn của dữ
    // liệu ngay lập tức.

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    // Regex này bắt buộc email phải là Gmail thực (có đuôi @gmail.com)
    @Pattern(regexp = "^[a-zA-Z0-9+_.-]+@gmail\\.com$", message = "Email không đúng định dạng (phải là @gmail.com)")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    // Regex kiểm tra số điện thoại Việt Nam (10 số, bắt đầu 0 hoặc +84)
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ (Phải bắt đầu bằng 0 hoặc +84 và đủ 10 số)")
    private String phone;
}
