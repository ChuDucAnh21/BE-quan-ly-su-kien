package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.entity.Event;
import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentConfig;
import com.example.backend.entity.Registration;
import com.example.backend.entity.Ticket;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.enums.RegistrationStatus;
import com.example.backend.enums.TicketStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.PaymentConfigRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.RegistrationRepository;
import com.example.backend.repository.TicketRepository;
import com.example.backend.dto.response.RegistrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentConfigRepository paymentConfigRepository;
    private final RegistrationRepository registrationRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    // API kiểm tra trạng thái thanh toán và sinh vé khi người dùng reload lại trang
    @GetMapping("/sync/{orderCode}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> syncPaymentStatus(@PathVariable String orderCode) {
        log.info("Chủ động đồng bộ trạng thái đơn hàng: {}", orderCode);

        Payment payment = paymentRepository.findByOrderCode(orderCode).orElse(null);
        if (payment == null) {
            throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status(ApiResponse.Status.builder().code(200)
                            .message("Đơn hàng đã được thanh toán thành công từ trước.").build())
                    .data("ALREADY_PAID")
                    .build());
        } else if (payment.getStatus() != PaymentStatus.PENDING) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status(ApiResponse.Status.builder().code(200)
                            .message("Đơn hàng đã được xử lý từ trước (Hủy/Thất bại).").build())
                    .data(payment.getStatus().name())
                    .build());
        }

        Registration registration = payment.getRegistration();
        Event event = registration.getEvent();
        PaymentConfig config = paymentConfigRepository.findByUserId(event.getOrganizer().getId()).orElse(null);

        if (config == null || !config.getIsActive()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        PayOS payOS = new PayOS(config.getClientId(), config.getApiKey(), config.getChecksumKey());

        try {
            // Lấy thông tin thanh toán từ SDK PayOS v2
            PaymentLink paymentLinkData = null;
            try {
                paymentLinkData = payOS.paymentRequests().get(orderCode);
            } catch (Exception ex) {
                log.error("Không kết nối được tới PayOS hoặc đơn không tồn tại", ex);
                throw new RuntimeException("Không tìm thấy đơn hàng trên cổng PayOS: " + ex.getMessage());
            }

            // case 1 : thanh toán thành công nhưng chưa gọi webhook do sự cố mạng hoặc lỗi
            // server
            if (paymentLinkData != null && "PAID".equals(paymentLinkData.getStatus().name())) {
                // Thanh toán thành toán -> Xé vé
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);

                registration.setStatus(RegistrationStatus.CONFIRMED);
                registrationRepository.save(registration);

                String rawQrCode = UUID.randomUUID().toString();
                String hashedQrCode = com.example.backend.util.HashUtil.sha256(rawQrCode);
                Ticket ticket = Ticket.builder()
                        .registration(registration)
                        .qrCode(rawQrCode)
                        .hashedQrCode(hashedQrCode)
                        .issuedAt(LocalDateTime.now())
                        .status(TicketStatus.ACTIVE)
                        .build();
                ticketRepository.save(ticket);

                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .status(ApiResponse.Status.builder().code(200)
                                .message("Kiểm tra thấy đơn hàng đã thanh toán. Đã sinh vé!").build())
                        .data("PAID_AND_TICKET_CREATED")
                        .build());
            }
            // case 2 : thanh toán thất bại do người dùng hủy hoặc hết hạn
            else if (paymentLinkData != null && ("CANCELLED".equals(paymentLinkData.getStatus().name())
                    || "EXPIRED".equals(paymentLinkData.getStatus().name()))) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                registration.setStatus(RegistrationStatus.CANCELLED);
                registrationRepository.save(registration);

                if (event.getSoldTickets() > 0) {
                    event.setSoldTickets(event.getSoldTickets() - 1);
                    eventRepository.save(event);
                }

                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .status(ApiResponse.Status.builder().code(200).message("Đơn hàng đã bị hủy hoặc hết hạn.")
                                .build())
                        .data(paymentLinkData.getStatus().name())
                        .build());
            } else {
                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .status(ApiResponse.Status.builder().code(200).message("Đơn hàng vẫn đang chờ thanh toán.")
                                .build())
                        .data("PENDING")
                        .build());
            }

        } catch (Exception e) {
            log.error("Lỗi khi gọi API PayOS để đồng bộ đơn hàng {}", orderCode, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @GetMapping("/my-tickets-pending")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyPendingPayments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Lấy danh sách đơn hàng đang chờ thanh toán (PENDING) cho user: {}", email);

        List<Payment> pendingPayments = paymentRepository
                .findByRegistration_User_EmailAndStatusOrderByCreatedAtDesc(email, PaymentStatus.PENDING);

        List<RegistrationResponse> data = pendingPayments.stream().map(payment -> {
            Registration registration = payment.getRegistration();
            return RegistrationResponse.builder()
                    .registrationId(registration.getId())
                    .eventId(registration.getEvent().getId())
                    .eventStartTime(registration.getEvent().getStartTime())
                    .userId(registration.getUser().getId())
                    .registrationCode(registration.getRegistrationCode())
                    .status(registration.getStatus().name())
                    .createdAt(registration.getCreatedAt())
                    .checkoutUrl(payment.getCheckoutUrl())
                    .orderCode(payment.getOrderCode())
                    .amount(payment.getAmount())
                    .build();
        }).toList();

        return ResponseEntity.ok(ApiResponse.<List<RegistrationResponse>>builder()
                .status(ApiResponse.Status.builder().code(200)
                        .message("Lấy danh sách các khoản chờ thanh toán thành công").build())
                .data(data)
                .build());
    }
}
