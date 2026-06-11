package com.example.backend.dto.response;

import com.example.backend.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EventResponse: Chứa dữ liệu Sự kiện trả về cho Client.
 *
 * ⚠️ GHI CHÚ VỀ TRƯỜNG 'schedules':
 *   - Chỉ được điền dữ liệu ở API Chi tiết: GET /events/{id}
 *   - Ở API danh sách: GET /events → luôn là NULL để tránh trả thừa dữ liệu,
 *     tối ưu hiệu năng khi render danh sách thẻ sự kiện.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;

    // Tên và ID của người tổ chức - dùng để hiển thị trên thẻ và trang chi tiết
    private String organizerName;
    private Long organizerId;
    private String organizerAvatar;

    private String title;
    private String category;
    private String shortDescription;
    private String description;

    private String bannerUrl; // URL ảnh bìa đã upload trên Cloudinary

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime registrationStartTime; // Thời gian mở bán vé
    private LocalDateTime registrationEndTime;   // Thời gian đóng bán vé

    private String location;

    private Integer capacity;       // Sức chứa tối đa của sự kiện
    private BigDecimal ticketPrice; // 0 = Miễn phí | > 0 = Có thu phí

    private EventStatus status; // DRAFT | PUBLISHED | CANCELLED

    // Số lượng vé đã bán thực tế (trạng thái CONFIRMED + CHECKED_IN)
    // Frontend dùng để tính "Còn X vé trống"
    private Long ticketsSold;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * [CHỈ CÓ TRONG API CHI TIẾT - GET /events/{id}]
     *
     * Danh sách các lịch trình con (Timeline) của sự kiện.
     * Dữ liệu đã được sắp xếp tăng dần theo startTime để Frontend render đúng thứ tự.
     *
     * Ví dụ kết quả trả về cho sự kiện "Đêm nhạc":
     *   - { id: 1, title: "Khai mạc", startTime: "19:00", endTime: "20:00" }
     *   - { id: 2, title: "Biểu diễn", startTime: "20:00", endTime: "23:00" }
     *   - { id: 3, title: "Bế mạc",   startTime: "23:00", endTime: "23:30" }
     *
     * Khi gọi GET /events (danh sách), field này = null (không tốn thêm DB query).
     */
    private List<EventScheduleResponse> schedules;
}
