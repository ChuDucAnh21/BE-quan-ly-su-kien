package com.example.backend.service;

import com.example.backend.dto.request.BatchCheckInRequest;
import com.example.backend.dto.request.CheckInRequest;
import com.example.backend.dto.response.BatchCheckInResponse;
import com.example.backend.dto.response.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
    // API Lấy danh sách toàn bộ Ticket của ID Sự Kiện
    Page<TicketResponse> getTicketsByEventId(Long eventId, Pageable pageable);

    // API Quét vé Check-in Online bằng Scanner
    TicketResponse processCheckIn(CheckInRequest request);

    // API Đồng bộ nhiều vé cùng lúc (Dành cho chức năng Offline Sync)
    BatchCheckInResponse processBatchCheckIn(BatchCheckInRequest request);

    // Hàm gọi nội bộ cho từng vé trong Batch (Yêu cầu Transaction độc lập)
    TicketResponse processSingleBatchCheckIn(String hashedQrCode, String staffEmail);

    // API Lấy danh sách vé của chính khách hàng đang đăng nhập
    Page<TicketResponse> getMyTickets(Pageable pageable);
}
