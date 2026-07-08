package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend.enums.EventStatus;

@Data
@Builder
public class RegistrationResponse {
    private Long registrationId;
    private Long eventId;
    private String eventName;
    private BigDecimal eventPrice;
    private String eventImage;
    private EventStatus eventStatus;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private Boolean isExpiredRegistration;
    private Long userId;
    private String registrationCode;
    private String status;
    private LocalDateTime createdAt;

    // Phục vụ luồng Thanh toán
    private String checkoutUrl;
    private String orderCode; // Mã đơn hàng để check trạng thái thanh toán
    private BigDecimal amount;
}
