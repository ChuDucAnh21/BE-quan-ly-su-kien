package com.example.backend.repository;

import com.example.backend.entity.Payment;
import com.example.backend.enums.PaymentStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
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
        List<Payment> findByRegistration_User_EmailAndStatusOrderByCreatedAtDesc(String email, PaymentStatus status);

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
                        @org.springframework.data.repository.query.Param("status") PaymentStatus status);

        @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.registration.event.organizer.id = :organizerId")
        java.math.BigDecimal sumAmountByOrganizerAndStatus(
                        @org.springframework.data.repository.query.Param("organizerId") Long organizerId,
                        @org.springframework.data.repository.query.Param("status") PaymentStatus status);

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
                        @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate,
                        @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate);

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
                        @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate,
                        @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate);
}
