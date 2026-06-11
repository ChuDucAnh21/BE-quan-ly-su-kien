package com.example.backend.entity;

import java.time.LocalDateTime;

import com.example.backend.enums.CheckInMethod;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checkin_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CheckInLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // nghĩa là nhiều bản ghi CheckInLog có thể thuộc về 1 Vé || fetch =
                                       // FetchType.LAZY: Tải dữ liệu ticket khi cần
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_in_by", nullable = false) // nghĩa là nhiều bản ghi CheckInLog có thể thuộc về 1 Nhân
                                                          // viên
    private User checkedInBy;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Enumerated(EnumType.STRING) // nghĩa là phương thức checkin là 1 enum
    private CheckInMethod method;
}
