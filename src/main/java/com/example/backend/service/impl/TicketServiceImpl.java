package com.example.backend.service.impl;

import com.example.backend.dto.request.BatchCheckInRequest;
import com.example.backend.dto.request.CheckInRequest;
import com.example.backend.dto.response.BatchCheckInResponse;
import com.example.backend.dto.response.TicketResponse;
import com.example.backend.dto.response.BatchCheckInResponse.BatchCheckInError;
import com.example.backend.entity.CheckInLog;
import com.example.backend.entity.Event;
import com.example.backend.entity.Ticket;
import com.example.backend.entity.User;
import com.example.backend.enums.CheckInMethod;
import com.example.backend.enums.TicketStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.TicketRepository;
import com.example.backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

        private final TicketRepository ticketRepository;
        private final EventRepository eventRepository;
        private final com.example.backend.repository.EventStaffAssignmentRepository staffAssignmentRepository;
        private final com.example.backend.repository.UserRepository userRepository;
        private final com.example.backend.repository.CheckInLogRepository checkInLogRepository;

        // Tự tiêm chính mình (Proxy) để có thể gọi hàm @Transactional nội bộ mà không
        // mất tính độc lập (REQUIRES_NEW)
        @org.springframework.context.annotation.Lazy
        @org.springframework.beans.factory.annotation.Autowired
        private TicketService self;

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponse> getTicketsByEventId(Long eventId, Pageable pageable) {
                // 1. Kiểm tra quyền sở hữu sự kiện
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                // Kiểm tra quyền: Admin hoặc Organizer của Event hoặc Staff được phân công
                boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                boolean isOrganizer = event.getOrganizer().getEmail().equals(email);
                boolean isAssignedStaff = staffAssignmentRepository.existsByEventIdAndStaff_Email(eventId, email);

                if (!isAdmin && !isOrganizer && !isAssignedStaff) {
                        throw new AppException(ErrorCode.ACCESS_DENIED);
                }

                // 2. Fetch data và Mapping
                Page<Ticket> tickets = ticketRepository.findByRegistration_Event_Id(eventId, pageable);
                return tickets.map(t -> {
                        User buyer = t.getRegistration().getUser();
                        return TicketResponse.builder()
                                        .id(t.getId())
                                        .qrCode(null) // Ẩn mã gốc để chống nội gián đánh cắp vé
                                        .hashedQrCode(t.getHashedQrCode()) // Chỉ trả mã băm cho App Staff đồng bộ
                                        .eventId(eventId)
                                        .userId(buyer.getId())
                                        .userEmail(buyer.getEmail())
                                        .userFullName(buyer.getFullName())
                                        .status(t.getStatus())
                                        .checkedInAt(t.getCheckedInAt() != null ? t.getCheckedInAt() : null)
                                        .issuedAt(t.getIssuedAt())
                                        .build();
                });
        }

        @Override
        @Transactional // PESSIMISTIC_WRITE phải đi kèm @Transactional để duy trì khóa trong suốt quá
                       // trình xử lý
        public TicketResponse processCheckIn(CheckInRequest request) {
                log.info("Bắt đầu xử lý tín hiệu Check-in với QR Code: {}", request.getQrCode());

                // 1. Dùng Database Pessimistic Lock khóa truy vấn Ticket
                // Kỹ thuật này block mọi lệnh SELECT từ Thread thứ 2 cho đến khi Thread 1 thực
                // thi cập nhật thành công (Tránh Bug trùng vé).
                Ticket ticket = ticketRepository.findByQrCodeWithLock(request.getQrCode())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                // 2. Ràng buộc Tình Trạng Vé
                if (ticket.getStatus() == TicketStatus.USED) {
                        log.warn("Vé này ĐÃ ĐƯỢC CHECK-IN TỪ TRƯỚC! Cảnh báo Lừa Đảo Quét Nhiều Thiết Bị (Duplicate Code): {}",
                                        request.getQrCode());
                        throw new AppException(ErrorCode.TICKET_ALREADY_USED);
                }
                if (ticket.getStatus() == TicketStatus.INVALID) {
                        log.warn("Hành khách quét vé bị hủy (Do khách đổi ý hủy đơn miễn phí): {}",
                                        request.getQrCode());
                        throw new AppException(ErrorCode.TICKET_INVALID);
                }

                // 3. Kỹ thuật kiểm tra quyền của người soát vé.
                // Người checkin phải là Admin, Organizer hoặc nằm trong danh sách Staff của
                // Event.
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                Long eventId = ticket.getRegistration().getEvent().getId();

                boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                boolean isOrganizer = ticket.getRegistration().getEvent().getOrganizer().getEmail().equals(email);
                boolean isAssignedStaff = staffAssignmentRepository.existsByEventIdAndStaff_Email(eventId, email);

                if (!isAdmin && !isOrganizer && !isAssignedStaff) {
                        log.warn("CẢNH BÁO BẢO MẬT: Tài khoản {} cố gắng check-in cho sự kiện {} mà không có quyền!",
                                        email,
                                        eventId);
                        throw new AppException(ErrorCode.ACCESS_DENIED);
                }

                // 4. Đánh Dấu Vé Đã Sử Dụng
                ticket.setStatus(TicketStatus.USED);
                ticket.setCheckedInAt(LocalDateTime.now());
                ticketRepository.save(ticket);
                log.info("Check-in hợp lệ! Cổng mở! Vé đã chuyển sang trạng thái USED!");

                // 5. Ghi Log vào bảng checkin_logs
                User staffUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                CheckInLog checkInLog = CheckInLog.builder()
                                .ticket(ticket)
                                .checkedInBy(staffUser)
                                .method(CheckInMethod.QR) // Quét QR trực tiếp Online
                                .build();
                checkInLogRepository.save(checkInLog);

                User buyer = ticket.getRegistration().getUser();
                return TicketResponse.builder()
                                .id(ticket.getId())
                                .qrCode(null) // Không trả lại mã gốc cho Staff
                                .hashedQrCode(ticket.getHashedQrCode())
                                .eventId(ticket.getRegistration().getEvent().getId())
                                .eventTitle(ticket.getRegistration().getEvent().getTitle())
                                .ticketPrice(ticket.getRegistration().getEvent().getTicketPrice())
                                .userId(buyer.getId())
                                .userEmail(buyer.getEmail())
                                .userFullName(buyer.getFullName())
                                .status(ticket.getStatus())
                                .issuedAt(ticket.getIssuedAt())
                                .checkedInAt(ticket.getCheckedInAt())
                                .build();
        }

        // Xử lý Check-in hàng loạt (Offline Mode) khi không có mạng
        @Override
        public BatchCheckInResponse processBatchCheckIn(BatchCheckInRequest request) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                log.info("Bắt đầu xử lý Batch Sync Offline ({} mã vé) từ Staff: {}", request.getHashedQrCodes().size(),
                                email);

                int successfulCount = 0;
                int failedCount = 0;
                List<TicketResponse> successfulTickets = new ArrayList<>();
                List<BatchCheckInError> errors = new ArrayList<>();

                // Gọi proxy (self) để đảm bảo Transaction độc lập
                for (String hashedQrCode : request.getHashedQrCodes()) {
                        try {
                                TicketResponse res = self.processSingleBatchCheckIn(hashedQrCode, email);
                                successfulTickets.add(res);
                                successfulCount++;
                        } catch (Exception e) {
                                failedCount++;
                                String errorMsg = e instanceof AppException
                                                ? ((AppException) e).getErrorCode().getMessage()
                                                : e.getMessage();
                                errors.add(new BatchCheckInError(hashedQrCode, errorMsg));
                                log.warn("Batch Sync lỗi mã {}: {}", hashedQrCode, errorMsg);
                        }
                }

                return BatchCheckInResponse.builder()
                                .total(request.getHashedQrCodes().size())
                                .successfulCount(successfulCount)
                                .failedCount(failedCount)
                                .successfulTickets(successfulTickets)
                                .errors(errors)
                                .build();
        }

        // hàm này dùng để check in từng vé một khi không có mạng
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public TicketResponse processSingleBatchCheckIn(String hashedQrCode, String staffEmail) {
                // 1. Tìm vé theo mã băm với Khóa bi quan
                Ticket ticket = ticketRepository.findByHashedQrCodeWithLock(hashedQrCode)
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                // 2. Ràng buộc Tình Trạng Vé
                if (ticket.getStatus() == TicketStatus.USED) {
                        throw new AppException(ErrorCode.TICKET_ALREADY_USED);
                }
                if (ticket.getStatus() == TicketStatus.INVALID) {
                        throw new AppException(ErrorCode.TICKET_INVALID);
                }

                // 3. Phân quyền
                Long eventId = ticket.getRegistration().getEvent().getId();
                boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                boolean isOrganizer = ticket.getRegistration().getEvent().getOrganizer().getEmail().equals(staffEmail);
                boolean isAssignedStaff = staffAssignmentRepository.existsByEventIdAndStaff_Email(eventId, staffEmail);

                if (!isAdmin && !isOrganizer && !isAssignedStaff) {
                        throw new AppException(ErrorCode.ACCESS_DENIED);
                }

                // 4. Cập nhật trạng thái
                ticket.setStatus(TicketStatus.USED);
                ticket.setCheckedInAt(LocalDateTime.now());
                ticketRepository.save(ticket);

                // 5. Ghi Log
                User staffUser = userRepository.findByEmail(staffEmail)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                CheckInLog checkInLog = CheckInLog.builder()
                                .ticket(ticket)
                                .checkedInBy(staffUser)
                                .method(CheckInMethod.MANUAL) // Đánh dấu là Check-in bằng tay / Offline Sync
                                .build();
                checkInLogRepository.save(checkInLog);

                User buyer = ticket.getRegistration().getUser();
                return TicketResponse.builder()
                                .id(ticket.getId())
                                .qrCode(null)
                                .hashedQrCode(ticket.getHashedQrCode())
                                .eventId(eventId)
                                .eventTitle(ticket.getRegistration().getEvent().getTitle())
                                .ticketPrice(ticket.getRegistration().getEvent().getTicketPrice())
                                .userId(buyer.getId())
                                .userEmail(buyer.getEmail())
                                .userFullName(buyer.getFullName())
                                .status(ticket.getStatus())
                                .issuedAt(ticket.getIssuedAt())
                                .checkedInAt(ticket.getCheckedInAt())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponse> getMyTickets(Pageable pageable) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                log.info("Khách hàng đang truy vấn danh sách vé cá nhân: {}", email);

                Page<Ticket> tickets = ticketRepository.findByRegistration_User_Email(email, pageable);

                return tickets.map(t -> TicketResponse.builder()
                                .id(t.getId())
                                .qrCode(t.getQrCode()) // Trả mã gốc cho khách hàng
                                .hashedQrCode(null) // Không cần trả mã băm
                                .eventId(t.getRegistration().getEvent().getId())
                                .eventTitle(t.getRegistration().getEvent().getTitle())
                                .ticketPrice(t.getRegistration().getEvent().getTicketPrice())
                                .userId(t.getRegistration().getUser().getId())
                                .registrationId(t.getRegistration().getId())
                                .userEmail(t.getRegistration().getUser().getEmail())
                                .userFullName(t.getRegistration().getUser().getFullName())
                                .status(t.getStatus())
                                .issuedAt(t.getIssuedAt())
                                .checkedInAt(t.getCheckedInAt() != null ? t.getCheckedInAt() : null)
                                .build());
        }
}
