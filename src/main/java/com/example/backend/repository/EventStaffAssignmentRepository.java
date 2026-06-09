package com.example.backend.repository;

import com.example.backend.entity.Event;
import com.example.backend.entity.EventStaffAssignment;
import com.example.backend.enums.UserStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventStaffAssignmentRepository extends JpaRepository<EventStaffAssignment, Long> {

        // SQL: SELECT * FROM event_staff_assignments WHERE event_id = ?
        List<EventStaffAssignment> findByEventId(Long eventId);

        // SQL: SELECT * FROM event_staff_assignments WHERE event_id = ? AND staff_id =
        // ?
        Optional<EventStaffAssignment> findByEventIdAndStaffId(Long eventId, Long staffId);

        boolean existsByEventIdAndStaff_Email(Long eventId, String email);

        // Lấy danh sách sự kiện mà nhân viên được phân công dựa trên ID, có lọc theo từ
        // khóa (Dành cho Admin/Organizer/Staff xem của chính mình)
        @Query("SELECT e.event FROM EventStaffAssignment e WHERE e.staff.id = :staffId " +
                        "AND e.event.status != com.example.backend.enums.EventStatus.DRAFT " +
                        "AND (:search IS NULL OR LOWER(e.event.title) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Event> findEventsByStaffIdWithFilters(
                        @Param("staffId") Long staffId,
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT e FROM EventStaffAssignment e WHERE e.event.id = :eventId " +
                        "AND (:status IS NULL OR e.staff.status = :status) " +
                        "AND (:search IS NULL OR LOWER(e.staff.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.staff.email) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<EventStaffAssignment> findStaffsByEventIdWithFilters(
                        @Param("eventId") Long eventId,
                        @Param("search") String search,
                        @Param("status") UserStatus status,
                        Pageable pageable);
}
