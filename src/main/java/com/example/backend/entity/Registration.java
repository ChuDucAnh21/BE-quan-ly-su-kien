package com.example.backend.entity;

import com.example.backend.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "event_id", "user_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)

// bảng thông tin đăng ký vé
public class Registration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Mã đăng ký vé
    @Column(name = "registration_code", unique = true, nullable = false)
    private String registrationCode;

    // Thời gian check-in
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;
}
