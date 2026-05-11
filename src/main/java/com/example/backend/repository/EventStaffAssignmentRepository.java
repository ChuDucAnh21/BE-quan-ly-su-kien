package com.example.backend.repository;

import com.example.backend.entity.EventStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
