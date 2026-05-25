package com.example.backend.dto.response;

import com.example.backend.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết của một giao dịch thanh toán
 * Dùng cho màn hình quản lý đơn hàng của Admin / Organizer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponse {
    
    // ID nội bộ của giao dịch
    private Long paymentId;

    // Mã đơn hàng liên kết với PayOS
    private String orderCode;

    // Số tiền giao dịch
    private BigDecimal amount;

    // Trạng thái thanh toán (SUCCESS, PENDING, FAILED, CANCELLED)
    private PaymentStatus status;

    // Thời gian thanh toán thành công (nếu có)
    private LocalDateTime paidAt;

    // Thời gian tạo đơn hàng
    private LocalDateTime createdAt;

    // Mã đăng ký (Registration code) liên kết với giao dịch này
    private String registrationCode;

    // Email của người dùng thực hiện giao dịch
    private String userEmail;

    // Họ tên của người dùng thực hiện giao dịch
    private String userFullName;
}
