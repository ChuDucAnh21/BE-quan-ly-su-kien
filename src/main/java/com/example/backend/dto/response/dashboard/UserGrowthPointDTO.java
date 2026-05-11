package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho biểu đồ Tăng trưởng người dùng (Line Chart - Multi-series).
 * Ví dụ: { "date": "2024-05-01", "count": 10, "type": "Customer" }
 * Trường "type" giúp Ant Design Charts phân biệt các đường trên cùng 1 biểu đồ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthPointDTO {
    // Ngày (dạng "yyyy-MM-dd")
    private String date;
    // Số lượng User đăng ký mới trong ngày đó
    private Long count;
    // Loại người dùng: "USER", "ORGANIZER", "STAFF"
    private String type;
}
