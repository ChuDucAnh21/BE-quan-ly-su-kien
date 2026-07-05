package com.example.backend.service.impl;

import com.example.backend.dto.response.MyPaymentTransactionResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.PaymentTransactionResponse;
import com.example.backend.dto.response.RegistrationResponse;
import com.example.backend.entity.Event;
import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentConfig;
import com.example.backend.entity.Registration;
import com.example.backend.entity.Ticket;
import com.example.backend.entity.User;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.enums.RegistrationStatus;
import com.example.backend.enums.Role;
import com.example.backend.enums.TicketStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.PaymentConfigRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.RegistrationRepository;
import com.example.backend.repository.TicketRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PaymentService;
import com.example.backend.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

        private final PaymentRepository paymentRepository;
        private final EventRepository eventRepository;
        private final UserRepository userRepository;
        private final PaymentConfigRepository paymentConfigRepository;
        private final RegistrationRepository registrationRepository;
        private final TicketRepository ticketRepository;

        @Override
        @Transactional(readOnly = true)
        // xem danh sách giao dịch của sự kiện, dùng cho admin và organizer
        public PageResponse<PaymentTransactionResponse> getEventPayments(Long eventId, String keyword,
                        PaymentStatus status, LocalDate from, LocalDate to, int page, int size) {

                // 1. Kiểm tra sự kiện tồn tại
                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                // 2. Lấy thông tin user hiện tại và kiểm tra quyền
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                User currentUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

                boolean isAdmin = currentUser.getRole() == Role.ADMIN;
                boolean isOwner = event.getOrganizer().getId().equals(currentUser.getId());

                // Chỉ ADMIN và ORGANIZER sở hữu sự kiện mới được xem
                if (!isAdmin && !isOwner) {
                        log.warn("Truy cập trái phép vào danh sách thanh toán của sự kiện {} bởi user {}", eventId,
                                        currentUser.getId());
                        throw new AppException(ErrorCode.ACCESS_DENIED);
                }

                // 3. Phân trang và sắp xếp: Mới nhất lên đầu
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                // 4. Xử lý thời gian
                LocalDateTime startDate = from != null ? from.atStartOfDay() : null;  // lấy mốc bắt đầu của ngày
                LocalDateTime endDate = to != null ? to.atTime(LocalTime.MAX) : null; // lấy mốc kết thúc của ngày

                // 5. Lấy dữ liệu
                Page<Payment> paymentPage = paymentRepository.findPaymentsByEventIdWithFilters(eventId, keyword, status,
                                startDate, endDate, pageable);

                // 5. Map sang DTO
                Page<PaymentTransactionResponse> mappedPage = paymentPage
                                .map(payment -> PaymentTransactionResponse.builder()
                                                .paymentId(payment.getId())
                                                .orderCode(payment.getOrderCode())
                                                .amount(payment.getAmount())
                                                .status(payment.getStatus())
                                                .paidAt(payment.getPaidAt())
                                                .createdAt(payment.getCreatedAt())
                                                .registrationCode(payment.getRegistration() != null
                                                                ? payment.getRegistration().getRegistrationCode()
                                                                : null)
                                                .userId(payment.getRegistration() != null
                                                                && payment.getRegistration().getUser() != null
                                                                                ? payment.getRegistration().getUser()
                                                                                                .getId()
                                                                                : null)
                                                .userEmail(payment.getRegistration() != null
                                                                && payment.getRegistration().getUser() != null
                                                                                ? payment.getRegistration().getUser()
                                                                                                .getEmail()
                                                                                : null)
                                                .userFullName(payment.getRegistration() != null
                                                                && payment.getRegistration().getUser() != null
                                                                                ? payment.getRegistration().getUser()
                                                                                                .getFullName()
                                                                                : null)
                                                .build());

                // 6. Trả về cấu trúc PageResponse
                return PageResponse.<PaymentTransactionResponse>builder()
                                .currentPage(mappedPage.getNumber())
                                .totalPages(mappedPage.getTotalPages())
                                .pageSize(mappedPage.getSize())
                                .totalElements(mappedPage.getTotalElements())
                                .data(mappedPage.getContent())
                                .build();
        }

        @Override
        @Transactional
        // đồng bộ trạng thái vé chờ thanh toán
        public String syncPaymentStatus(String orderCode) {
                Payment payment = paymentRepository.findByOrderCode(orderCode).orElse(null);
                if (payment == null) {
                        throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                }

                if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        return "ALREADY_PAID";
                } else if (payment.getStatus() != PaymentStatus.PENDING) {
                        return payment.getStatus().name();
                }

                Registration registration = payment.getRegistration();
                Event event = registration.getEvent();
                PaymentConfig config = paymentConfigRepository.findByUserId(event.getOrganizer().getId()).orElse(null);

                if (config == null || !config.getIsActive()) {
                        throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                }

                PayOS payOS = new PayOS(config.getClientId(), config.getApiKey(), config.getChecksumKey());

                try {
                        PaymentLink paymentLinkData = null;
                        try {
                                paymentLinkData = payOS.paymentRequests().get(orderCode);
                        } catch (Exception ex) {
                                log.error("Không kết nối được tới PayOS hoặc đơn không tồn tại", ex);
                                throw new RuntimeException(
                                                "Không tìm thấy đơn hàng trên cổng PayOS: " + ex.getMessage());
                        }

                        if (paymentLinkData != null && "PAID".equals(paymentLinkData.getStatus().name())) {
                                payment.setStatus(PaymentStatus.SUCCESS);
                                payment.setPaidAt(LocalDateTime.now());
                                paymentRepository.save(payment);

                                registration.setStatus(RegistrationStatus.CONFIRMED);
                                registrationRepository.save(registration);

                                String rawQrCode = UUID.randomUUID().toString();
                                String hashedQrCode = HashUtil.sha256(rawQrCode);
                                Ticket ticket = Ticket.builder()
                                                .registration(registration)
                                                .qrCode(rawQrCode)
                                                .hashedQrCode(hashedQrCode)
                                                .issuedAt(LocalDateTime.now())
                                                .status(TicketStatus.ACTIVE)
                                                .build();
                                ticketRepository.save(ticket);

                                return "PAID_AND_TICKET_CREATED";
                        } else if (paymentLinkData != null && ("CANCELLED".equals(paymentLinkData.getStatus().name())
                                        || "EXPIRED".equals(paymentLinkData.getStatus().name()))) {
                                payment.setStatus(PaymentStatus.FAILED);
                                paymentRepository.save(payment);

                                registration.setStatus(RegistrationStatus.CANCELLED);
                                registrationRepository.save(registration);

                                if (event.getSoldTickets() > 0) {
                                        event.setSoldTickets(event.getSoldTickets() - 1);
                                        eventRepository.save(event);
                                }

                                return paymentLinkData.getStatus().name();
                        } else {
                                return "PENDING";
                        }

                } catch (Exception e) {
                        log.error("Lỗi khi gọi API PayOS để đồng bộ đơn hàng {}", orderCode, e);
                        throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                }
        }

        @Override
        public PageResponse<RegistrationResponse> getMyPendingPayments(int page, int size, String sort, String search) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                Direction direction = "asc".equalsIgnoreCase(sort) ? Direction.ASC : Direction.DESC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

                Page<Payment> pendingPaymentsPage = paymentRepository.findPendingPaymentsWithSearch(email,
                                PaymentStatus.PENDING, search, pageable);

                Page<RegistrationResponse> dataPage = pendingPaymentsPage.map(payment -> {
                        Registration registration = payment.getRegistration();
                        return RegistrationResponse.builder()
                                        .registrationId(registration.getId())
                                        .eventId(registration.getEvent().getId())
                                        .eventName(registration.getEvent().getTitle())
                                        .eventPrice(registration.getEvent().getTicketPrice())
                                        .eventImage(registration.getEvent().getBannerUrl())
                                        .eventStartTime(registration.getEvent().getStartTime())
                                        .userId(registration.getUser().getId())
                                        .registrationCode(registration.getRegistrationCode())
                                        .status(registration.getStatus().name())
                                        .createdAt(registration.getCreatedAt())
                                        .checkoutUrl(payment.getCheckoutUrl())
                                        .orderCode(payment.getOrderCode())
                                        .amount(payment.getAmount())
                                        .build();
                });

                return PageResponse.<RegistrationResponse>builder()
                                .currentPage(dataPage.getNumber())
                                .totalPages(dataPage.getTotalPages())
                                .pageSize(dataPage.getSize())
                                .totalElements(dataPage.getTotalElements())
                                .data(dataPage.getContent())
                                .build();
        }

        @Override
        public PageResponse<MyPaymentTransactionResponse> getMyPaymentHistory(int page, int size, String sort,
                        String search, PaymentStatus status, LocalDate startDate, LocalDate endDate) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                Direction direction = "asc".equalsIgnoreCase(sort) ? Direction.ASC : Direction.DESC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

                LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
                LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

                Page<Payment> paymentHistoryPage = paymentRepository.findMyPaymentHistory(email, search, status,
                                startDateTime, endDateTime, pageable);

                Page<MyPaymentTransactionResponse> dataPage = paymentHistoryPage.map(payment -> {
                        Registration registration = payment.getRegistration();
                        return MyPaymentTransactionResponse.builder()
                                        .paymentId(payment.getId())
                                        .orderCode(payment.getOrderCode())
                                        .amount(payment.getAmount())
                                        .status(payment.getStatus())
                                        .paidAt(payment.getPaidAt())
                                        .createdAt(payment.getCreatedAt())
                                        .checkoutUrl(payment.getCheckoutUrl())
                                        .eventId(registration.getEvent().getId())
                                        .eventTitle(registration.getEvent().getTitle())
                                        .eventImage(registration.getEvent().getBannerUrl())
                                        .eventStartTime(registration.getEvent().getStartTime())
                                        .registrationCode(registration.getRegistrationCode())
                                        .build();
                });

                return PageResponse.<MyPaymentTransactionResponse>builder()
                                .currentPage(dataPage.getNumber())
                                .totalPages(dataPage.getTotalPages())
                                .pageSize(dataPage.getSize())
                                .totalElements(dataPage.getTotalElements())
                                .data(dataPage.getContent())
                                .build();
        }
}
