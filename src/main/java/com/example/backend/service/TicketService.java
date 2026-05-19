package com.example.backend.service;

import com.example.backend.dto.request.BatchCheckInRequest;
import com.example.backend.dto.request.CheckInRequest;
import com.example.backend.dto.response.BatchCheckInResponse;
import com.example.backend.dto.response.CheckInLogResponse;
import com.example.backend.dto.response.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
    // API Lấy danh sách toàn bộ Ticket của ID Sự Kiện
    Page<TicketResponse> getTicketsCustomerByEventId(Long eventId, String searchKeyword, Pageable pageable);

    // API Quét vé Check-in Online bằng Scanner
    TicketResponse processCheckIn(CheckInRequest request);

    // API Đồng bộ nhiều vé cùng lúc (Dành cho chức năng Offline Sync)
    BatchCheckInResponse processBatchCheckIn(BatchCheckInRequest request);

    // Hàm gọi nội bộ cho từng vé trong Batch (Yêu cầu Transaction độc lập)
    TicketResponse processSingleBatchCheckIn(String hashedQrCode, Long eventId, String staffEmail);

    // API Lấy danh sách vé của chính khách hàng đang đăng nhập
    Page<TicketResponse> getMyTickets(Pageable pageable);

    // API Lấy lịch sử Check-in của Sự kiện có hỗ trợ bộ lọc
    Page<CheckInLogResponse> getCheckInHistoryByEventId(Long eventId, String search, com.example.backend.enums.CheckInMethod method, java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate, Pageable pageable);
}
