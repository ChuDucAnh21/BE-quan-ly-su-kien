package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho biểu đồ Tần suất Check-in theo Khung giờ (Column Chart).
 * Ví dụ: { "hour": 18, "count": 120 }
 * Frontend hiển thị "18" thành "18:00 - 19:00".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInFrequencyDTO {
    // Khung giờ bắt đầu (0 -> 23)
    private Integer hour;
    // Số lượng khách check-in trong khung giờ đó
    private Long count;
}
