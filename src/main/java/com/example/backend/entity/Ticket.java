package com.example.backend.entity;

import com.example.backend.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Column(name = "qr_code", unique = true, nullable = false)
    private String qrCode; // Mã gốc, dùng để cấp cho khách

    @Column(name = "hashed_qr_code", unique = true, nullable = false)
    private String hashedQrCode; // Mã băm, dùng để API trả cho Staff offline

    @Column(name = "issued_at")
    private LocalDateTime issuedAt; // Thời gian phát hành vé

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt; // Thời gian check-in

    @Enumerated(EnumType.STRING)
    private TicketStatus status;
}
