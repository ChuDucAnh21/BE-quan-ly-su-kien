package com.example.backend.repository;

import com.example.backend.entity.Registration;
import com.example.backend.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Đếm số lượng vé đã nằm trong trạng thái chỉ định (Ví dụ: CONFIRMED hoặc
    // CHECKED_IN)
    long countByEventIdAndStatusIn(Long eventId, List<RegistrationStatus> statuses);

    // Kiểm tra xem User đã đăng ký Event này chưa
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    // Tìm kiếm đăng ký theo EventId và UserId
    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);

    // Tìm tất cả các đăng ký của một sự kiện
    List<Registration> findByEventId(Long eventId);

    // Đếm số lượng vé đã bán theo nhóm eventId (Giải quyết N+1 Query trong getAllEvents)
    // Kết quả: [[eventId, count], [eventId, count], ...]
    @Query("SELECT r.event.id, COUNT(r) FROM Registration r " +
           "WHERE r.event.id IN :eventIds " +
           "AND r.status IN :statuses " +
           "GROUP BY r.event.id")
    List<Object[]> countTicketsSoldGroupByEventId(@Param("eventIds") List<Long> eventIds,
                                                   @Param("statuses") List<RegistrationStatus> statuses);
}
