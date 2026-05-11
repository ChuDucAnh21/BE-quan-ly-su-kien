package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RegistrationResponse {
    private Long registrationId;
    private Long eventId;
    private LocalDateTime eventStartTime;
    private Long userId;
    private String registrationCode;
    private String status;
    private LocalDateTime createdAt;

    // Phục vụ luồng Thanh toán
    private String checkoutUrl;
    private String orderCode; // Mã đơn hàng để check trạng thái thanh toán
    private BigDecimal amount;
}
