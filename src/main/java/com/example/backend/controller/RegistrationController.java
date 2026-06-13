package com.example.backend.controller;

import com.example.backend.dto.request.RegistrationRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.RegistrationResponse;
import com.example.backend.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationController {

        private final RegistrationService registrationService;

        // Đăng ký tham gia Sự kiện
        // Lưu ý: Mọi User đã Đăng nhập đều có quyền Đăng ký mua vé
        @PostMapping
        @PreAuthorize("hasRole('USER') or hasRole('ORGANIZER') or hasRole('ADMIN') or hasRole('STAFF')")
        public ResponseEntity<ApiResponse<RegistrationResponse>> registerEvent(
                        @Valid @RequestBody RegistrationRequest request) {

                RegistrationResponse data = registrationService.registerEvent(request);

                ApiResponse<RegistrationResponse> response = ApiResponse.<RegistrationResponse>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(201)
                                                .message("Đăng ký thành công. Vui lòng thanh toán nếu cần.")
                                                .build())
                                .data(data)
                                .build();

                return ResponseEntity.status(201).body(response);
        }

        // 2. Hủy đăng ký vé (Chỉ áp dụng cho Vé Miễn Phí hoặc vé chưa thanh toán)
        @PostMapping("/{registrationId}/cancel")
        @PreAuthorize("hasRole('USER') or hasRole('ORGANIZER') or hasRole('ADMIN') or hasRole('STAFF')")
        public ResponseEntity<ApiResponse<Void>> cancelRegistration(@PathVariable Long registrationId) {
                registrationService.cancelRegistration(registrationId);

                ApiResponse<Void> response = ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Đã hủy đăng ký thành công. Hệ thống đã nhả lại suất trống cho những người khác.")
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }
}
