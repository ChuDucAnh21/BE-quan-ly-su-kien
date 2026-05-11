package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho biểu đồ Cột Chồng (Stacked Column) - Số lượng vé Free vs Trả Phí theo Tháng.
 * Ví dụ: { "month": "2024-05", "count": 450, "type": "VÉ TRẢ PHÍ" }
 * Trường "type" giúp Ant Design Charts phân biệt 2 nhóm cột chồng nhau.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypePointDTO {
    // Tháng (dạng "yyyy-MM")
    private String month;
    // Số lượng vé trong tháng đó
    private Long count;
    // Loại vé: "VÉ MIỄN PHÍ" hoặc "VÉ TRẢ PHÍ"
    private String type;
}
