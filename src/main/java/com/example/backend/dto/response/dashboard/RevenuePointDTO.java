package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho biểu đồ Doanh thu theo Ngày.
 * Ví dụ: { "date": "2024-05-01", "revenue": 5000000 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenuePointDTO {
    // Ngày (dạng "yyyy-MM-dd")
    private String date;
    // Tổng doanh thu trong ngày đó
    private BigDecimal revenue;
}
