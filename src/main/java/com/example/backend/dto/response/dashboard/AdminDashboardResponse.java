package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalOrganizers;
    private long totalEvents;
    private long totalTickets; // Tổng số vé hệ thống đã bán ra (SUCCESS)
    private BigDecimal totalRevenue; // Tổng luân chuyển dòng tiền qua hệ thống
}
