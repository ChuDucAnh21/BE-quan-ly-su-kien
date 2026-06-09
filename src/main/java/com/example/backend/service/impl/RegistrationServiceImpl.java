package com.example.backend.service.impl;

import com.example.backend.dto.request.RegistrationRequest;
import com.example.backend.dto.response.RegistrationResponse;
import com.example.backend.entity.Event;
import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentConfig;
import com.example.backend.entity.Registration;
import com.example.backend.entity.Ticket;
import com.example.backend.entity.User;
import com.example.backend.enums.EventStatus;
import com.example.backend.enums.PaymentProvider;
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
import com.example.backend.repository.UserRepository;
import com.example.backend.service.RegistrationService;
import com.example.backend.util.HashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentConfigRepository paymentConfigRepository;
    private final TicketRepository ticketRepository;

    // URL callback của FE sau khi thanh toán PayOS, cấu hình trong
    // application.properties
    @Value("${app.payment.return-url:http://localhost:3000/notifincation/register/registerSuccess}")
    private String paymentReturnUrl;

    @Value("${app.payment.cancel-url:http://localhost:3000/notifincation/register/registerFaild}")
    private String paymentCancelUrl;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional // Đảm bảo tính nguyên tử: Xảy ra lỗi là rollback toàn bộ (Vé + Giao dịch)
    public RegistrationResponse registerEvent(RegistrationRequest request) {
        User currentUser = getCurrentUser();
        Long eventId = request.getEventId();

        // 1. Lấy thông tin Event với khoá bi quan (PESSIMISTIC_WRITE) để khóa dòng dữ
        // liệu trong DB.
        // Điều này đảm bảo khi 100 user ùa vào mua vé cùng 1 mili-giây, các truy vấn sẽ
        // phải xếp hàng
        // chờ nhau thay vì đọc chung 1 số lượng SoldTickets cũ rích (Chống bán lố vé /
        // Overbooking).
        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // 2. Chặn các trạng thái không hợp lệ
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new AppException(ErrorCode.EVENT_NOT_STARTED);
        }

        // 2. Chặn nếu sự kiện chưa tới giờ mở bán vé
        if (event.getRegistrationStartTime() != null
                && LocalDateTime.now().isBefore(event.getRegistrationStartTime())) {
            throw new AppException(ErrorCode.REGISTRATION_NOT_OPENED);
        }

        // Chặn nếu sự kiện đã kết thúc đăng ký (quá Registration End Time hoặc quá
        // Start Time)
        LocalDateTime deadline = event.getRegistrationEndTime() != null ? event.getRegistrationEndTime()
                : event.getStartTime();
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new AppException(ErrorCode.REGISTRATION_CLOSED);
        }

        // 3. Kiểm tra số lượng vé khả dụng
        if (event.getSoldTickets() >= event.getCapacity()) {
            throw new AppException(ErrorCode.TICKET_SOLD_OUT);
        }

        // 4. Khóa User (UniqueConstraint): 1 User chỉ được mua 1 vé cho 1 Event
        java.util.Optional<Registration> optReg = registrationRepository.findByEventIdAndUserId(eventId,
                currentUser.getId());
        if (optReg.isPresent()) {
            Registration existingReg = optReg.get();
            if (existingReg.getStatus() == RegistrationStatus.CANCELLED) {
                // Nếu đã hủy trước đó -> Xóa sạch lịch sử để tạo mới
                ticketRepository.findByRegistrationId(existingReg.getId()).ifPresent(ticketRepository::delete);
                paymentRepository.findByRegistrationId(existingReg.getId()).ifPresent(paymentRepository::delete);
                registrationRepository.delete(existingReg);

                // Ép Hibernate đẩy lệnh DELETE xuống Database ngay lập tức
                // Tránh lỗi Unique Constraint khi lệnh INSERT bên dưới chạy
                registrationRepository.flush();
            } else {
                throw new AppException(ErrorCode.ALREADY_REGISTERED);
            }
        }

        // 5. GIỮ CHỖ (Bảo vệ rổ hàng): Tăng tạm thời SoldTickets lên 1
        event.setSoldTickets(event.getSoldTickets() + 1);
        eventRepository.save(event);

        // Sinh mã đăng ký ngẫu nhiên 8 ký tự : mã này dùng để hiển thị cho user
        String regCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 6. Xử lý phân nhánh Miễn phí & Trả phí
        boolean isFree = event.getTicketPrice().compareTo(BigDecimal.ZERO) == 0;

        Registration registration = Registration.builder() // Tạo 1 bản ghi đăng ký tham gia sự kiện
                .event(event)
                .user(currentUser)
                .registrationCode(regCode)
                .status(isFree ? RegistrationStatus.CONFIRMED : RegistrationStatus.PENDING_PAYMENT)
                .build();
        registration = registrationRepository.save(registration);

        Payment payment;
        String checkoutUrl = null;
        // orderCode của PayOS yêu cầu kiểu số (long, tối đa số có 53-bit).
        // Ta sẽ dùng: Epoch milliseconds (13 chữ số) + Sinh ngẫu nhiên 2 chữ số (để
        // chống trùng thời gian) = 15 chữ số.
        // 15 chữ số vẫn an toàn dưới ngưỡng 53-bit (max ~9.007.199.254.740.991)
        int randomNum = (int) (Math.random() * 100);
        String orderCode = String.valueOf(System.currentTimeMillis()) + String.format("%02d", randomNum);

        if (isFree) {
            // Nhánh VÉ FREE: Cắm một bản ghi "Thanh toán ảo" (Dummy Payment)
            payment = Payment.builder()
                    .registration(registration)
                    .orderCode(orderCode)
                    .provider(PaymentProvider.SYSTEM) // Đánh dấu là hệ thống tự sinh
                    .amount(BigDecimal.ZERO)
                    .status(PaymentStatus.SUCCESS)
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            // Bất cứ khi nào vé được CONFIRMED (tức vé Free này), NỐI LOGIC SINH VÉ QR tự
            // động
            String rawQrCode = UUID.randomUUID().toString();
            String hashedQrCode = HashUtil.sha256(rawQrCode);
            Ticket ticket = Ticket.builder()
                    .registration(registration)
                    .qrCode(rawQrCode) // Random 36 ký tự làm mã QR độc nhất
                    .hashedQrCode(hashedQrCode) // Lưu mã băm
                    .issuedAt(LocalDateTime.now())
                    .status(TicketStatus.ACTIVE) // Vé khả dụng ngay lập tức
                    .build();
            ticketRepository.save(ticket);

            log.info("Cấp phát vé Miễn phí và tạo QRCode thành công cho User: {} - Sự kiện: {}", currentUser.getEmail(),
                    event.getTitle());
        } else {
            // Nhánh VÉ PAID: Kiểm tra Organizer đã thiết lập PayOS chưa
            PaymentConfig config = paymentConfigRepository.findByUserId(event.getOrganizer().getId())
                    .orElse(null);
            if (config == null || !config.getIsActive()) {
                // Đề phòng Organizer "lách luật" lúc tạo event
                throw new AppException(ErrorCode.PAYMENT_CONFIG_NOT_FOUND);
            }

            // Sinh bản ghi Payment PENDING chờ Checkout
            payment = Payment.builder()
                    .registration(registration)
                    .orderCode(orderCode)
                    .provider(PaymentProvider.PAYOS)
                    .amount(event.getTicketPrice())
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            // Kết nối với PayOS SDK để lấy Link thanh toán thật
            try {
                // 1. Khởi tạo đối tượng PayOS với cấu hình của Event Organizer
                PayOS payOS = new PayOS(
                        config.getClientId(),
                        config.getApiKey(),
                        config.getChecksumKey());

                // 2. Chuẩn bị URL đường dẫn FrontEnd để PayOS trả người dùng về sau khi quét QR
                // xong (cấu hình trong application.properties)
                String cancelUrl = paymentCancelUrl;
                String returnUrl = paymentReturnUrl;

                // 3. Đổi sang mô hình dữ liệu v2 của SDK 2.x
                CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest
                        .builder()
                        .orderCode(Long.parseLong(orderCode))
                        .amount(event.getTicketPrice().longValue()) // SDK v2 nhận amount dạng long
                        .description("Thanh toan ve su kien")
                        .cancelUrl(cancelUrl)
                        .returnUrl(returnUrl)
                        .build();

                // 4. Lệnh kích hoạt SDK v2.x (Sử dụng interface mới qua paymentRequests())
                CreatePaymentLinkResponse data = payOS.paymentRequests()
                        .create(paymentData);

                // 5. Lấy Checkout URL xịn từ Response
                checkoutUrl = data.getCheckoutUrl();

            } catch (Exception e) {
                log.error("Lỗi khi kết nối đến PayOS để tạo QRCode chuyển khoản: {}", e.getMessage());
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Trả lỗi 500 do lỗi SDK viễn thông
            }

            // Cập nhật URL xịn vào Giỏ hàng để đề phòng FrontEnd muốn lấy lại
            payment.setCheckoutUrl(checkoutUrl);
            paymentRepository.save(payment);

            log.info("Tạo giỏ hàng chờ thanh toán PayOS. Đơn: {} - Số tiền: {}", orderCode, payment.getAmount());
        }

        // 7. Chuẩn bị dữ liệu trả về cho Client
        return RegistrationResponse.builder()
                .registrationId(registration.getId())
                .eventId(event.getId())
                .eventStartTime(event.getStartTime())
                .userId(currentUser.getId())
                .registrationCode(regCode)
                .status(registration.getStatus().name())
                .createdAt(registration.getCreatedAt())
                .checkoutUrl(checkoutUrl) // Trả dòng này cho FE để FE điều hướng KH đi chuyển khoản , nếu là vé
                                          // free thì checkoutUrl = null
                .orderCode(orderCode)
                .amount(payment.getAmount())
                .build();
    }

    @Override
    @Transactional // Khóa giao dịch: phải Rollback toàn bộ trạng thái và SoldTickets nếu có lỗi
                   // xảy ra
    public void cancelRegistration(Long registrationId) {
        User currentUser = getCurrentUser();

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new AppException(ErrorCode.REGISTRATION_NOT_FOUND));

        // 0. Phân quyền: Cấm người dùng Hủy giùm vé của người khác
        if (!registration.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        Event event = registration.getEvent();

        // Optional<Ticket> ticketOpt =
        // ticketRepository.findByRegistrationId(registrationId);

        // // 1. Chặn hủy nếu vé đã được sử dụng (Check-in rồi thì miễn trả vé)
        // if (ticketOpt.isPresent() && (ticketOpt.get().getStatus() ==
        // TicketStatus.USED
        // || ticketOpt.get().getStatus() == TicketStatus.INVALID)) {
        // throw new AppException(ErrorCode.CANNOT_CANCEL_PAID_TICKET);
        // }

        // 2. Chặn hủy nếu bản ghi đã bị vô hiệu hóa trước đó
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            return; // Idempotency: Đã hủy rồi thì không làm gì thêm
        }

        // Chỉ cho phép hủy nếu là Vé Miễn Phí (ticket_price == 0)
        // HOẶC vé trả phí nhưng CHƯA THANH TOÁN (PENDING_PAYMENT)
        boolean isFreeTicket = event.getTicketPrice().compareTo(BigDecimal.ZERO) == 0;
        boolean isUnpaid = registration.getStatus() == RegistrationStatus.PENDING_PAYMENT;

        if (!isFreeTicket && !isUnpaid) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_PAID_TICKET);
        }

        if (LocalDateTime.now().isAfter(event.getStartTime())) {
            throw new AppException(ErrorCode.EVENT_NOT_STARTED); // Tái chế lại mã lỗi này mang ý "Sự kiện ngoại
                                                                 // tuyến/khóa đăng ký"
        }

        // 1. Chuyển trạng thái Đăng ký tham gia về CANCELLED
        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        // 2. Vô hiệu hóa mã QR điện tử (Ticket) nếu vé đã được cấp
        // (ticketOpt.isPresent())
        ticketRepository.findByRegistrationId(registrationId).ifPresent(ticket -> {
            ticket.setStatus(TicketStatus.INVALID);
            ticketRepository.save(ticket);
        });
        Payment payment = paymentRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.setStatus(PaymentStatus.CANCELLED);

        paymentRepository.save(payment);

        // 4. Giải phóng giỏ hàng: Trả 1 suất (Capacity) lại cho Hệ thống
        if (event.getSoldTickets() > 0) {
            event.setSoldTickets(event.getSoldTickets() - 1);
            eventRepository.save(event);
        }

        log.info("Hủy đăng ký thành công: User {}, Event {}, Trạng thái cũ: {}", currentUser.getEmail(),
                event.getTitle(), isFreeTicket ? "VÉ FREE" : "VÉ CHƯA THANH TOÁN");
    }
}
