package com.example.backend.service;

import com.example.backend.dto.request.PaymentConfigRequest;
import com.example.backend.dto.response.PaymentConfigResponse;

public interface PaymentConfigService {
    
    // Lấy cấu hình của User đang đăng nhập
    PaymentConfigResponse getMyPaymentConfig();

    // Thêm mới hoặc cập nhật cấu hình PayOS
    PaymentConfigResponse upsertPaymentConfig(PaymentConfigRequest request);
}
