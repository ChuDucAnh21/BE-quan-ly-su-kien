package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Thống kê chi tiết dành cho 1 Sự kiện cụ thể.
 * Dùng cho trang quản lý chi tiết event của Organizer/Admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatisticsResponse {

    // =================== THÔNG TIN CƠ BẢN SỰ KIỆN ===================
    private Long eventId;
    private String eventTitle;
    private String status;

    // =================== THỐNG KÊ VÉ ===================
    /** Tổng số chỗ của sự kiện */
    private Integer capacity;

    /** Số vé đã bán (CONFIRMED + CHECKED_IN) */
    private long totalTicketsSold;

    /** Số vé còn trống */
    private long ticketsRemaining;

    /** Tỉ lệ lấp đầy (%) */
    private double occupancyRate;

    /** Số vé đã check-in (USED) */
    private long checkedInCount;

    /** Số vé chưa check-in (ACTIVE — đã mua nhưng chưa vào) */
    private long notCheckedInCount;

    /** Tỉ lệ check-in (%) */
    private double checkInRate;

    // =================== THỐNG KÊ DOANH THU ===================
    /** Giá vé */
    private BigDecimal ticketPrice;

    /** Tổng doanh thu (chỉ tính giao dịch SUCCESS) */
    private BigDecimal totalRevenue;

    /** Số giao dịch thành công */
    private long successPaymentCount;

    /** Số giao dịch đang chờ thanh toán */
    private long pendingPaymentCount;

    /** Số giao dịch bị hủy */
    private long cancelledPaymentCount;

    // =================== BIỂU ĐỒ BÁN VÉ THEO NGÀY ===================
    /** Biến động bán vé theo ngày (Line Chart) */
    private List<ChartPointDTO> ticketSalesByDay;

    // =================== BIỂU ĐỒ CHECK-IN THEO GIỜ ===================
    /** Tần suất check-in theo khung giờ (Column Chart) */
    private List<ChartPointDTO> checkInByHour;
}
