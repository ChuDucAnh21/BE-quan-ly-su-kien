package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.MyPaymentTransactionResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.RegistrationResponse;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.backend.enums.PaymentStatus;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

        private final PaymentService paymentService;

        // API kiểm tra trạng thái thanh toán và sinh vé khi người dùng reload lại trang
        @GetMapping("/sync/{orderCode}")
        public ResponseEntity<ApiResponse<String>> syncPaymentStatus(@PathVariable String orderCode) {
                log.info("Chủ động đồng bộ trạng thái đơn hàng: {}", orderCode);
                String result = paymentService.syncPaymentStatus(orderCode);

                String message = "";
                if ("ALREADY_PAID".equals(result)) {
                        message = "Đơn hàng đã được thanh toán thành công từ trước.";
                } else if ("PAID_AND_TICKET_CREATED".equals(result)) {
                        message = "Kiểm tra thấy đơn hàng đã thanh toán. Đã sinh vé!";
                } else if ("CANCELLED".equals(result) || "EXPIRED".equals(result)) {
                        message = "Đơn hàng đã bị hủy hoặc hết hạn.";
                } else if ("PENDING".equals(result)) {
                        message = "Đơn hàng vẫn đang chờ thanh toán.";
                } else {
                        message = "Đơn hàng đã được xử lý từ trước (Hủy/Thất bại).";
                }

                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .status(ApiResponse.Status.builder().code(200).message(message).build())
                                .data(result)
                                .build());
        }

        @GetMapping("/my-tickets-pending")
        @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN','STAFF')")
        public ResponseEntity<ApiResponse<PageResponse<RegistrationResponse>>> getMyPendingPayments(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "desc") String sort,
                        @RequestParam(required = false) String search) {
                
                log.info("Lấy danh sách đơn hàng đang chờ thanh toán (PENDING)");
                PageResponse<RegistrationResponse> pageResponse = paymentService.getMyPendingPayments(page, size, sort, search);

                return ResponseEntity
                                .ok(ApiResponse.<PageResponse<RegistrationResponse>>builder()
                                                .status(ApiResponse.Status.builder().code(200)
                                                                .message("Lấy danh sách các khoản chờ thanh toán thành công")
                                                                .build())
                                                .data(pageResponse)
                                                .build());
        }

        @GetMapping("/my-payment-history")
        @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN', 'STAFF')")
        public ResponseEntity<ApiResponse<PageResponse<MyPaymentTransactionResponse>>> getMyPaymentHistory(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "desc") String sort,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) PaymentStatus status,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                
                log.info("Lấy lịch sử giao dịch cá nhân");
                PageResponse<MyPaymentTransactionResponse> pageResponse = paymentService.getMyPaymentHistory(page, size, sort, search, status, startDate, endDate);

                return ResponseEntity.ok(ApiResponse.<PageResponse<MyPaymentTransactionResponse>>builder()
                                .status(ApiResponse.Status.builder().code(200).message("Lấy lịch sử giao dịch thành công").build())
                                .data(pageResponse)
                                .build());
        }
}
