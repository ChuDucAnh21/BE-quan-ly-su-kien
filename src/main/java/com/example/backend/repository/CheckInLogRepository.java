package com.example.backend.repository;

import com.example.backend.entity.CheckInLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInLogRepository extends JpaRepository<CheckInLog, Long> {
    
    // Lấy lịch sử check-in của 1 sự kiện, sắp xếp mới nhất lên đầu
    Page<CheckInLog> findByTicket_Registration_Event_IdOrderByCreatedAtDesc(Long eventId, Pageable pageable);
}
