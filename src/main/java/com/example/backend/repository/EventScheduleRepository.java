package com.example.backend.repository;

import com.example.backend.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

    // Lấy danh sách lịch trình con của 1 Sự kiện, đã được sắp xếp theo thời gian
    // bắt đầu
    List<EventSchedule> findByEventIdOrderByStartTimeAsc(Long eventId);

    // Lấy chi tiết 1 lịch trình con đảm bảo nó thuộc quyền của đúng Event đó
    Optional<EventSchedule> findByIdAndEventId(Long id, Long eventId);

    // Dùng để Xóa toàn bộ lịch trình khi có tác động Đồng bộ (Tối ưu Bulk Delete bằng SQL)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM EventSchedule e WHERE e.event.id = :eventId")
    void deleteAllByEventId(@org.springframework.data.repository.query.Param("eventId") Long eventId);
}
