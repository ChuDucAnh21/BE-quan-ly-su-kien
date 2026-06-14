package com.example.backend.repository;

import com.example.backend.entity.Payment;
import com.example.backend.enums.PaymentStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

        // Tìm các giao dịch đang chờ thanh toán nhưng đã quá hạn (Timeout dọn rác)
        List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime dateTime);

        // Lấy danh sách giao dịch đang chờ thanh toán của người dùng hiện tại (để có
        // thể mở lại trang thanh toán)
        @Query("SELECT p FROM Payment p WHERE p.registration.user.email = :email AND p.status = :status AND (:search IS NULL OR LOWER(p.registration.event.title) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Payment> findPendingPaymentsWithSearch(
                        @Param("email") String email,
                        @Param("status") PaymentStatus status,
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT p FROM Payment p " +
               "WHERE p.registration.user.email = :email " +
               "AND (:status IS NULL OR p.status = :status) " +
               "AND (CAST(:startDate AS timestamp) IS NULL OR p.createdAt >= :startDate) " +
               "AND (CAST(:endDate AS timestamp) IS NULL OR p.createdAt <= :endDate) " +
               "AND (:keyword IS NULL OR " +
               "LOWER(p.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
               "LOWER(p.registration.event.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
               "LOWER(p.registration.registrationCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Payment> findMyPaymentHistory(
                        @Param("email") String email,
                        @Param("keyword") String keyword,
                        @Param("status") PaymentStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // Kỹ thuật PESSIMISTIC_WRITE (Khóa bi quan): Khi một request (Webhook PayOS)
        // gọi hàm này,
        // dòng dữ liệu Payment này sẽ bị khóa chặt lại ở tầng Database.
        // Bất kỳ luồng (Request) thứ 2 nào cố gắng tìm và đọc Payment này để sinh vé sẽ
        // phải ĐỨNG ĐỢI
        // luồng thứ 1 xử lý xong và commit. Điều này chống tắc nghẽn và ngăn việc sinh
        // 2 vé cho 1 đơn hàng (Idempotency).

        // cái này để tránh tình trạng 2 request cùng sinh vé 1 lúc
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        Optional<Payment> findByOrderCode(String orderCode);

        Optional<Payment> findByRegistrationId(Long registrationId);

        @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
        java.math.BigDecimal sumAmountByStatus(
                        @Param("status") PaymentStatus status);

        @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.registration.event.organizer.id = :organizerId")
        java.math.BigDecimal sumAmountByOrganizerAndStatus(
                        @Param("organizerId") Long organizerId,
                        @Param("status") PaymentStatus status);

        @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
        java.math.BigDecimal sumAmountByStatusAndCreatedAtBetween(
                        @Param("status") PaymentStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.registration.event.organizer.id = :organizerId AND p.createdAt BETWEEN :startDate AND :endDate")
        java.math.BigDecimal sumAmountByOrganizerAndStatusAndCreatedAtBetween(
                        @Param("organizerId") Long organizerId,
                        @Param("status") PaymentStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // ===========================================================
        // CÁC QUERY THỐNG KÊ CHO DASHBOARD BIỂU ĐỒ
        // ===========================================================

        /**
         * Tính tổng doanh thu theo từng Ngày trong khoảng thời gian (Bar Chart).
         * Kết quả: [{ date: "2024-05-01", revenue: 5000000 }, ...]
         * 
         * @param startDate Ngày bắt đầu lọc
         * @param endDate   Ngày kết thúc lọc
         */
        @Query("SELECT FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d') as date, SUM(p.amount) as revenue " +
                        "FROM Payment p " +
                        "WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d') " +
                        "ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d')")
        List<Object[]> sumRevenueGroupByDay(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Đếm số lượng vé (Trả phí / Miễn phí) theo từng Tháng (Stacked Column Chart).
         * Dựa vào ticketPrice của Event: ticketPrice > 0 là trả phí, = 0 là miễn phí.
         * Kết quả: [{ month: "2024-05", type: "VÉ TRẢ PHÍ", count: 450 }, ...]
         * 
         * @param startDate Ngày bắt đầu
         * @param endDate   Ngày kết thúc
         */
        @Query("SELECT " +
                        "FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m') as month, " +
                        "CASE WHEN p.registration.event.ticketPrice > 0 THEN 'VÉ TRẢ PHÍ' ELSE 'VÉ MIỄN PHÍ' END as type, "
                        +
                        "COUNT(p) as count " +
                        "FROM Payment p " +
                        "WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m'), " +
                        "CASE WHEN p.registration.event.ticketPrice > 0 THEN 'VÉ TRẢ PHÍ' ELSE 'VÉ MIỄN PHÍ' END " +
                        "ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m')")
        List<Object[]> countTicketTypeGroupByMonth(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // ===========================================================
        // QUERY THỐNG KÊ THEO SỰ KIỆN CỤ THỂ
        // ===========================================================

        /** Tổng doanh thu SUCCESS của 1 sự kiện */
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
                        "WHERE p.registration.event.id = :eventId AND p.status = 'SUCCESS'")
        java.math.BigDecimal sumRevenueByEventId(
                        @Param("eventId") Long eventId);

        /** Tổng doanh thu SUCCESS của 1 sự kiện trong khoảng thời gian */
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
                        "WHERE p.registration.event.id = :eventId AND p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
        java.math.BigDecimal sumRevenueByEventIdInRange(
                        @Param("eventId") Long eventId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /** Đếm giao dịch theo trạng thái cho 1 sự kiện */
        @Query("SELECT COUNT(p) FROM Payment p " +
                        "WHERE p.registration.event.id = :eventId AND p.status = :status")
        long countByEventIdAndStatus(
                        @Param("eventId") Long eventId,
                        @Param("status") PaymentStatus status);

        /** Đếm giao dịch theo trạng thái cho 1 sự kiện trong khoảng thời gian */
        @Query("SELECT COUNT(p) FROM Payment p " +
                        "WHERE p.registration.event.id = :eventId AND p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
        long countByEventIdAndStatusInRange(
                        @Param("eventId") Long eventId,
                        @Param("status") PaymentStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Lấy danh sách giao dịch thanh toán của một sự kiện, có phân trang, tìm kiếm
         * và lọc theo khoảng thời gian
         */
        @Query("SELECT p FROM Payment p " +
                        "WHERE p.registration.event.id = :eventId " +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (CAST(:startDate AS timestamp) IS NULL OR p.createdAt >= :startDate) " +
                        "AND (CAST(:endDate AS timestamp) IS NULL OR p.createdAt <= :endDate) " +
                        "AND (:keyword IS NULL OR " +
                        "LOWER(p.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.registration.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.registration.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.registration.registrationCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        org.springframework.data.domain.Page<Payment> findPaymentsByEventIdWithFilters(
                        @Param("eventId") Long eventId,
                        @Param("keyword") String keyword,
                        @Param("status") PaymentStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /** Lấy top các sự kiện có doanh thu cao nhất trong khoảng thời gian */
        @Query("SELECT p.registration.event.title as label, SUM(p.amount) as value " +
                        "FROM Payment p " +
                        "WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY p.registration.event.id, p.registration.event.title " +
                        "ORDER BY value DESC")
        Page<Object[]> findTopEventsByRevenue(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);
}
