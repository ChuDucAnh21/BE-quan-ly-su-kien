package com.example.backend.entity;

import com.example.backend.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity // Thực thể đại diện cho các Sự kiện trong hệ thống
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng khi tạo mới
    private Long id;

    // Người tổ chức sự kiện (Liên kết tới bảng User)
    @ManyToOne(fetch = FetchType.LAZY) // Nhiều sự kiện có thể thuộc về 1 người tổ chức
    @JoinColumn(name = "organizer_id", nullable = false) // Khóa ngoại tham chiếu đến User
    private User organizer;

    private String title;

    private String category;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "registration_start_time")
    private LocalDateTime registrationStartTime;

    @Column(name = "registration_end_time")
    private LocalDateTime registrationEndTime;

    private String location;

    private Integer capacity;

    @Column(name = "ticket_price")
    private BigDecimal ticketPrice;

    @Column(name = "sold_tickets", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer soldTickets = 0;

    @Enumerated(EnumType.STRING) // Lưu trạng thái dưới dạng chuỗi (ví dụ: "PUBLISHED")
    private EventStatus status;
}
