package com.example.backend.service;

import com.example.backend.dto.response.dashboard.AdminDashboardResponse;
import com.example.backend.dto.response.dashboard.ChartPointDTO;
import com.example.backend.dto.response.dashboard.CheckInFrequencyDTO;
import com.example.backend.dto.response.dashboard.OrganizerDashboardResponse;
import com.example.backend.dto.response.dashboard.RevenuePointDTO;
import com.example.backend.dto.response.dashboard.TicketSalesPointDTO;
import com.example.backend.dto.response.dashboard.TicketTypePointDTO;
import com.example.backend.dto.response.dashboard.UserGrowthPointDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    // ===================== API TỔNG QUAN (ĐÃ CÓ) =====================
    AdminDashboardResponse getAdminDashboard();
    OrganizerDashboardResponse getOrganizerDashboard();

    // ===================== API BIỂU ĐỒ ADMIN =====================

    /** Biểu đồ Tròn: Cơ cấu tài khoản theo Role (All-time) */
    List<ChartPointDTO> getRoleDistribution();

    /** Biểu đồ Đường: Tăng trưởng user đăng ký mới theo Ngày (lọc theo from/to) */
    List<UserGrowthPointDTO> getUserGrowthChart(LocalDate from, LocalDate to);

    /** Biểu đồ Cột: Top 5 Organizer có nhiều sự kiện nhất (All-time) */
    List<ChartPointDTO> getTop5OrganizersChart();

    /** Biểu đồ Cột: Số sự kiện mới tạo theo Tháng (lọc theo from/to) */
    List<ChartPointDTO> getMonthlyEventsChart(LocalDate from, LocalDate to);

    /** Biểu đồ Tròn: Tỉ lệ các trạng thái sự kiện (All-time) */
    List<ChartPointDTO> getEventStatusChart();

    /** Biểu đồ Cột ngang: Sự kiện phân theo Danh mục (All-time) */
    List<ChartPointDTO> getEventCategoryChart();

    /** Biểu đồ Cột: Doanh thu theo Ngày (lọc theo from/to) */
    List<RevenuePointDTO> getRevenueChart(LocalDate from, LocalDate to);

    /** Biểu đồ Cột Chồng: Số vé Free/Trả phí theo Tháng (lọc theo from/to) */
    List<TicketTypePointDTO> getTicketTypeChart(LocalDate from, LocalDate to);

    // ===================== API BIỂU ĐỒ ORGANIZER =====================

    /** Biểu đồ Tròn: Tỉ lệ lấp đầy (Đã bán vs Còn trống) của sự kiện Organizer (All-time) */
    List<ChartPointDTO> getOccupancyChart();

    /** Biểu đồ Đường: Biến động bán vé theo Ngày của Organizer (lọc theo from/to) */
    List<TicketSalesPointDTO> getTicketSalesTrendChart(LocalDate from, LocalDate to);

    /** Biểu đồ Tròn: Cơ cấu khách (Trả phí vs Miễn phí) của Organizer (All-time) */
    List<ChartPointDTO> getAttendeeStructureChart();

    /** Biểu đồ Tròn: Trạng thái check-in (Đã vào / Chưa vào) của Organizer (All-time) */
    List<ChartPointDTO> getCheckInStatusChart();

    /** Biểu đồ Cột: Tần suất check-in theo Khung giờ của Organizer (lọc theo from/to) */
    List<CheckInFrequencyDTO> getCheckInFrequencyChart(LocalDate from, LocalDate to);
}
