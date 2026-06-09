package com.example.backend.controller;

import com.example.backend.dto.request.ForgotPasswordRequest;
import com.example.backend.dto.request.GoogleLoginRequest;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.request.ResetPasswordRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.dto.response.UserProfileResponse;
import com.example.backend.service.AuthService;
import com.example.backend.service.PasswordResetService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController // Đánh dấu đây là lớp Controller xử lý các Request HTTP và trả về dữ liệu thuần
                // JSON
@RequestMapping("/api/v1/auth") // Định nghĩa đường dẫn gốc cho các API trong file này là /api/v1/auth
@RequiredArgsConstructor // Tự động tạo Constructor tiêm Bean (Dependency Injection) cho các trường
                         // `final`
public class AuthController {

        private final AuthService authService;
        private final PasswordResetService passwordResetService;

        // 1. API Đăng ký tài khoản (Public)
        @PostMapping("/register") // Phương thức POST: Dùng để tạo mới dữ liệu
        public ApiResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
                // @RequestBody: Chuyển dữ liệu từ JSON gửi lên thành đối tượng Java
                // @Valid: Kích hoạt việc kiểm tra định dạng dữ liệu (Email, SĐT...) dựa trên
                // các Annotation trong DTO
                authService.register(request);
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("User registered successfully")
                                                .build())
                                .build();
        }

        // 2. API Đăng nhập hệ thống
        @PostMapping("/login")
        public ApiResponse<UserProfileResponse> login(@RequestBody @Valid LoginRequest request,
                        HttpServletResponse response) {
                LoginResponse loginResponse = authService.login(request);

                // --- CƠ CHẾ BẢO MẬT COOKIE CHỐNG HACK ---
                // Lưu Access Token vào Cookie thay vì trả về LocalStorage để tăng tính bảo mật
                Cookie accessCookie = new Cookie("access_token", loginResponse.getAccessToken());

                // Set HttpOnly = true: Rất quan trọng! Ngăn chặn hacker dùng mã Javascript độc
                // hại (XSS)
                // chạy trên trình duyệt để đánh cắp Token của người dùng.
                accessCookie.setHttpOnly(true);

                // Set Secure = true: Cookie chỉ được gửi đi nếu trình duyệt dùng HTTPS (bảo mật
                // dữ liệu đường truyền)
                accessCookie.setSecure(true);

                // Path = "/": Cookie này sẽ tự động được trình duyệt đính kèm vào MỌI request
                // gọi lên Backend
                accessCookie.setPath("/");
                accessCookie.setMaxAge(24 * 60 * 60); // Thời gian sống của Cookie chứa access_token(24h)

                // Lưu Refresh Token vào một Cookie riêng (Thời gian sống lâu hơn: 30 ngày)
                Cookie refreshCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(true);
                refreshCookie.setPath("/api/v1/auth/refresh"); // Chỉ gửi Cookie này khi gọi API làm mới Token
                refreshCookie.setMaxAge(30 * 24 * 60 * 60); // Thời gian sống của Cookie chứa refresh_token(30
                                                            // ngày)

                // Thêm các Cookie này vào phản hồi trả về cho trình duyệt để khi có request
                // trình duyệt sẽ tự động gửi kèm với api
                response.addCookie(accessCookie);
                response.addCookie(refreshCookie);

                return ApiResponse.<UserProfileResponse>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Login successful")
                                                .build())
                                .data(loginResponse.getUser())
                                .build();
        }

        // 2.5 API Đăng nhập bằng Google
        @PostMapping("/google-login")
        public ApiResponse<UserProfileResponse> googleLogin(
                        @RequestBody @Valid GoogleLoginRequest request,
                        HttpServletResponse response) {
                LoginResponse loginResponse = authService.googleLogin(request);

                // Lưu JWT vào Cookie tương tự login bình thường
                Cookie accessCookie = new Cookie("access_token", loginResponse.getAccessToken());
                accessCookie.setHttpOnly(true);
                accessCookie.setSecure(true);
                accessCookie.setPath("/");
                accessCookie.setMaxAge(24 * 60 * 60);

                Cookie refreshCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(true);
                refreshCookie.setPath("/api/v1/auth/refresh");
                refreshCookie.setMaxAge(30 * 24 * 60 * 60);

                response.addCookie(accessCookie);
                response.addCookie(refreshCookie);

                return ApiResponse.<UserProfileResponse>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Google Login successful")
                                                .build())
                                .data(loginResponse.getUser())
                                .build();
        }

        // 3. API Đăng xuất hệ thống (Logout)
        @PostMapping("/logout")
        public ApiResponse<Void> logout(HttpServletResponse response) {
                // --- XÓA ROOT COOKIE CHỨA ACCESS TOKEN ---
                Cookie accessCookie = new Cookie("access_token", null); // Set giá trị rỗng
                accessCookie.setHttpOnly(true);
                accessCookie.setSecure(true);
                accessCookie.setPath("/");
                accessCookie.setMaxAge(0); // MaxAge = 0 ra lệnh cho trình duyệt XÓA Cookie này ngay lập tức

                // --- XÓA REFRESH COOKIE CHỨA REFRESH TOKEN ---
                Cookie refreshCookie = new Cookie("refresh_token", null);
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(true);
                refreshCookie.setPath("/api/v1/auth/refresh");
                refreshCookie.setMaxAge(0);

                // Ghi đè 2 Cookie rỗng này vào luồng Response trả về cho Frontend
                response.addCookie(accessCookie);
                response.addCookie(refreshCookie);

                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Đăng xuất thành công")
                                                .build())
                                .build();
        }

        // 4. API Làm mới Access Token (Refresh Token)
        // Khi Access Token (thời hạn ngắn 24h) bị hết hạn, Frontend sẽ gọi API này một
        // cách thầm lặng.
        // Trình duyệt sẽ tự động đính kèm Refresh Cookie (sống 30 ngày) vào request này
        // nhờ Path = "/api/v1/auth/refresh".
        @PostMapping("/refresh")
        public ApiResponse<Void> refreshToken(
                        @CookieValue(name = "refresh_token", required = false) String refreshToken,
                        HttpServletResponse response) {

                if (refreshToken == null) {
                        return ApiResponse.<Void>builder()
                                        .status(ApiResponse.Status.builder()
                                                        .code(401)
                                                        .message("Không tìm thấy Refresh Token")
                                                        .build())
                                        .build();
                }

                // Gọi Service xử lý sinh mã Access Token mới
                String newAccessToken = authService.refreshToken(refreshToken);

                // Cập nhật lại Access Token vào Cookie
                Cookie accessCookie = new Cookie("access_token", newAccessToken);
                accessCookie.setHttpOnly(true);
                accessCookie.setSecure(true);
                accessCookie.setPath("/");
                accessCookie.setMaxAge(24 * 60 * 60); // 24h

                response.addCookie(accessCookie);

                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Làm mới Token thành công")
                                                .build())
                                .build();
        }

        // ===================== QUÊN MẬT KHẨU (FORGOT PASSWORD) =====================

        // Bước 1: Yêu cầu gửi mã reset mật khẩu
        @PostMapping("/forgot-password")
        public ApiResponse<Map<String, Integer>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
                int expiryMinutes = passwordResetService.processForgotPassword(request);
                return ApiResponse.<Map<String, Integer>>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Mã khôi phục đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.")
                                                .build())
                                .data(Map.of("expireTime", expiryMinutes))
                                .build();
        }

        // Bước 2: Dùng mã từ email để đặt lại mật khẩu mới
        @PostMapping("/reset-password")
        public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
                passwordResetService.processResetPassword(request);
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại.")
                                                .build())
                                .build();
        }
}
