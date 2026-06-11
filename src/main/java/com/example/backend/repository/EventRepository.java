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

       // Tìm sự kiện và khóa dòng dữ liệu (Pessimistic Lock) để chống bán lố vé khi
       // nhiều luồng truy cập cùng lúc
       @Lock(LockModeType.PESSIMISTIC_WRITE)
       @Query("SELECT e FROM Event e WHERE e.id = :id")
       Optional<Event> findByIdWithLock(@Param("id") Long id); 

       // Lấy sự kiện theo ID sự kiện và bắt buộc phải thuộc về đúng Organizer đó
       Optional<Event> findByIdAndOrganizer(Long id, User organizer);

       // Tìm kiếm sự kiện đa luồng cho User, Organizer, Admin
       @Query("SELECT e FROM Event e WHERE " +
                     "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
                     +
                     "AND (:hasCategories = false OR e.category IN :categories) " +
                     "AND (:status IS NULL OR e.status = :status) " +
                     "AND (:organizerId IS NULL OR e.organizer.id = :organizerId) " +
                     "AND (CAST(:startTime AS timestamp) IS NULL OR e.startTime >= :startTime) " +
                     "AND (CAST(:endTime AS timestamp) IS NULL OR e.endTime <= :endTime) " +
                     "AND (:minPrice IS NULL OR e.ticketPrice >= :minPrice) " +
                     "AND (:maxPrice IS NULL OR e.ticketPrice <= :maxPrice) " +
                     "AND (:isFree IS NULL OR (:isFree = true AND e.ticketPrice = 0) OR (:isFree = false AND e.ticketPrice > 0))")
       Page<Event> searchEvents(@Param("keyword") String keyword,
                     @Param("hasCategories") boolean hasCategories,
                     @Param("categories") java.util.List<String> categories,
                     @Param("status") EventStatus status,
                     @Param("organizerId") Long organizerId,
                     @Param("startTime") java.time.LocalDateTime startTime,
                     @Param("endTime") java.time.LocalDateTime endTime,
                     @Param("minPrice") java.math.BigDecimal minPrice,
                     @Param("maxPrice") java.math.BigDecimal maxPrice,
                     @Param("isFree") Boolean isFree,
                     Pageable pageable);

       long countByOrganizer(User organizer);

       @Query("SELECT COUNT(e) FROM Event e WHERE e.createdAt BETWEEN :startDate AND :endDate")
       long countByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                     @Param("endDate") java.time.LocalDateTime endDate);

       @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer = :organizer AND e.createdAt BETWEEN :startDate AND :endDate")
       long countByOrganizerAndCreatedAtBetween(@Param("organizer") User organizer,
                     @Param("startDate") java.time.LocalDateTime startDate,
                     @Param("endDate") java.time.LocalDateTime endDate);

       // ===========================================================
       // CÁC QUERY THỐNG KÊ CHO DASHBOARD BIỂU ĐỒ
       // ===========================================================

       @Query("SELECT e.status as status, COUNT(e) as count FROM Event e GROUP BY e.status")
       List<Object[]> countGroupByStatus();

       /** Phân loại theo trạng thái, có lọc khoảng thời gian */
       @Query("SELECT e.status as status, COUNT(e) as count FROM Event e " +
                     "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
                     "GROUP BY e.status")
       List<Object[]> countGroupByStatusInRange(@Param("startDate") java.time.LocalDateTime startDate,
                     @Param("endDate") java.time.LocalDateTime endDate);

       /**
        * Đếm số lượng sự kiện theo từng Danh mục (Bar Chart ngang).
        * Kết quả: [{ category: "Âm nhạc", count: 45 }, ...]
        */
       @Query("SELECT e.category as category, COUNT(e) as count FROM Event e GROUP BY e.category ORDER BY COUNT(e) DESC")
       List<Object[]> countGroupByCategory();

       /** Phân loại theo danh mục, có lọc khoảng thời gian */
       @Query("SELECT e.category as category, COUNT(e) as count FROM Event e " +
                     "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
                     "GROUP BY e.category ORDER BY COUNT(e) DESC")
       List<Object[]> countGroupByCategoryInRange(@Param("startDate") java.time.LocalDateTime startDate,
                     @Param("endDate") java.time.LocalDateTime endDate);

       /**
        * Đếm số lượng sự kiện mới tạo theo từng Tháng (Column Chart).
        * Kết quả: [{ month: "2024-01", count: 12 }, ...]
        * 
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

       /**
        * Tính tổng Capacity của tất cả sự kiện thuộc về 1 Organizer.
        * Dùng cho biểu đồ Tỉ lệ lấp đầy. Thay thế findAll() để tránh OOM.
        */
       @Query("SELECT COALESCE(SUM(e.capacity), 0) FROM Event e WHERE e.organizer.id = :organizerId")
       Long sumCapacityByOrganizerId(@Param("organizerId") Long organizerId);

       /** Tổng capacity có lọc khoảng thời gian (theo ngày tạo event) */
       @Query("SELECT COALESCE(SUM(e.capacity), 0) FROM Event e " +
                     "WHERE e.organizer.id = :organizerId " +
                     "AND e.createdAt BETWEEN :startDate AND :endDate")
       Long sumCapacityByOrganizerIdInRange(@Param("organizerId") Long organizerId,
                     @Param("startDate") java.time.LocalDateTime startDate,
                     @Param("endDate") java.time.LocalDateTime endDate);

       /** Top 5 Organizer, có lọc khoảng thời gian */
       @Query("SELECT e.organizer.fullName as organizerName, COUNT(e) as eventCount " +
                     "FROM Event e " +
                     "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
                     "GROUP BY e.organizer.id, e.organizer.fullName " +
                     "ORDER BY COUNT(e) DESC")
       List<Object[]> findTop5OrganizersInRange(org.springframework.data.domain.Pageable pageable,
                     @Param("startDate") java.time.LocalDateTime startDate,
                     @Param("endDate") java.time.LocalDateTime endDate);

       /**
        * Lấy 5 sự kiện sắp diễn ra, nối chuỗi Tên + Mô tả ngắn + Thời gian.
        * Dùng cho AI / Bot hoặc hiển thị nhanh.
        */
       @Query("SELECT CONCAT('- ', e.title, ': ', e.shortDescription, ' (Thời gian: ', e.startTime, ')') " +
                     "FROM Event e " +
                     "WHERE e.status = 'PUBLISHED' AND e.startTime > CURRENT_TIMESTAMP " +
                     "ORDER BY e.startTime ASC LIMIT 5")
       List<String> findUpcomingEventsStr();

       /**
        * Function Calling: Tìm kiếm top 3 sự kiện theo từ khóa và trạng thái.
        */
       List<Event> findTop3ByTitleContainingIgnoreCaseAndStatus(String title, EventStatus status);

       List<Event> findTop5ByStatusAndStartTimeAfterOrderByStartTimeAsc(EventStatus status, java.time.LocalDateTime startTime);

       // Các query bổ sung cho Chatbot
       List<Event> findTop5ByCategoryContainingIgnoreCaseAndStatus(String category, EventStatus status);

       List<Event> findTop5ByLocationContainingIgnoreCaseAndStatus(String location, EventStatus status);

       List<Event> findTop5ByTicketPriceAndStatus(java.math.BigDecimal price, EventStatus status);

       List<Event> findTop5ByStatusAndStartTimeBetweenOrderByStartTimeAsc(EventStatus status, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

       @Query("SELECT DISTINCT e.category FROM Event e WHERE e.status = :status AND e.category IS NOT NULL")
       List<String> findDistinctCategoryByStatus(@Param("status") EventStatus status);


}
