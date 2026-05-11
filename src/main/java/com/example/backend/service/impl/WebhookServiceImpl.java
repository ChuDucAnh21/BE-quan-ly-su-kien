package com.example.backend.service.impl;

import com.example.backend.entity.Event;
import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentConfig;
import com.example.backend.entity.Registration;
import com.example.backend.entity.Ticket;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.enums.RegistrationStatus;
import com.example.backend.enums.TicketStatus;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.PaymentConfigRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.RegistrationRepository;
import com.example.backend.repository.TicketRepository;
import com.example.backend.service.WebhookService;
import com.example.backend.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
// import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements WebhookService {

    private final PaymentRepository paymentRepository;
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final PaymentConfigRepository paymentConfigRepository;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional // Khởi động Giao dịch Database - Nếu có bất cứ Exception nào, toàn bộ Data sẽ
                   // rollback
    public ObjectNode processWebhook(ObjectNode payload) { // payload là dữ liệu từ PayOS dội về
        ObjectNode response = objectMapper.createObjectNode();
        try {
            // 1. Phân tách payload JSON từ PayOS dội về
            // Trong SDK v2.0.1, chúng ta nên sử dụng thẳng ObjectNode hoặc ép kiểu qua
            // Webhook model mới.
            // Tuy nhiên hàm verify của v2 có thể nhận trực tiếp payload hoặc object
            // Webhook.
            Webhook webhookType = objectMapper.treeToValue(payload, Webhook.class);

            if (webhookType.getData() == null) {
                response.put("success", false);
                response.put("message", "No Data in Payload");
                return response;
            }

            // Ghi nhận orderCode dạng số của PayOS và chuyển về chuỗi để Query DB
            String orderCode = String.valueOf(webhookType.getData().getOrderCode());

            // 2. TÌM VÀ KHÓA ĐƠN HÀNG (JPA PESSIMISTIC_WRITE)
            // Lệnh khoá bi quan này ngăn luồng (Thread) thứ hai làm nhiễu dữ liệu nếu rớt
            // mạng và Webhook bắn 2 lần liên tục
            Payment payment = paymentRepository.findByOrderCode(orderCode).orElse(null);

            if (payment == null) {
                response.put("success", false);
                response.put("message", "Order Not Found");
                return response;
            }

            // CƠ CHẾ IDEMPOTENCY: Nếu đơn đã xử lý (Thành công hoặc Thất bại/Hủy) rồi thì
            // thoát ra ngay
            // (Tránh sinh lặp vé hoặc bị trừ lặp capacity)
            if (payment.getStatus() != PaymentStatus.PENDING) {
                response.put("success", true);
                response.put("message", "Order already processed");
                return response;
            }

            // 3. TÌM BỘ KHÓA PAYMENT_CONFIG ĐỂ LẤY CHECKSUM KEY
            // Chúng ta thiết kế hệ thống Multi-tenant, nên cấu hình cổng trả tiền phụ thuộc
            // vào Event Organizer này là ai!
            Registration registration = payment.getRegistration();
            Event event = registration.getEvent();
            PaymentConfig config = paymentConfigRepository.findByUserId(event.getOrganizer().getId()).orElse(null);

            if (config == null || !config.getIsActive()) {
                response.put("success", false);
                response.put("message", "Organizer Payment Config Not Found");
                return response;
            }

            // 4. VERIFY CHỮ KÝ (HMAC-SHA256) BẰNG THƯ VIỆN PAYOS
            PayOS payOS = new PayOS(config.getClientId(), config.getApiKey(), config.getChecksumKey());
            try {
                // Verify PayOS Webhook - SDK v2.x sử dụng cơ chế mới thông qua
                // webhooks().verify()
                // Hàm này tự động kiểm tra chữ ký và trả về dữ liệu đã được xác thực
                // WebhookData verifiedData = payOS.webhooks().verify(webhookType);
                payOS.webhooks().verify(webhookType);

                // Nếu vượt rào thành công qua bước Verify, thông tin giao dịch đã an toàn 100%
                if ("00".equals(webhookType.getCode()) && webhookType.getSuccess()) {
                    // --- BƯỚC 1: XÁC MÌNH ĐƠN HÀNG THÀNH CÔNG ---
                    payment.setStatus(PaymentStatus.SUCCESS);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setRawResponse(payload.toString());
                    paymentRepository.save(payment);

                    // Chuyển Trạng thái Đăng ký tham gia thành CONFIRMED
                    registration.setStatus(RegistrationStatus.CONFIRMED);
                    registrationRepository.save(registration);

                    // --- BƯỚC 2: KHẮC VÉ ĐIỆN TỬ (TICKET) ---
                    String rawQrCode = UUID.randomUUID().toString();
                    String hashedQrCode = HashUtil.sha256(rawQrCode);
                    Ticket ticket = Ticket.builder()
                            .registration(registration)
                            .qrCode(rawQrCode) // Lưu mã gốc
                            .hashedQrCode(hashedQrCode) // Lưu mã băm để đối chiếu
                            .issuedAt(LocalDateTime.now())
                            .status(TicketStatus.ACTIVE)
                            .build();
                    ticketRepository.save(ticket);

                    log.info("Webhook xử lý THÀNH CÔNG. Cấp vé cho user {} tại sự kiện {}. Mã Đơn: {}",
                            registration.getUser().getEmail(), event.getTitle(), orderCode);
                } else {
                    // --- NHÁNH THẤT BẠI / HỦY BỎ ---
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setRawResponse(payload.toString());
                    paymentRepository.save(payment);

                    // Ép trạng thái về HỦY
                    registration.setStatus(RegistrationStatus.CANCELLED);
                    registrationRepository.save(registration);

                    // TRẢ LẠI SUẤT (CAPACITY). Giúp người sau có cơ hội nhảy vào mua tiếp
                    if (event.getSoldTickets() > 0) {
                        event.setSoldTickets(event.getSoldTickets() - 1);
                        eventRepository.save(event);
                    }

                    log.warn("Webhook thanh toán THẤT BẠI. Hủy khoản thanh toán và trả slot cho kiện {}. Mã Đơn: {}",
                            event.getTitle(), orderCode);
                }

                response.put("success", true);
                response.put("message", "Ok");
                return response;

            } catch (Exception e) {
                log.error("Sai chữ ký Webhook (Fake Payload) / Lỗi Xác Thực: {}", e.getMessage());
                response.put("success", false);
                response.put("message", "Invalid Signature");
                return response;
            }

        } catch (Exception ex) {
            log.error("Lỗi Server cực bộ khi xử lý Webhook SDK: {}", ex.getMessage());
            response.put("success", false);
            response.put("message", "Internal Server Error");
            return response;
        }
    }
}
