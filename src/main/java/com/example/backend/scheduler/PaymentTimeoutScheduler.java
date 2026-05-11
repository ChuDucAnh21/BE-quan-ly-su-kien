package com.example.backend.scheduler;

import com.example.backend.entity.Event;
import com.example.backend.entity.Payment;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.enums.RegistrationStatus;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeoutScheduler {

    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;

    /**
     * CronJob: Chạy mỗi 5 phút một lần (300.000 ms)
     * Nhiệm vụ: Tìm tất cả các giao dịch đang PENDING quá 5 phút
     * - Hủy giao dịch (Payment -> CANCELLED)
     * - Hủy đăng ký (Registration -> CANCELLED)
     * - Trả lại vé cho hệ thống (SoldTickets - 1)
     */

    // cứ 5 phút chạy 1 lần, các giao dịch đang PENDING quá 5 phút sẽ bị huỷ
    @Scheduled(fixedRate = 300000) // @Scheduled là annotation của Spring Boot để lên lịch cho một phương thức
    @Transactional
    public void cancelUnpaidOrders() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5); // Thời hạn 5 phút

        List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING, timeoutThreshold);

        if (expiredPayments.isEmpty()) {
            return;
        }

        log.info("Bắt đầu quét và dọn dẹp {} đơn hàng ngâm quá 5 phút...", expiredPayments.size());

        for (Payment payment : expiredPayments) {
            // 1. Huỷ Payment
            payment.setStatus(PaymentStatus.CANCELLED);

            // 2. Huỷ Registration
            payment.getRegistration().setStatus(RegistrationStatus.CANCELLED);

            // 3. Trả lại capacity (Nhả vé)
            Event event = payment.getRegistration().getEvent();
            if (event.getSoldTickets() > 0) {
                event.setSoldTickets(event.getSoldTickets() - 1);
                eventRepository.save(event);
            }
        }

        paymentRepository.saveAll(expiredPayments); // lưu lại trạng thái đã huỷ vào DB để sau này thống kê báo cáo
        log.info("Đã hoàn tất dọn rác và hoàn trả Ticket Capacity cho hệ thống.");
    }
}
