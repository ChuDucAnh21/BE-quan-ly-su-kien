package com.example.backend.service.impl;

import com.example.backend.dto.response.dashboard.*;
import com.example.backend.entity.User;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.enums.Role;
import com.example.backend.enums.TicketStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.*;
import com.example.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;

    // -------------------------------------------------------
    // HELPER: Lấy User hiện tại từ SecurityContext
    // -------------------------------------------------------
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    }

    // -------------------------------------------------------
    // HELPER: Nếu FE không truyền from/to, mặc định 30 ngày gần nhất
    // -------------------------------------------------------
    private LocalDate resolveFrom(LocalDate from) {
        return from != null ? from : LocalDate.now().minusDays(30);
    }

    private LocalDate resolveTo(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }

    // ===========================================================
    // API TỔNG QUAN (ĐÃ CÓ)
    // ===========================================================

    @Override
    public AdminDashboardResponse getAdminDashboard() {
        long totalUsers = userRepository.countByRole(Role.USER);
        long totalOrganizers = userRepository.countByRole(Role.ORGANIZER);
        long totalEvents = eventRepository.count();
        long totalTickets = ticketRepository.count();
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalOrganizers(totalOrganizers)
                .totalEvents(totalEvents)
                .totalTickets(totalTickets)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    public OrganizerDashboardResponse getOrganizerDashboard() {
        User organizer = getCurrentUser();
        long totalEvents = eventRepository.countByOrganizer(organizer);
        long totalTicketsSold = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.ACTIVE)
                + ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.USED);
        long totalCheckedIn = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.USED);
        BigDecimal totalRevenue = paymentRepository.sumAmountByOrganizerAndStatus(organizer.getId(), PaymentStatus.SUCCESS);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        return OrganizerDashboardResponse.builder()
                .totalEvents(totalEvents)
                .totalTicketsSold(totalTicketsSold)
                .totalCheckedIn(totalCheckedIn)
                .totalRevenue(totalRevenue)
                .build();
    }

    // ===========================================================
    // API BIỂU ĐỒ ADMIN
    // ===========================================================

    /**
     * Biểu đồ Tròn: Cơ cấu tài khoản theo Role.
     * Không cần lọc thời gian vì đây là số liệu tổng (All-time).
     */
    @Override
    public List<ChartPointDTO> getRoleDistribution() {
        // Đếm từng Role và tạo danh sách ChartPointDTO trả về
        return Arrays.stream(Role.values())
                .map(role -> ChartPointDTO.builder()
                        .label(role.name())
                        .count(userRepository.countByRole(role))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Đường: Tăng trưởng người dùng theo Ngày.
     * Lấy dữ liệu từng ngày và phân loại theo Role.
     * Kết quả: [{ date: "2024-05-01", count: 10, type: "USER" }, ...]
     */
    @Override
    public List<UserGrowthPointDTO> getUserGrowthChart(LocalDate from, LocalDate to) {
        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);

        List<Object[]> rows = userRepository.countByDateAndRole(startDT, endDT);
        // Mỗi row = [date (java.sql.Date), role (Role enum), count (Long)]
        return rows.stream()
                .map(row -> UserGrowthPointDTO.builder()
                        .date(row[0].toString())        // Ngày dạng "yyyy-MM-dd"
                        .count(((Number) row[2]).longValue())
                        .type(row[1].toString())         // Tên Role
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Cột: Top 5 Organizer có nhiều sự kiện nhất.
     * Dùng Pageable để giới hạn kết quả trả về chỉ 5 Organizer đứng đầu.
     */
    @Override
    public List<ChartPointDTO> getTop5OrganizersChart() {
        List<Object[]> rows = eventRepository.findTop5Organizers(PageRequest.of(0, 5));
        // Mỗi row = [organizerName (String), eventCount (Long)]
        return rows.stream()
                .map(row -> ChartPointDTO.builder()
                        .label((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Cột: Số sự kiện mới tạo theo từng Tháng.
     */
    @Override
    public List<ChartPointDTO> getMonthlyEventsChart(LocalDate from, LocalDate to) {
        LocalDateTime startDT = resolveFrom(from).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);

        List<Object[]> rows = eventRepository.countGroupByMonth(startDT, endDT);
        // Mỗi row = [month (String "yyyy-MM"), count (Long)]
        return rows.stream()
                .map(row -> ChartPointDTO.builder()
                        .label((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Tròn: Tỉ lệ các trạng thái sự kiện (All-time).
     */
    @Override
    public List<ChartPointDTO> getEventStatusChart() {
        List<Object[]> rows = eventRepository.countGroupByStatus();
        // Mỗi row = [status (EventStatus enum), count (Long)]
        return rows.stream()
                .map(row -> ChartPointDTO.builder()
                        .label(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Cột ngang: Sự kiện phân theo Danh mục (All-time).
     */
    @Override
    public List<ChartPointDTO> getEventCategoryChart() {
        List<Object[]> rows = eventRepository.countGroupByCategory();
        // Mỗi row = [category (String), count (Long)]
        return rows.stream()
                .map(row -> ChartPointDTO.builder()
                        .label((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Cột: Doanh thu theo Ngày (lọc theo from/to).
     */
    @Override
    public List<RevenuePointDTO> getRevenueChart(LocalDate from, LocalDate to) {
        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);

        List<Object[]> rows = paymentRepository.sumRevenueGroupByDay(startDT, endDT);
        // Mỗi row = [date (String "yyyy-MM-dd"), revenue (BigDecimal)]
        return rows.stream()
                .map(row -> RevenuePointDTO.builder()
                        .date((String) row[0])
                        .revenue((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Cột Chồng: Số vé Free/Trả phí theo Tháng (lọc theo from/to).
     */
    @Override
    public List<TicketTypePointDTO> getTicketTypeChart(LocalDate from, LocalDate to) {
        LocalDateTime startDT = resolveFrom(from).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);

        List<Object[]> rows = paymentRepository.countTicketTypeGroupByMonth(startDT, endDT);
        // Mỗi row = [month (String "yyyy-MM"), type (String), count (Long)]
        return rows.stream()
                .map(row -> TicketTypePointDTO.builder()
                        .month((String) row[0])
                        .type((String) row[1])
                        .count(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ===========================================================
    // API BIỂU ĐỒ ORGANIZER
    // ===========================================================

    /**
     * Biểu đồ Tròn: Tỉ lệ lấp đầy (Đã bán vs Còn trống).
     * Tính từ tổng capacity của tất cả sự kiện của Organizer.
     */
    @Override
    public List<ChartPointDTO> getOccupancyChart() {
        User organizer = getCurrentUser();
        // Đếm vé đã bán (ACTIVE + USED)
        long sold = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.ACTIVE)
                + ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.USED);

        // Lấy tổng capacity từ tất cả sự kiện của Organizer
        long totalCapacity = eventRepository.findAll().stream()
                .filter(e -> e.getOrganizer().getId().equals(organizer.getId()))
                .mapToLong(e -> e.getCapacity() != null ? e.getCapacity() : 0L)
                .sum();

        long remaining = Math.max(0, totalCapacity - sold);

        return List.of(
                ChartPointDTO.builder().label("Đã bán").count(sold).build(),
                ChartPointDTO.builder().label("Còn trống").count(remaining).build()
        );
    }

    /**
     * Biểu đồ Đường: Biến động bán vé theo Ngày của Organizer.
     */
    @Override
    public List<TicketSalesPointDTO> getTicketSalesTrendChart(LocalDate from, LocalDate to) {
        User organizer = getCurrentUser();
        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);

        List<Object[]> rows = ticketRepository.countSoldByDayForOrganizer(organizer.getId(), startDT, endDT);
        // Mỗi row = [date (String "yyyy-MM-dd"), value (Long)]
        return rows.stream()
                .map(row -> TicketSalesPointDTO.builder()
                        .date((String) row[0])
                        .value(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Biểu đồ Tròn: Cơ cấu khách (Trả phí vs Miễn phí) của Organizer.
     * Dựa vào ticketPrice của Event: > 0 là trả phí, = 0 là miễn phí.
     */
    @Override
    public List<ChartPointDTO> getAttendeeStructureChart() {
        User organizer = getCurrentUser();
        // Đếm số vé của các sự kiện Có giá (ticketPrice > 0)
        long paidCount = eventRepository.findAll().stream()
                .filter(e -> e.getOrganizer().getId().equals(organizer.getId()))
                .filter(e -> e.getTicketPrice() != null && e.getTicketPrice().compareTo(BigDecimal.ZERO) > 0)
                .mapToLong(e -> ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.ACTIVE)
                        + ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.USED))
                .sum();

        long freeCount = eventRepository.findAll().stream()
                .filter(e -> e.getOrganizer().getId().equals(organizer.getId()))
                .filter(e -> e.getTicketPrice() == null || e.getTicketPrice().compareTo(BigDecimal.ZERO) == 0)
                .mapToLong(e -> (long) (e.getSoldTickets() != null ? e.getSoldTickets() : 0))
                .sum();

        return List.of(
                ChartPointDTO.builder().label("Khách mua vé").count(paidCount).build(),
                ChartPointDTO.builder().label("Khách mời/Free").count(freeCount).build()
        );
    }

    /**
     * Biểu đồ Tròn: Trạng thái check-in (Đã vào cổng vs Chưa đến).
     */
    @Override
    public List<ChartPointDTO> getCheckInStatusChart() {
        User organizer = getCurrentUser();
        long used = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.USED);
        long active = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.ACTIVE);

        return List.of(
                ChartPointDTO.builder().label("Đã vào cổng").count(used).build(),
                ChartPointDTO.builder().label("Chưa đến").count(active).build()
        );
    }

    /**
     * Biểu đồ Cột: Tần suất check-in theo Khung giờ.
     */
    @Override
    public List<CheckInFrequencyDTO> getCheckInFrequencyChart(LocalDate from, LocalDate to) {
        User organizer = getCurrentUser();
        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);

        List<Object[]> rows = ticketRepository.countCheckedInGroupByHour(organizer.getId(), startDT, endDT);
        // Mỗi row = [hour (Integer), count (Long)]
        return rows.stream()
                .map(row -> CheckInFrequencyDTO.builder()
                        .hour(((Number) row[0]).intValue())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
}
