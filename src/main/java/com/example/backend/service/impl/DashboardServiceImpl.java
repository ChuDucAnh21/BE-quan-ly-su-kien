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

        private LocalDateTime toStartOfDay(LocalDate date) {
                return date != null ? date.atStartOfDay() : null;
        }

        private LocalDateTime toEndOfDay(LocalDate date) {
                return date != null ? date.atTime(LocalTime.MAX) : null;
        }

        @Override
        public AdminDashboardResponse getAdminDashboard(LocalDate from, LocalDate to) {
                long totalUsers, totalOrganizers, totalStaff, totalAdmin, totalEvents, totalTickets;
                BigDecimal totalRevenueSuccess, totalRevenueFail;

                if (from == null && to == null) {
                        totalUsers = userRepository.countByRole(Role.USER);
                        totalOrganizers = userRepository.countByRole(Role.ORGANIZER);
                        totalStaff = userRepository.countByRole(Role.STAFF);
                        totalAdmin = userRepository.countByRole(Role.ADMIN);
                        totalEvents = eventRepository.count();
                        totalTickets = ticketRepository.count();
                        totalRevenueSuccess = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
                        totalRevenueFail = paymentRepository.sumAmountByStatus(PaymentStatus.FAILED);
                } else {
                        LocalDateTime start = toStartOfDay(from != null ? from : LocalDate.of(2000, 1, 1));
                        LocalDateTime end = toEndOfDay(to != null ? to : LocalDate.now());

                        totalUsers = userRepository.countByRoleInRange(Role.USER, start, end);
                        totalOrganizers = userRepository.countByRoleInRange(Role.ORGANIZER, start, end);
                        totalStaff = userRepository.countByRoleInRange(Role.STAFF, start, end);
                        totalAdmin = userRepository.countByRoleInRange(Role.ADMIN, start, end);
                        totalEvents = eventRepository.countByCreatedAtBetween(start, end);
                        totalTickets = ticketRepository.countByIssuedAtBetween(start, end);
                        totalRevenueSuccess = paymentRepository
                                        .sumAmountByStatusAndCreatedAtBetween(PaymentStatus.SUCCESS, start, end);
                        totalRevenueFail = paymentRepository.sumAmountByStatusAndCreatedAtBetween(PaymentStatus.FAILED,
                                        start, end);
                }

                if (totalRevenueSuccess == null)
                        totalRevenueSuccess = BigDecimal.ZERO;
                if (totalRevenueFail == null)
                        totalRevenueFail = BigDecimal.ZERO;

                return AdminDashboardResponse.builder()
                                .totalUsers(totalUsers)
                                .totalOrganizers(totalOrganizers)
                                .totalStaff(totalStaff)
                                .totalAdmin(totalAdmin)
                                .totalEvents(totalEvents)
                                .totalTickets(totalTickets)
                                .totalRevenueSuccess(totalRevenueSuccess)
                                .totalRevenueFail(totalRevenueFail)
                                .build();
        }

        @Override
        public OrganizerDashboardResponse getOrganizerDashboard(LocalDate from, LocalDate to) {
                User organizer = getCurrentUser();
                long totalEvents, totalTicketsSold, totalCheckedIn, totalCapacity;
                BigDecimal totalRevenue;

                if (from == null && to == null) {
                        totalEvents = eventRepository.countByOrganizer(organizer);
                        totalTicketsSold = ticketRepository.countByOrganizerAndStatus(organizer.getId(),
                                        TicketStatus.ACTIVE)
                                        + ticketRepository.countByOrganizerAndStatus(organizer.getId(),
                                                        TicketStatus.USED);
                        totalCheckedIn = ticketRepository.countByOrganizerAndStatus(organizer.getId(),
                                        TicketStatus.USED);
                        totalRevenue = paymentRepository.sumAmountByOrganizerAndStatus(organizer.getId(),
                                        PaymentStatus.SUCCESS);
                        totalCapacity = eventRepository.sumCapacityByOrganizerId(organizer.getId());
                } else {
                        LocalDateTime start = toStartOfDay(from != null ? from : LocalDate.of(2000, 1, 1));
                        LocalDateTime end = toEndOfDay(to != null ? to : LocalDate.now());

                        totalEvents = eventRepository.countByOrganizerAndCreatedAtBetween(organizer, start, end);
                        totalTicketsSold = ticketRepository.countByOrganizerAndStatusInRange(organizer.getId(),
                                        TicketStatus.ACTIVE, start, end)
                                        + ticketRepository.countByOrganizerAndStatusInRange(organizer.getId(),
                                                        TicketStatus.USED, start, end);
                        totalCheckedIn = ticketRepository.countByOrganizerAndStatusInRange(organizer.getId(),
                                        TicketStatus.USED, start, end);
                        totalRevenue = paymentRepository.sumAmountByOrganizerAndStatusAndCreatedAtBetween(
                                        organizer.getId(), PaymentStatus.SUCCESS, start, end);
                        totalCapacity = eventRepository.sumCapacityByOrganizerIdInRange(organizer.getId(), start, end);
                }

                if (totalRevenue == null)
                        totalRevenue = BigDecimal.ZERO;

                return OrganizerDashboardResponse.builder()
                                .totalEvents(totalEvents)
                                .totalTicketsSold(totalTicketsSold)
                                .totalCapacity(totalCapacity)
                                .totalCheckedIn(totalCheckedIn)
                                .totalRevenue(totalRevenue)
                                .build();
        }

        // ===========================================================
        // API BIỂU ĐỒ ADMIN
        // ===========================================================

        /**
         * Biểu đồ Tròn: Cơ cấu tài khoản theo Role.
         */
        @Override
        public List<ChartPointDTO> getRoleDistribution(LocalDate from, LocalDate to) {
                if (from == null && to == null) {
                        // All-time: đếm từng Role không lọc thời gian
                        return Arrays.stream(Role.values())
                                        .map(role -> ChartPointDTO.builder()
                                                        .label(role.name())
                                                        .count(userRepository.countByRole(role))
                                                        .build())
                                        .collect(Collectors.toList());
                }
                LocalDateTime startDT = resolveFrom(from).atStartOfDay();
                LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);
                return Arrays.stream(Role.values())
                                .map(role -> ChartPointDTO.builder()
                                                .label(role.name())
                                                .count(userRepository.countByRoleInRange(role, startDT, endDT))
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
                                                .date(row[0].toString()) // Ngày dạng "yyyy-MM-dd"
                                                .count(((Number) row[2]).longValue())
                                                .type(row[1].toString()) // Tên Role
                                                .build())
                                .collect(Collectors.toList());
        }

        /**
         * Biểu đồ Cột: Top 5 Organizer có nhiều sự kiện nhất.
         * Dùng Pageable để giới hạn kết quả trả về chỉ 5 Organizer đứng đầu.
         */
        @Override
        public List<ChartPointDTO> getTop5OrganizersChart(LocalDate from, LocalDate to) {
                if (from == null && to == null) {
                        List<Object[]> rows = eventRepository.findTop5Organizers(PageRequest.of(0, 5));
                        return rows.stream()
                                        .map(row -> ChartPointDTO.builder()
                                                        .label((String) row[0])
                                                        .count(((Number) row[1]).longValue())
                                                        .build())
                                        .collect(Collectors.toList());
                }
                LocalDateTime startDT = resolveFrom(from).atStartOfDay();
                LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);
                List<Object[]> rows = eventRepository.findTop5OrganizersInRange(PageRequest.of(0, 5), startDT, endDT);
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
        public List<ChartPointDTO> getEventStatusChart(LocalDate from, LocalDate to) {
                List<Object[]> rows;
                if (from == null && to == null) {
                        rows = eventRepository.countGroupByStatus();
                } else {
                        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
                        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);
                        rows = eventRepository.countGroupByStatusInRange(startDT, endDT);
                }
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
        public List<ChartPointDTO> getEventCategoryChart(LocalDate from, LocalDate to) {
                List<Object[]> rows;
                if (from == null && to == null) {
                        rows = eventRepository.countGroupByCategory();
                } else {
                        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
                        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);
                        rows = eventRepository.countGroupByCategoryInRange(startDT, endDT);
                }
                // config lại label để hiển thị tiếng việt
                // { label: 'Âm nhạc và Giải trí', value: 'MUSIC' },
                // { label: 'Hội nghị', value: 'CONFERENCE' },
                // { label: 'Workshop', value: 'WORKSHOP' },
                // { label: 'Triển lãm', value: 'EXHIBITION' },
                // { label: 'Thể thao', value: 'SPORT' },
                // { label: 'Công nghệ', value: 'TECH' },
                // { label: 'Doanh nghiệp', value: 'BUSINESSES' },
                // { label: 'Nghệ thuật', value: 'ART' },
                // { label: 'Lễ hội', value: 'FESTIVAL' },
                // { label: 'Cộng đồng', value: 'COMMUNITY' },
                // { label: 'Trực tuyến', value: 'ONLINE' },
                // { label: 'Khác', value: 'OTHER' },

                rows.stream().forEach(row -> {
                        String label = row[0].toString();
                        if (label.equals("MUSIC")) {
                                row[0] = "Âm nhạc và Giải trí";
                        } else if (label.equals("SPORT")) {
                                row[0] = "Thể thao";
                        } else if (label.equals("ART_THEATER")) {
                                row[0] = "Nghệ thuật";
                        } else if (label.equals("CONFERENCE")) {
                                row[0] = "Hội nghị";
                        } else if (label.equals("TECH")) {
                                row[0] = "Công nghệ";
                        } else if (label.equals("FESTIVAL")) {
                                row[0] = "Lễ hội";
                        } else if (label.equals("WORKSHOP")) {
                                row[0] = "Workshop";
                        } else if (label.equals("EXHIBITION")) {
                                row[0] = "Triển lãm";
                        } else if (label.equals("ONLINE")) {
                                row[0] = "Trực tuyến";
                        } else if (label.equals("BUSINESS")) {
                                row[0] = "Doanh nghiệp";
                        } else if (label.equals("COMMUNITY")) {
                                row[0] = "Cộng đồng";
                        } else if (label.equals("OTHER")) {
                                row[0] = "Khác";
                        } else {
                                row[0] = label;
                        }
                });
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
        // BIỂU ĐỒ ADMIN: TOP 10 SỰ KIỆN THEO DOANH THU & SỐ VÉ
        // ===========================================================

        @Override
        public TopEventsChartResponse getTopEventsChart(LocalDate from, LocalDate to) {
                // 1. Xác định khoảng thời gian lọc (Mặc định All-time cho Top 10 nếu không
                // truyền)
                LocalDateTime start = toStartOfDay(from != null ? from : LocalDate.of(2000, 1, 1));
                LocalDateTime end = toEndOfDay(to != null ? to : LocalDate.now());

                // 2. Query DB giới hạn top 10
                org.springframework.data.domain.Pageable top10 = PageRequest.of(0, 10);

                org.springframework.data.domain.Page<Object[]> revenueData = paymentRepository
                                .findTopEventsByRevenue(start, end, top10);
                org.springframework.data.domain.Page<Object[]> ticketsData = ticketRepository
                                .findTopEventsByTicketsSold(start, end, top10);

                // 3. Map kết quả sang ChartPointDTO
                List<ChartPointDTO> topRevenueEvents = revenueData.getContent().stream()
                                .map(row -> new ChartPointDTO((String) row[0],
                                                ((java.math.BigDecimal) row[1]).longValue()))
                                .collect(Collectors.toList());

                List<ChartPointDTO> topSoldEvents = ticketsData.getContent().stream()
                                .map(row -> new ChartPointDTO((String) row[0], ((Number) row[1]).longValue()))
                                .collect(Collectors.toList());

                return TopEventsChartResponse.builder()
                                .topRevenueEvents(topRevenueEvents)
                                .topSoldEvents(topSoldEvents)
                                .build();
        }

        // ===========================================================
        // API BIỂU ĐỒ ORGANIZER
        // ===========================================================

        /**
         * Biểu đồ Tròn: Tỉ lệ lấp đầy (Đã bán vs Còn trống).
         * Tính từ tổng capacity của tất cả sự kiện của Organizer.
         */
        @Override
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public List<ChartPointDTO> getOccupancyChart(LocalDate from, LocalDate to) {
                User organizer = getCurrentUser();
                long sold, totalCapacity;
                if (from == null && to == null) {
                        sold = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.ACTIVE)
                                        + ticketRepository.countByOrganizerAndStatus(organizer.getId(),
                                                        TicketStatus.USED);
                        totalCapacity = eventRepository.sumCapacityByOrganizerId(organizer.getId());
                } else {
                        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
                        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);
                        sold = ticketRepository.countByOrganizerAndStatusInRange(organizer.getId(), TicketStatus.ACTIVE,
                                        startDT, endDT)
                                        + ticketRepository.countByOrganizerAndStatusInRange(organizer.getId(),
                                                        TicketStatus.USED, startDT, endDT);
                        totalCapacity = eventRepository.sumCapacityByOrganizerIdInRange(organizer.getId(), startDT,
                                        endDT);
                }
                long remaining = Math.max(0, totalCapacity - sold);
                return List.of(
                                ChartPointDTO.builder().label("Đã bán").count(sold).build(),
                                ChartPointDTO.builder().label("Còn trống").count(remaining).build());
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
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public List<ChartPointDTO> getAttendeeStructureChart(LocalDate from, LocalDate to) {
                User organizer = getCurrentUser();
                long paidCount, freeCount;
                if (from == null && to == null) {
                        paidCount = ticketRepository.countPaidTicketsByOrganizerId(organizer.getId());
                        freeCount = ticketRepository.countFreeTicketsByOrganizerId(organizer.getId());
                } else {
                        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
                        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);
                        paidCount = ticketRepository.countPaidTicketsByOrganizerIdInRange(organizer.getId(), startDT,
                                        endDT);
                        freeCount = ticketRepository.countFreeTicketsByOrganizerIdInRange(organizer.getId(), startDT,
                                        endDT);
                }
                return List.of(
                                ChartPointDTO.builder().label("Khách mua vé").count(paidCount).build(),
                                ChartPointDTO.builder().label("Khách mời/Free").count(freeCount).build());
        }

        /**
         * Biểu đồ Tròn: Trạng thái check-in (Đã vào cổng vs Chưa đến).
         */
        @Override
        public List<ChartPointDTO> getCheckInStatusChart(LocalDate from, LocalDate to) {
                User organizer = getCurrentUser();
                long used, active;
                if (from == null && to == null) {
                        used = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.USED);
                        active = ticketRepository.countByOrganizerAndStatus(organizer.getId(), TicketStatus.ACTIVE);
                } else {
                        LocalDateTime startDT = resolveFrom(from).atStartOfDay();
                        LocalDateTime endDT = resolveTo(to).atTime(LocalTime.MAX);
                        used = ticketRepository.countByOrganizerAndStatusInRange(organizer.getId(), TicketStatus.USED,
                                        startDT, endDT);
                        active = ticketRepository.countByOrganizerAndStatusInRange(organizer.getId(),
                                        TicketStatus.ACTIVE, startDT, endDT);
                }
                return List.of(
                                ChartPointDTO.builder().label("Đã vào cổng").count(used).build(),
                                ChartPointDTO.builder().label("Chưa đến").count(active).build());
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

        // -------------------------------------------------------
        // THỐNG KÊ THEO SỰ KIỆN CỤ THỂ
        // -------------------------------------------------------

        @Override
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public EventStatisticsResponse getEventStatistics(Long eventId, LocalDate from, LocalDate to) {
                // 1. Lấy thông tin sự kiện
                com.example.backend.entity.Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                // 2. Kiểm tra quyền: ADMIN xem được tất cả, ORGANIZER chỉ xem của chính mình
                User currentUser = getCurrentUser();
                boolean isAdmin = currentUser.getRole() == Role.ADMIN;
                boolean isOwner = event.getOrganizer().getId().equals(currentUser.getId());
                if (!isAdmin && !isOwner) {
                        throw new AppException(ErrorCode.ACCESS_DENIED);
                }

                // 3. Xác định khoảng thời gian lọc cho biểu đồ
                // Mặc định: from = ngày mở đăng ký (hoặc ngày tạo event nếu null)
                // to = cuối ngày hôm nay
                LocalDateTime defaultFrom = event.getRegistrationStartTime() != null
                                ? event.getRegistrationStartTime()
                                : (event.getCreatedAt() != null ? event.getCreatedAt() : event.getStartTime());
                LocalDateTime fromDT = (from != null)
                                ? from.atStartOfDay()
                                : (defaultFrom != null ? defaultFrom : LocalDateTime.now().minusMonths(3));
                LocalDateTime toDT = (to != null)
                                ? to.atTime(LocalTime.MAX)
                                : LocalDateTime.now().with(LocalTime.MAX);

                // 4. Thống kê vé
                long activeCount, usedCount;
                if (from == null && to == null) {
                        activeCount = ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.ACTIVE);
                        usedCount = ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.USED);
                } else {
                        activeCount = ticketRepository.countByEventIdAndStatusInRange(eventId, TicketStatus.ACTIVE,
                                        fromDT, toDT);
                        usedCount = ticketRepository.countByEventIdAndStatusInRange(eventId, TicketStatus.USED, fromDT,
                                        toDT);
                }

                long totalSold = activeCount + usedCount;
                int capacity = event.getCapacity() != null ? event.getCapacity() : 0;
                long remaining = Math.max(0, capacity - totalSold);
                double occupancy = capacity > 0 ? Math.round((totalSold * 100.0 / capacity) * 10.0) / 10.0 : 0;
                double checkInRate = totalSold > 0 ? Math.round((usedCount * 100.0 / totalSold) * 10.0) / 10.0 : 0;

                // 5. Thống kê thanh toán
                BigDecimal revenue;
                long successPayments, pendingPayments, cancelledPayments;

                if (from == null && to == null) {
                        revenue = paymentRepository.sumRevenueByEventId(eventId);
                        successPayments = paymentRepository.countByEventIdAndStatus(eventId, PaymentStatus.SUCCESS);
                        pendingPayments = paymentRepository.countByEventIdAndStatus(eventId, PaymentStatus.PENDING);
                        cancelledPayments = paymentRepository.countByEventIdAndStatus(eventId, PaymentStatus.CANCELLED);
                } else {
                        revenue = paymentRepository.sumRevenueByEventIdInRange(eventId, fromDT, toDT);
                        successPayments = paymentRepository.countByEventIdAndStatusInRange(eventId,
                                        PaymentStatus.SUCCESS, fromDT, toDT);
                        pendingPayments = paymentRepository.countByEventIdAndStatusInRange(eventId,
                                        PaymentStatus.PENDING, fromDT, toDT);
                        cancelledPayments = paymentRepository.countByEventIdAndStatusInRange(eventId,
                                        PaymentStatus.CANCELLED, fromDT, toDT);
                }

                // 6. Biểu đồ bán vé theo ngày (lọc theo from/to)
                List<Object[]> salesRows = ticketRepository.countSoldByDayForEvent(eventId, fromDT, toDT);
                List<ChartPointDTO> ticketSalesByDay = salesRows.stream()
                                .map(row -> ChartPointDTO.builder()
                                                .label((String) row[0])
                                                .count(((Number) row[1]).longValue())
                                                .build())
                                .collect(Collectors.toList());

                // 7. Biểu đồ check-in theo giờ (lọc theo from/to)
                List<Object[]> hourRows = ticketRepository.countCheckInByHourForEvent(eventId, fromDT, toDT);
                List<ChartPointDTO> checkInByHour = hourRows.stream()
                                .map(row -> ChartPointDTO.builder()
                                                .label(String.format("%02d:00", ((Number) row[0]).intValue()))
                                                .count(((Number) row[1]).longValue())
                                                .build())
                                .collect(Collectors.toList());

                return EventStatisticsResponse.builder()
                                .eventId(event.getId())
                                .eventTitle(event.getTitle())
                                .status(event.getStatus() != null ? event.getStatus().name() : null)
                                .capacity(capacity)
                                .totalTicketsSold(totalSold)
                                .ticketsRemaining(remaining)
                                .occupancyRate(occupancy)
                                .checkedInCount(usedCount)
                                .notCheckedInCount(activeCount)
                                .checkInRate(checkInRate)
                                .ticketPrice(event.getTicketPrice())
                                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                                .successPaymentCount(successPayments)
                                .pendingPaymentCount(pendingPayments)
                                .cancelledPaymentCount(cancelledPayments)
                                .ticketSalesByDay(ticketSalesByDay)
                                .checkInByHour(checkInByHour)
                                .build();
        }
}
