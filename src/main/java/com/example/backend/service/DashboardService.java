package com.example.backend.service;

import com.example.backend.dto.response.dashboard.AdminDashboardResponse;
import com.example.backend.dto.response.dashboard.ChartPointDTO;
import com.example.backend.dto.response.dashboard.CheckInFrequencyDTO;
import com.example.backend.dto.response.dashboard.EventStatisticsResponse;
import com.example.backend.dto.response.dashboard.OrganizerDashboardResponse;
import com.example.backend.dto.response.dashboard.RevenuePointDTO;
import com.example.backend.dto.response.dashboard.TicketSalesPointDTO;
import com.example.backend.dto.response.dashboard.TicketTypePointDTO;
import com.example.backend.dto.response.dashboard.UserGrowthPointDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    // ===================== API TỔNG QUAN =====================
    AdminDashboardResponse getAdminDashboard(LocalDate from, LocalDate to);
    OrganizerDashboardResponse getOrganizerDashboard(LocalDate from, LocalDate to);

    // ===================== BIỂU ĐỒ ADMIN =====================

    /** Cơ cấu tài khoản theo Role. from/to null → All-time */
    List<ChartPointDTO> getRoleDistribution(LocalDate from, LocalDate to);

    /** Tăng trưởng user theo Ngày */
    List<UserGrowthPointDTO> getUserGrowthChart(LocalDate from, LocalDate to);

    /** Top 5 Organizer có nhiều sự kiện nhất */
    List<ChartPointDTO> getTop5OrganizersChart(LocalDate from, LocalDate to);

    /** Số sự kiện mới tạo theo Tháng */
    List<ChartPointDTO> getMonthlyEventsChart(LocalDate from, LocalDate to);

    /** Tỉ lệ các trạng thái sự kiện */
    List<ChartPointDTO> getEventStatusChart(LocalDate from, LocalDate to);

    /** Sự kiện phân theo Danh mục */
    List<ChartPointDTO> getEventCategoryChart(LocalDate from, LocalDate to);

    /** Doanh thu theo Ngày */
    List<RevenuePointDTO> getRevenueChart(LocalDate from, LocalDate to);

    /** Số vé Free/Trả phí theo Tháng */
    List<TicketTypePointDTO> getTicketTypeChart(LocalDate from, LocalDate to);

    /** Top 5 Sự kiện nổi bật (Theo doanh thu và Số vé bán ra) */
    com.example.backend.dto.response.dashboard.TopEventsChartResponse getTopEventsChart(LocalDate from, LocalDate to);

    // ===================== BIỂU ĐỒ ORGANIZER =====================

    /** Tỉ lệ lấp đầy (Đã bán vs Còn trống). from/to null → All-time */
    List<ChartPointDTO> getOccupancyChart(LocalDate from, LocalDate to);

    /** Biến động bán vé theo Ngày */
    List<TicketSalesPointDTO> getTicketSalesTrendChart(LocalDate from, LocalDate to);

    /** Cơ cấu khách (Trả phí vs Miễn phí). from/to null → All-time */
    List<ChartPointDTO> getAttendeeStructureChart(LocalDate from, LocalDate to);

    /** Trạng thái check-in (Đã vào / Chưa vào). from/to null → All-time */
    List<ChartPointDTO> getCheckInStatusChart(LocalDate from, LocalDate to);

    /** Tần suất check-in theo Khung giờ */
    List<CheckInFrequencyDTO> getCheckInFrequencyChart(LocalDate from, LocalDate to);

    // ===================== THỐNG KÊ SỰ KIỆN CỤ THỂ =====================

    /**
     * Thống kê toàn diện cho 1 sự kiện.
     * @param from Lọc biểu đồ từ ngày (null = mặc định từ ngày mở đăng ký)
     * @param to   Lọc biểu đồ đến ngày (null = mặc định hôm nay)
     */
    EventStatisticsResponse getEventStatistics(Long eventId, LocalDate from, LocalDate to);
}
