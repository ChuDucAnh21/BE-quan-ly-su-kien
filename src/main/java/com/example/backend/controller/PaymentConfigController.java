package com.example.backend.controller;

import com.example.backend.dto.request.PaymentConfigRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PaymentConfigResponse;
import com.example.backend.service.PaymentConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-configs")
@RequiredArgsConstructor
public class PaymentConfigController {

    private final PaymentConfigService paymentConfigService;

    // Xem cấu hình PayOS của bản thân (Chỉ Organizer)
    @GetMapping("/me")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<PaymentConfigResponse>> getMyPaymentConfig() {
        PaymentConfigResponse data = paymentConfigService.getMyPaymentConfig();
        ApiResponse<PaymentConfigResponse> response = ApiResponse.<PaymentConfigResponse>builder()
                .status(ApiResponse.Status.builder()
                        .code(200)
                        .message("Lấy cấu hình thanh toán thành công")
                        .build())
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    // Thiết lập/Cập nhật cấu hình PayOS
    @PutMapping("/me")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<PaymentConfigResponse>> upsertPaymentConfig(
            @Valid @RequestBody PaymentConfigRequest request) {
        PaymentConfigResponse data = paymentConfigService.upsertPaymentConfig(request);
        ApiResponse<PaymentConfigResponse> response = ApiResponse.<PaymentConfigResponse>builder()
                .status(ApiResponse.Status.builder()
                        .code(200)
                        .message("Cập nhật cấu hình thanh toán thành công")
                        .build())
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
