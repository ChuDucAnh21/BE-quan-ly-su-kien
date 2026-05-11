package com.example.backend.service.impl;

import com.example.backend.dto.request.PaymentConfigRequest;
import com.example.backend.dto.response.PaymentConfigResponse;
import com.example.backend.entity.PaymentConfig;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.PaymentConfigRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PaymentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentConfigServiceImpl implements PaymentConfigService {

    private final PaymentConfigRepository paymentConfigRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // hàm chuyển sang dạng DTO trả về cho client
    private PaymentConfigResponse mapToResponse(PaymentConfig config) {
        return PaymentConfigResponse.builder()
                .id(config.getId())
                .clientId(config.getClientId())
                .apiKey(config.getApiKey())
                .checksumKey(config.getChecksumKey())
                .isActive(config.getIsActive())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    @Override
    public PaymentConfigResponse getMyPaymentConfig() {
        User user = getCurrentUser();
        PaymentConfig config = paymentConfigRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_CONFIG_NOT_FOUND));
        return mapToResponse(config);
    }

    // Hàm tạo mới hoặc cập nhật cấu hình cổng thanh toán
    @Override
    @Transactional
    public PaymentConfigResponse upsertPaymentConfig(PaymentConfigRequest request) {
        User user = getCurrentUser();
        Optional<PaymentConfig> configOpt = paymentConfigRepository.findByUserId(user.getId());

        PaymentConfig config;
        if (configOpt.isPresent()) { // kiểm tra đã cấu hình chưa
            config = configOpt.get();
            config.setClientId(request.getClientId());
            config.setApiKey(request.getApiKey());
            config.setChecksumKey(request.getChecksumKey());
            // Kích hoạt lại nếu trước đó bị tắt
            config.setIsActive(true);
        } else {
            config = PaymentConfig.builder()
                    .user(user)
                    .clientId(request.getClientId())
                    .apiKey(request.getApiKey())
                    .checksumKey(request.getChecksumKey())
                    .isActive(true)
                    .build();
        }

        return mapToResponse(paymentConfigRepository.save(config));
    }
}
