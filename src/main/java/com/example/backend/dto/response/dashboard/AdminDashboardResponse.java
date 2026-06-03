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
    private long totalStaff;
    private long totalAdmin;
    private long totalEvents;
    private long totalTickets; // Tổng số vé hệ thống đã bán ra (SUCCESS)
    private BigDecimal totalRevenueSuccess; // Tổng luân chuyển dòng tiền thành công qua hệ thống
    private BigDecimal totalRevenueFail; // Tổng luân chuyển dòng tiền thất bại qua hệ thống
}
