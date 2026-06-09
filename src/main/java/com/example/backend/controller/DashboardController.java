package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.dashboard.*;
import com.example.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

        private final DashboardService dashboardService;

        // ============================================================
        // API TỔNG QUAN (STATISTIC CARDS)
        // ============================================================

        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<AdminDashboardResponse> getAdminDashboard(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<AdminDashboardResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê Admin thành công").build())
                                .data(dashboardService.getAdminDashboard(from, to))
                                .build();
        }

        @GetMapping("/organizer")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<OrganizerDashboardResponse> getOrganizerDashboard(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<OrganizerDashboardResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê Organizer thành công")
                                                .build())
                                .data(dashboardService.getOrganizerDashboard(from, to))
                                .build();
        }

        // ============================================================
        // BIỂU ĐỒ ADMIN
        // ============================================================

        @GetMapping("/admin/role-distribution")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<List<ChartPointDTO>> getRoleDistribution(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<List<ChartPointDTO>>builder()
                                .status(ApiResponse.Status.builder().message("OK").build())
                                .data(dashboardService.getRoleDistribution(from, to))
                                .build();
        }

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

        @GetMapping("/events/{eventId}/statistics")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
        public ApiResponse<EventStatisticsResponse> getEventStatistics(
                        @PathVariable Long eventId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
                return ApiResponse.<EventStatisticsResponse>builder()
                                .status(ApiResponse.Status.builder().message("Lấy thống kê sự kiện thành công").build())
                                .data(dashboardService.getEventStatistics(eventId, from, to))
                                .build();
        }
}
