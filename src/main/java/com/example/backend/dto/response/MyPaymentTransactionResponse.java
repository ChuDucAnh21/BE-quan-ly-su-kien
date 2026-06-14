package com.example.backend.dto.response;

import com.example.backend.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết của một giao dịch thanh toán cá nhân
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPaymentTransactionResponse {

    private Long paymentId;
    private String orderCode;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private String checkoutUrl;

    private Long eventId;
    private String eventTitle;
    private String eventImage;
    private LocalDateTime eventStartTime;
    private String registrationCode;

}
