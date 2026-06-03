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
public class OrganizerDashboardResponse {
    private long totalEvents;
    private long totalTicketsSold; // Tổng vé (SUCCESS) của các sự kiện do mình tổ chức
    private long totalCapacity; // Tổng số chỗ ngồi (số lượng vé bán được cài đặt)
    private BigDecimal totalRevenue; // Doanh thu thực nhận
    private long totalCheckedIn; // Khách đã quét mã (USED)
}
