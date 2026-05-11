package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dùng chung cho các biểu đồ dạng "nhãn - số lượng".
 * Ví dụ: { "label": "ADMIN", "count": 5 }
 * Dùng cho: Cơ cấu tài khoản, Trạng thái sự kiện, Phân loại danh mục...
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartPointDTO {
    // Nhãn trục X (tên danh mục, tên vai trò, tên trạng thái...)
    private String label;
    // Giá trị trục Y (số lượng)
    private Long count;
}
