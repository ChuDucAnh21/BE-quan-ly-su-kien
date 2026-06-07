package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.dashboard.*;
import com.example.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
@Tag(name = "Dashboards", description = "Các API thống kê phục vụ màn hình Báo cáo (Admin/Organizer). "
                + "Tham số `from`/`to` là tùy chọn: nếu không truyền → All-time hoặc 30 ngày gần nhất tuỳ từng API.")
public class DashboardController {

        private final DashboardService dashboardService;

        // ============================================================
        // API TỔNG QUAN (S=TATISTIC CARDS)
        // ===========================================================

        @Operation(summary = "Thống kê tổng quan Admin", description = "Tổng User, Event, Vé, Doanh thu toàn hệ thống.")
        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<AdminDashboardResponse> getAdminDashboard(
                        @Parameter(description = "Từ ngày (yyyy-MM-dd). Null = All-time") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @Parameter(description = "Đến ngày (yyyy-MM-dd). Null = All-time") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<AdminDashboardResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê Admin thành công").build())
                                .data(dashboardService.getAdminDashboard(from, to))
                                .build();
        }

        @Operation(summary = "Thống kê tổng quan Organizer", description = "Hiệu suất cá nhân của Organizer.")
        @GetMapping("/organizer")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<OrganizerDashboardResponse> getOrganizerDashboard(
                        @Parameter(description = "Từ ngày (yyyy-MM-dd). Null = All-time") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @Parameter(description = "Đến ngày (yyyy-MM-dd). Null = All-time") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<OrganizerDashboardResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê Organizer thành công")
                                                .build())
                                .data(dashboardService.getOrganizerDashboard(from, to))
                                .build();
        }

        // ============================================================
        // BIỂU ĐỒ ADMIN
        // ============================================================

        @Operation(summary = "[Admin] Cơ cấu tài khoản (Donut Chart)", description = "Tỉ lệ các loại tài khoản: ADMIN, ORGANIZER, STAFF, USER. "
                        + "Không truyền from/to → All-time. Truyền from/to → chỉ tính user đăng ký trong khoảng đó.")
        @GetMapping("/admin/role-distribution")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getRoleDistribution(
                        @Parameter(description = "Từ ngày (yyyy-MM-dd). Null = All-time") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @Parameter(description = "Đến ngày (yyyy-MM-dd). Null = All-time") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getRoleDistribution(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Tăng trưởng người dùng (Line Chart)", description = "User mới đăng ký theo Ngày, phân loại theo Role. Mặc định 30 ngày gần nhất.")
        @GetMapping("/admin/user-growth")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<UserGrowthPointDTO>> getUserGrowthChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<UserGrowthPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getUserGrowthChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Top 5 Organizer (Bar Chart)", description = "Top Organizer có nhiều sự kiện nhất. Không truyền from/to → All-time.")
        @GetMapping("/admin/top-organizers")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getTop5Organizers(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getTop5OrganizersChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Sự kiện mới theo Tháng (Column Chart)", description = "Số lượng sự kiện mới tạo theo từng tháng. Mặc định 30 ngày gần nhất.")
        @GetMapping("/admin/monthly-events")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getMonthlyEventsChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getMonthlyEventsChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Trạng thái sự kiện (Pie Chart)", description = "Tỉ lệ DRAFT, PUBLISHED, CANCELLED... Không truyền from/to → All-time.")
        @GetMapping("/admin/event-status")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getEventStatusChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getEventStatusChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Sự kiện theo Danh mục (Horizontal Bar Chart)", description = "Số lượng sự kiện phân theo danh mục. Không truyền from/to → All-time.")
        @GetMapping("/admin/event-category")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getEventCategoryChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getEventCategoryChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Doanh thu theo Ngày (Bar Chart)", description = "Tổng doanh thu PayOS theo từng ngày. Mặc định 30 ngày gần nhất.")
        @GetMapping("/admin/revenue-stats")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<RevenuePointDTO>> getRevenueChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<RevenuePointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getRevenueChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Vé Free vs Trả phí theo Tháng (Stacked Column Chart)", description = "Số vé miễn phí và trả phí theo từng tháng. Mặc định 30 ngày gần nhất.")
        @GetMapping("/admin/ticket-type-stats")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<TicketTypePointDTO>> getTicketTypeChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<TicketTypePointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getTicketTypeChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Top 10 Sự kiện nổi bật (Bar/Column Chart)", description = "Trả về 2 luồng dữ liệu: Top 10 theo Doanh thu và Top 10 theo Số vé bán ra.")
        @GetMapping("/admin/top-events")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<TopEventsChartResponse> getTopEventsChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<TopEventsChartResponse>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getTopEventsChart(from, to))
                                .build();
        }

        // ============================================================
        // BIỂU ĐỒ ORGANIZER
        // ============================================================

        @Operation(summary = "[Organizer] Tỉ lệ lấp đầy (Pie Chart)", description = "Vé đã bán và còn trống trên tổng capacity. Không truyền from/to → All-time.")
        @GetMapping("/organizer/occupancy")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<ChartPointDTO>> getOccupancyChart(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getOccupancyChart(from, to))
                                .build();
        }

        @Operation(summary = "[Organizer] Biến động bán vé (Line Chart)", description = "Số lượng vé bán ra theo từng ngày. Mặc định 30 ngày gần nhất.")
        @GetMapping("/organizer/sales-trend")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<TicketSalesPointDTO>> getTicketSalesTrend(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<TicketSalesPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getTicketSalesTrendChart(from, to))
                                .build();
        }

        @Operation(summary = "[Organizer] Cơ cấu khách tham gia (Pie Chart)", description = "Tỉ lệ khách mua vé trả phí và vé miễn phí. Không truyền from/to → All-time.")
        @GetMapping("/organizer/attendee-structure")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<ChartPointDTO>> getAttendeeStructure(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getAttendeeStructureChart(from, to))
                                .build();
        }

        @Operation(summary = "[Organizer] Trạng thái Check-in (Pie Chart)", description = "Số khách đã check-in và chưa check-in. Không truyền from/to → All-time.")
        @GetMapping("/organizer/checkin-status")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<ChartPointDTO>> getCheckInStatus(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getCheckInStatusChart(from, to))
                                .build();
        }

        @Operation(summary = "[Organizer] Tần suất Check-in theo giờ (Column Chart)", description = "Số lượng check-in phân theo khung giờ. Mặc định 30 ngày gần nhất.")
        @GetMapping("/organizer/checkin-frequency")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<CheckInFrequencyDTO>> getCheckInFrequency(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<CheckInFrequencyDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getCheckInFrequencyChart(from, to))
                                .build();
        }

        // ============================================================
        // THỐNG KÊ SỰ KIỆN CỤ THỂ
        // ============================================================

        @Operation(summary = "Thống kê chi tiết 1 sự kiện", description = "Vé bán/còn/đã check-in, doanh thu, tỉ lệ lấp đầy, biểu đồ bán vé theo ngày và check-in theo giờ. "
                        + "ADMIN xem mọi sự kiện; ORGANIZER chỉ xem sự kiện của chính mình. "
                        + "Tham số from/to dùng để lọc toàn bộ số liệu thống kê và biểu đồ theo khoảng thời gian.")
        @GetMapping("/events/{eventId}/statistics")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
        public ApiResponse<EventStatisticsResponse> getEventStatistics(
                        @PathVariable Long eventId,
                        @Parameter(description = "Từ ngày lọc biểu đồ (yyyy-MM-dd). Null = từ ngày mở đăng ký sự kiện") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @Parameter(description = "Đến ngày lọc biểu đồ (yyyy-MM-dd). Null = hôm nay") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<EventStatisticsResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê sự kiện thành công").build())
                                .data(dashboardService.getEventStatistics(eventId, from, to))
                                .build();
        }
}
