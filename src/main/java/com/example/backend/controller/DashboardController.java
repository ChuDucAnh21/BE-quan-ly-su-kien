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
@Tag(name = "Dashboards", description = "Các API thống kê phục vụ màn hình Báo cáo (Admin/Organizer)")
public class DashboardController {

        private final DashboardService dashboardService;

        // ============================================================
        // CÁC API TỔNG QUAN (STATISTIC CARDS)
        // ============================================================

        @Operation(summary = "Thống kê tổng quan Admin",
                description = "Trả về các chỉ số tổng quan toàn hệ thống (Tổng User, Event, Vé, Doanh thu). Yêu cầu quyền ADMIN.")
        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<AdminDashboardResponse> getAdminDashboard() {
                return ApiResponse.<AdminDashboardResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê Admin thành công").build())
                                .data(dashboardService.getAdminDashboard())
                                .build();
        }

        @Operation(summary = "Thống kê tổng quan Organizer",
                description = "Trả về các chỉ số hiệu suất cá nhân của Organizer. Yêu cầu quyền ORGANIZER.")
        @GetMapping("/organizer")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<OrganizerDashboardResponse> getOrganizerDashboard() {
                return ApiResponse.<OrganizerDashboardResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê Organizer thành công").build())
                                .data(dashboardService.getOrganizerDashboard())
                                .build();
        }

        // ============================================================
        // CÁC API BIỂU ĐỒ ADMIN
        // ============================================================

        @Operation(summary = "[Admin] Cơ cấu tài khoản (Donut Chart)",
                description = "Trả về tỉ lệ các loại tài khoản: ADMIN, ORGANIZER, STAFF, USER. Không lọc thời gian.")
        @GetMapping("/admin/role-distribution")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getRoleDistribution() {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getRoleDistribution())
                                .build();
        }

        @Operation(summary = "[Admin] Tăng trưởng người dùng (Line Chart)",
                description = "Trả về số lượng user mới đăng ký theo Ngày, phân loại theo Role. Hỗ trợ lọc `from`/`to`.")
        @GetMapping("/admin/user-growth")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<UserGrowthPointDTO>> getUserGrowthChart(
                        @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd), mặc định 30 ngày trước")
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @Parameter(description = "Ngày kết thúc (yyyy-MM-dd), mặc định hôm nay")
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<UserGrowthPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getUserGrowthChart(from, to))
                                .build();
        }

        @Operation(summary = "[Admin] Top 5 Organizer có nhiều sự kiện (Bar Chart)",
                description = "Trả về danh sách 5 Organizer có nhiều sự kiện nhất. Không lọc thời gian.")
        @GetMapping("/admin/top-organizers")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getTop5Organizers() {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getTop5OrganizersChart())
                                .build();
        }

        @Operation(summary = "[Admin] Sự kiện mới theo Tháng (Column Chart)",
                description = "Trả về số lượng sự kiện mới tạo theo từng tháng. Hỗ trợ lọc `from`/`to`.")
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

        @Operation(summary = "[Admin] Trạng thái sự kiện (Pie Chart)",
                description = "Trả về tỉ lệ các trạng thái sự kiện: DRAFT, PUBLISHED, CANCELLED... Không lọc thời gian.")
        @GetMapping("/admin/event-status")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getEventStatusChart() {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getEventStatusChart())
                                .build();
        }

        @Operation(summary = "[Admin] Sự kiện theo Danh mục (Horizontal Bar Chart)",
                description = "Trả về số lượng sự kiện phân theo từng danh mục. Không lọc thời gian.")
        @GetMapping("/admin/event-category")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getEventCategoryChart() {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getEventCategoryChart())
                                .build();
        }

        @Operation(summary = "[Admin] Doanh thu theo Ngày (Bar Chart)",
                description = "Trả về tổng doanh thu PayOS từ các giao dịch thành công theo từng ngày. Hỗ trợ lọc `from`/`to`.")
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

        @Operation(summary = "[Admin] Vé Free vs Trả phí theo Tháng (Stacked Column Chart)",
                description = "Trả về số lượng vé miễn phí và trả phí theo từng tháng. Hỗ trợ lọc `from`/`to`.")
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

        // ============================================================
        // CÁC API BIỂU ĐỒ ORGANIZER
        // ============================================================

        @Operation(summary = "[Organizer] Tỉ lệ lấp đầy (Pie Chart)",
                description = "Trả về số vé đã bán và còn trống trên tổng capacity các sự kiện của Organizer. Không lọc thời gian.")
        @GetMapping("/organizer/occupancy")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<ChartPointDTO>> getOccupancyChart() {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getOccupancyChart())
                                .build();
        }

        @Operation(summary = "[Organizer] Biến động bán vé (Line Chart)",
                description = "Trả về số lượng vé được bán ra theo từng ngày. Hỗ trợ lọc `from`/`to`.")
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

        @Operation(summary = "[Organizer] Cơ cấu khách tham gia (Pie Chart)",
                description = "Trả về tỉ lệ khách mua vé trả phí và khách nhận vé miễn phí. Không lọc thời gian.")
        @GetMapping("/organizer/attendee-structure")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<ChartPointDTO>> getAttendeeStructure() {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getAttendeeStructureChart())
                                .build();
        }

        @Operation(summary = "[Organizer] Trạng thái Check-in (Pie Chart)",
                description = "Trả về số khách đã check-in và chưa check-in. Không lọc thời gian.")
        @GetMapping("/organizer/checkin-status")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<List<ChartPointDTO>> getCheckInStatus() {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getCheckInStatusChart())
                                .build();
        }

        @Operation(summary = "[Organizer] Tần suất Check-in theo giờ (Column Chart)",
                description = "Trả về số lượng khách check-in phân theo khung giờ trong ngày. Hỗ trợ lọc `from`/`to`.")
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
}
