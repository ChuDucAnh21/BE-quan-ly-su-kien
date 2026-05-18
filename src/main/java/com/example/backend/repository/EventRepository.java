package com.example.backend.repository;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

        // Tìm sự kiện và khóa dòng dữ liệu (Pessimistic Lock) để chống bán lố vé khi nhiều luồng truy cập cùng lúc
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT e FROM Event e WHERE e.id = :id")
        Optional<Event> findByIdWithLock(@Param("id") Long id);

        // Lấy sự kiện theo ID sự kiện và bắt buộc phải thuộc về đúng Organizer đó
        Optional<Event> findByIdAndOrganizer(Long id, User organizer);

        // Tìm kiếm sự kiện đa luồng cho User, Organizer, Admin
        // - keyword: tìm theo tên hoặc địa điểm
        // - category: lọc theo danh mục
        // - status: lọc theo trạng thái
        // - organizerId: Nếu truy xuất danh sách của riêng 1 Organizer nào đó
        @Query("SELECT e FROM Event e WHERE " +
                        "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
                        +
                        "AND (:category IS NULL OR e.category = :category) " +
                        "AND (:status IS NULL OR e.status = :status) " +
                        "AND (:organizerId IS NULL OR e.organizer.id = :organizerId) " +
                        "AND (CAST(:startTime AS timestamp) IS NULL OR e.startTime >= :startTime) " +
                        "AND (CAST(:endTime AS timestamp) IS NULL OR e.endTime <= :endTime) " +
                        "AND (:minPrice IS NULL OR e.ticketPrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR e.ticketPrice <= :maxPrice) " +
                        "AND (:isFree IS NULL OR (:isFree = true AND e.ticketPrice = 0) OR (:isFree = false AND e.ticketPrice > 0))")
        Page<Event> searchEvents(@Param("keyword") String keyword,
                        @Param("category") String category,
                        @Param("status") EventStatus status,
                        @Param("organizerId") Long organizerId,
                        @Param("startTime") java.time.LocalDateTime startTime,
                        @Param("endTime") java.time.LocalDateTime endTime,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                        @Param("isFree") Boolean isFree,
                        Pageable pageable);

        long countByOrganizer(User organizer);

        // ===========================================================
        // CÁC QUERY THỐNG KÊ CHO DASHBOARD BIỂU ĐỒ
        // ===========================================================

        /**
         * Đếm số lượng sự kiện theo từng Trạng thái (Pie Chart).
         * Kết quả: [{ status: "PUBLISHED", count: 30 }, ...]
         */
        @Query("SELECT e.status as status, COUNT(e) as count FROM Event e GROUP BY e.status")
        List<Object[]> countGroupByStatus();

        /**
         * Đếm số lượng sự kiện theo từng Danh mục (Bar Chart ngang).
         * Kết quả: [{ category: "Âm nhạc", count: 45 }, ...]
         */
        @Query("SELECT e.category as category, COUNT(e) as count FROM Event e GROUP BY e.category ORDER BY COUNT(e) DESC")
        List<Object[]> countGroupByCategory();

        /**
         * Đếm số lượng sự kiện mới tạo theo từng Tháng (Column Chart).
         * Kết quả: [{ month: "2024-01", count: 12 }, ...]
         * @param startDate Ngày bắt đầu lọc
         * @param endDate   Ngày kết thúc lọc
         */
        @Query("SELECT FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m') as month, COUNT(e) as count " +
                "FROM Event e " +
                "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m') " +
                "ORDER BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m')")
        List<Object[]> countGroupByMonth(@Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        /**
         * Tìm Top 5 Organizer có nhiều sự kiện nhất (Bar Chart).
         * Kết quả: [{ organizerName: "ABC", eventCount: 25 }, ...]
         */
        @Query("SELECT e.organizer.fullName as organizerName, COUNT(e) as eventCount " +
                "FROM Event e " +
                "GROUP BY e.organizer.id, e.organizer.fullName " +
                "ORDER BY COUNT(e) DESC")
        List<Object[]> findTop5Organizers(org.springframework.data.domain.Pageable pageable);
}
