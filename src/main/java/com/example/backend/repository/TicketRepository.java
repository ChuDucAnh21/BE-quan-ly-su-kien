package com.example.backend.repository;

import com.example.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // API Tích hợp PESSIMISTIC_WRITE: Cấm kị việc 2 nhân viên quét cùng 1 mã QR
    // liên tiếp tại 1 thời điểm
    // Nghĩa là khi 1 nhân viên quét mã QR, hệ thống sẽ khóa mã QR đó lại
    // để nhân viên khác không thể quét mã QR đó
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.qrCode = :qrCode")
    Optional<Ticket> findByQrCodeWithLock(@Param("qrCode") String qrCode);

    // Tìm kiếm bằng mã băm với cơ chế khóa bi quan (Cho luồng Batch Sync và quét QR Offline)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.hashedQrCode = :hashedQrCode")
    Optional<Ticket> findByHashedQrCodeWithLock(@Param("hashedQrCode") String hashedQrCode);

    // Tìm vé theo ID đăng ký
    Optional<Ticket> findByRegistrationId(Long registrationId);

    // Tìm danh sách vé của toàn bộ sự kiện này phục vụ Admin/Organizer
    Page<Ticket> findByRegistration_Event_Id(Long eventId, Pageable pageable);

    // Tìm danh sách vé mà khách hàng sở hữu
    // SQL: SELECT * FROM tickets t
    // JOIN registrations r ON t.registration_id = r.id
    // JOIN users u ON r.user_id = u.id
    // WHERE u.email = :email
    Page<Ticket> findByRegistration_User_Email(String email, Pageable pageable);

    long count();

    long countByStatus(com.example.backend.enums.TicketStatus status);

    @Query("SELECT count(t) FROM Ticket t WHERE t.status = :status AND t.registration.event.organizer.id = :organizerId")
    long countByOrganizerAndStatus(@Param("organizerId") Long organizerId, @Param("status") com.example.backend.enums.TicketStatus status);

    // ===========================================================
    // CÁC QUERY THỐNG KÊ CHO DASHBOARD BIỂU ĐỒ
    // ===========================================================

    /**
     * Đếm số vé đã bán (ACTIVE + USED) theo từng Ngày (Line Chart - Organizer).
     * Dùng cho biểu đồ Biến động bán vé.
     * Kết quả: [{ date: "2024-05-01", value: 12 }, ...]
     * @param organizerId ID của Organizer cần lọc
     * @param startDate   Ngày bắt đầu
     * @param endDate     Ngày kết thúc
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', t.issuedAt, '%Y-%m-%d') as date, COUNT(t) as value " +
            "FROM Ticket t " +
            "WHERE t.registration.event.organizer.id = :organizerId " +
            "AND t.issuedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', t.issuedAt, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', t.issuedAt, '%Y-%m-%d')")
    List<Object[]> countSoldByDayForOrganizer(
            @Param("organizerId") Long organizerId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Đếm tần suất check-in theo Khung giờ trong ngày (Column Chart - Organizer).
     * Dùng cho biểu đồ Tần suất check-in (hỗ trợ điều phối nhân sự).
     * Kết quả: [{ hour: 18, count: 120 }, { hour: 19, count: 450 }, ...]
     * @param organizerId ID của Organizer cần lọc
     * @param startDate   Ngày bắt đầu
     * @param endDate     Ngày kết thúc
     */
    @Query("SELECT FUNCTION('HOUR', t.checkedInAt) as hour, COUNT(t) as count " +
            "FROM Ticket t " +
            "WHERE t.status = 'USED' " +
            "AND t.registration.event.organizer.id = :organizerId " +
            "AND t.checkedInAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('HOUR', t.checkedInAt) " +
            "ORDER BY FUNCTION('HOUR', t.checkedInAt)")
    List<Object[]> countCheckedInGroupByHour(
            @Param("organizerId") Long organizerId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
