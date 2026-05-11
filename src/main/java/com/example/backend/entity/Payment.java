package com.example.backend.entity;

import com.example.backend.enums.PaymentProvider;
import com.example.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)

// bảng thông tin thanh toán
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 1 đăng ký có thể có nhiều thanh toán, nhưng ở đây ta chỉ có 1 thanh toán là
                                       // thành công
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Column(name = "order_code", unique = true)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "checkout_url")
    private String checkoutUrl; // url để chuyển hướng user đến trang thanh toán

    @Column(name = "payment_link_id")
    private String paymentLinkId; // ID của link thanh toán trên PayOS

    @Column(name = "paid_at")
    private LocalDateTime paidAt; // thời gian thanh toán thành công

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse; // response từ PayOS
}
