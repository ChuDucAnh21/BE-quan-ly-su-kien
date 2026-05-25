package com.example.backend.service.impl;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.PaymentTransactionResponse;
import com.example.backend.entity.Event;
import com.example.backend.entity.Payment;
import com.example.backend.entity.User;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.enums.Role;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<PaymentTransactionResponse> getEventPayments(Long eventId, String keyword, PaymentStatus status, LocalDate from, LocalDate to, int page, int size) {
        
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
            log.warn("Truy cập trái phép vào danh sách thanh toán của sự kiện {} bởi user {}", eventId, currentUser.getId());
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // 3. Phân trang và sắp xếp: Mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 4. Xử lý thời gian
        LocalDateTime startDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime endDate = to != null ? to.atTime(LocalTime.MAX) : null;

        // 5. Lấy dữ liệu
        Page<Payment> paymentPage = paymentRepository.findPaymentsByEventIdWithFilters(eventId, keyword, status, startDate, endDate, pageable);

        // 5. Map sang DTO
        Page<PaymentTransactionResponse> mappedPage = paymentPage.map(payment -> PaymentTransactionResponse.builder()
                .paymentId(payment.getId())
                .orderCode(payment.getOrderCode())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .registrationCode(payment.getRegistration() != null ? payment.getRegistration().getRegistrationCode() : null)
                .userEmail(payment.getRegistration() != null && payment.getRegistration().getUser() != null ? payment.getRegistration().getUser().getEmail() : null)
                .userFullName(payment.getRegistration() != null && payment.getRegistration().getUser() != null ? payment.getRegistration().getUser().getFullName() : null)
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
}
