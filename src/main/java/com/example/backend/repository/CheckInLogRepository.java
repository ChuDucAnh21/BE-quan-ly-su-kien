package com.example.backend.repository;

import com.example.backend.entity.CheckInLog;
import com.example.backend.enums.CheckInMethod;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInLogRepository extends JpaRepository<CheckInLog, Long> {

       // Lấy lịch sử check-in của 1 sự kiện, sắp xếp mới nhất lên đầu (không lọc)
       Page<CheckInLog> findByTicket_Registration_Event_IdOrderByCreatedAtDesc(Long eventId, Pageable pageable);

       // Lấy lịch sử check-in với bộ lọc nâng cao
       @Query("SELECT c FROM CheckInLog c WHERE c.ticket.registration.event.id = :eventId " +
                     "AND (:search IS NULL OR (" +
                     "LOWER(c.ticket.registration.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(c.ticket.registration.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(c.ticket.registration.registrationCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(c.ticket.hashedQrCode) LIKE LOWER(CONCAT('%', :search, '%')))) " +
                     "AND (:method IS NULL OR c.method = :method) " +
                     "AND (cast(:fromDate as timestamp) IS NULL OR c.createdAt >= :fromDate) " +
                     "AND (cast(:toDate as timestamp) IS NULL OR c.createdAt <= :toDate) " +
                     "ORDER BY c.createdAt DESC")
       Page<CheckInLog> findCheckInHistoryWithFilters(
                     @Param("eventId") Long eventId,
                     @Param("search") String search,
                     @Param("method") CheckInMethod method,
                     @Param("fromDate") LocalDateTime fromDate,
                     @Param("toDate") LocalDateTime toDate,
                     Pageable pageable);
}
