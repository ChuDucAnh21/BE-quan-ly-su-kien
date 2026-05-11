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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người tổ chức sự kiện (Liên kết tới bảng User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    private String title; // Tên sự kiện

    private String category; // Danh mục (Công nghệ, Ca nhạc...)

    @Column(name = "short_description")
    private String shortDescription; // Mô tả ngắn

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết (TEXT để chứa được nhiều ký tự)

    @Column(name = "banner_url")
    private String bannerUrl; // Ảnh bìa sự kiện

    @Column(name = "start_time")
    private LocalDateTime startTime; // Thời gian bắt đầu sự kiện

    @Column(name = "end_time")
    private LocalDateTime endTime; // Thời gian kết thúc sự kiện

    @Column(name = "registration_start_time")
    private LocalDateTime registrationStartTime; // Thời gian mở bán vé

    @Column(name = "registration_end_time")
    private LocalDateTime registrationEndTime; // Thời gian đóng bán vé

    private String location; // Địa điểm tổ chức

    private Integer capacity; // Sức chứa tối đa (số lượng vé)

    @Column(name = "ticket_price")
    private BigDecimal ticketPrice; // Giá vé (Dùng BigDecimal để tính toán tiền tệ chính xác)

    @Column(name = "sold_tickets", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer soldTickets = 0; // Số vé đã bán (Được tự động tăng khi khách đăng ký)

    @Enumerated(EnumType.STRING)
    private EventStatus status; // Trạng thái sự kiện (DRAFT, PUBLISHED, CANCELLED...)
}
