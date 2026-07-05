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

// import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
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

        // 1. Kiểm tra tính hợp lệ của cấu hình PayOS trước khi lưu
        try {
            // Sử dụng RestTemplate của Spring Boot để call API PayOS
            // Cách này sẽ bắt lỗi HTTP 401/400 cực kỳ chuẩn xác và không bị nuốt Exception
            // như HttpURLConnection
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", request.getClientId());
            headers.set("x-api-key", request.getApiKey());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Thử gọi lấy thông tin đơn hàng giả
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    "https://api-merchant.payos.vn/v2/payment-requests/123456",
                    HttpMethod.GET, entity, String.class);

            // ĐẶC BIỆT CHÚ Ý: PayOS luôn trả về HTTP Status 200 OK kể cả khi lỗi!
            // Mã lỗi thực sự nằm bên trong cấu trúc JSON của body: {"code":"401",
            // "desc":"Unauthorized"}
            String body = response.getBody();
            if (body != null) {
                log.info("PayOS Response Body: {}", body); // Log ra để dễ debug
                // Nếu PayOS xác thực thành công nhưng không tìm thấy đơn, mã trả về sẽ là "212"
                // (Payment link not found)
                // Nếu trả về bất kỳ mã nào khác (như "214", "401", "400"...) nghĩa là sai Key /
                // sai Client ID!
                if (body.contains("\"code\":\"214\"")) {
                    throw new AppException(ErrorCode.PAYMENT_CONFIG_INVALID);
                }
            }

            // Nếu chạy qua đoạn này mà không có lỗi 401/400 trong body thì key hợp lệ
        } catch (HttpClientErrorException e) {
            // Phòng hờ trường hợp PayOS thay đổi cấu trúc và trả về chuẩn HTTP 4xx
            int statusCode = e.getStatusCode().value();
            log.info("PayOS HTTP Status: {}", statusCode);
            if (statusCode == 401 || statusCode == 400 || statusCode == 403) {
                throw new AppException(ErrorCode.PAYMENT_CONFIG_INVALID);
            }
        } catch (AppException e) {
            throw e; // Ném lại đúng lỗi config
        } catch (Exception e) {
            // Các lỗi khác như mất mạng (ConnectException) thì bỏ qua để tránh block luồng
            // lưu dữ liệu
            log.error("Lỗi khi kết nối PayOS test: {}", e.getMessage());
        }

        // 2. Nếu hợp lệ, tiến hành lưu vào DB
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
