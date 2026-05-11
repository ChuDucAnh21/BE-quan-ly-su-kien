package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    // DTO trả về sau khi đăng nhập thành công.
    // Chứa mã Token để người dùng dùng cho các lần gọi API sau.

    private String accessToken; // Mã truy cập (ngắn hạn - thường 1 ngày)
    private String refreshToken; // Mã làm mới (dài hạn - thường 30 ngày)
    private UserProfileResponse user; // Thông tin tóm tắt của User vừa đăng nhập
}
