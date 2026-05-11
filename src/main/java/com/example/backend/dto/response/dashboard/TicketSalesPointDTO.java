package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho biểu đồ Biến động bán vé theo Ngày (Organizer).
 * Ví dụ: { "date": "2024-05-01", "value": 12 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSalesPointDTO {
    // Ngày (dạng "yyyy-MM-dd")
    private String date;
    // Số lượng vé bán được trong ngày đó
    private Long value;
}
