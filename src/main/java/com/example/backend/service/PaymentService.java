package com.example.backend.service;

import com.example.backend.dto.response.MyPaymentTransactionResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.PaymentTransactionResponse;
import com.example.backend.dto.response.RegistrationResponse;
import com.example.backend.enums.PaymentStatus;

import java.time.LocalDate;

public interface PaymentService {

    /**
     * Lấy danh sách giao dịch thanh toán của một sự kiện
     * @param eventId ID sự kiện
     * @param keyword Tìm kiếm theo mã đơn, email, mã đăng ký
     * @param status Lọc theo trạng thái (có thể null)
     * @param from Bắt đầu từ ngày (có thể null)
     * @param to Đến ngày (có thể null)
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số phần tử mỗi trang
     * @return Danh sách giao dịch có phân trang
     */
    PageResponse<PaymentTransactionResponse> getEventPayments(Long eventId, String keyword, PaymentStatus status, LocalDate from, LocalDate to, int page, int size);

    String syncPaymentStatus(String orderCode);

    PageResponse<RegistrationResponse> getMyPendingPayments(int page, int size, String sort, String search);

    PageResponse<MyPaymentTransactionResponse> getMyPaymentHistory(int page, int size, String sort, String search, PaymentStatus status, LocalDate startDate, LocalDate endDate);
}
