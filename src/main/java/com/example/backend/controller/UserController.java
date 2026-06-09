package com.example.backend.controller;

import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.UserProfileResponse;
import com.example.backend.service.CloudinaryService;
import com.example.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users") // Toàn bộ API trong này yêu cầu đã đăng nhập (cấu hình trong SecurityConfig)
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;
        private final CloudinaryService cloudinaryService;

        // 1. API Lấy thông tin cá nhân của chính mình
        @GetMapping("/profile")
        public ApiResponse<UserProfileResponse> getMyProfile(Authentication authentication) {
                // Authentication: Đối tượng chứa thông tin người dùng đang đăng nhập (lấy từ
                // Token)
                String email = authentication.getName(); // Lấy email của người đang bơi trong hệ thống
                return ApiResponse.<UserProfileResponse>builder()
                                .data(userService.getMyProfile(email))
                                .build();
        }

        // 2. API Cập nhật thông tin cá nhân
        @PutMapping("/profile/update-info")
        public ApiResponse<UserProfileResponse> updateProfile(
                        Authentication authentication,
                        @RequestBody @Valid UpdateProfileRequest request) {
                String email = authentication.getName();
                return ApiResponse.<UserProfileResponse>builder()
                                .status(ApiResponse.Status.builder().message("Cập nhật thông tin thành công").build())
                                .data(userService.updateProfile(email, request))
                                .build();
        }

        // 3. API Tải ảnh đại diện lên
        @PutMapping("/profile/update-avatar")
        public ApiResponse<UserProfileResponse> updateAvatar(
                        Authentication authentication,
                        @RequestParam("file") MultipartFile file) throws IOException { // MultipartFile: Kiểu dữ liệu
                                                                                       // tệp tin gửi từ form
                String email = authentication.getName();

                // B1: Đẩy ảnh qua Cloudinary để lấy link ảnh https
                String avatarUrl = cloudinaryService.uploadImage(file);

                // B2: Cập nhật link ảnh đó vào DB cho User
                return ApiResponse.<UserProfileResponse>builder()
                                .status(ApiResponse.Status.builder().message("Cập nhật ảnh đại diện thành công")
                                                .build())
                                .data(userService.updateAvatar(email, avatarUrl))
                                .build();
        }

        // 4. API Đổi mật khẩu
        @PutMapping("/profile/password")
        public ApiResponse<Void> changePassword(
                        Authentication authentication,
                        @RequestBody @Valid ChangePasswordRequest request) {
                String email = authentication.getName();
                userService.changePassword(email, request);
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder().message("Thay đổi mật khẩu thành công").build())
                                .build();
        }

        // 5. API Khóa tài khoản của chính mình (Xóa mềm)
        @DeleteMapping("/profile-block")
        public ApiResponse<Void> deleteMyAccount(Authentication authentication) {
                String email = authentication.getName();
                userService.softDeleteMyAccount(email);
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder().message("Tài khoản đã được khóa thành công")
                                                .build())
                                .build();
        }

        // 6. API Lấy thông tin công khai của một người dùng bất kỳ (dựa vào ID)
        @GetMapping("/info-user/{id}")
        public ApiResponse<UserProfileResponse> getPublicProfileById(
                        @PathVariable Long id) {
                return ApiResponse.<UserProfileResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thông tin công khai thành công")
                                                .build())
                                .data(userService.getPublicProfileById(id))
                                .build();
        }

}
