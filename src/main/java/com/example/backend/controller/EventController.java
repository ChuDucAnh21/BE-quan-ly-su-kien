package com.example.backend.controller;

import com.example.backend.dto.request.EventRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.EventResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.PaymentTransactionResponse;
import com.example.backend.enums.EventStatus;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.service.EventService;
import com.example.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventController {

        private final EventService eventService;
        private final PaymentService paymentService;

        /**
         * 1. Public API: Ai cũng có thể vào xem danh sách Sự Kiện
         * Hỗ trợ tìm kiếm, phân trang và bộ lọc.
         */
        @GetMapping("/public/events-all")
        public ApiResponse<PageResponse<EventResponse>> getAllEvents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) java.util.List<String> categories,
                        @RequestParam(required = false) EventStatus status,
                        @RequestParam(required = false) Long organizerId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startTime,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endTime,
                        @RequestParam(required = false) java.math.BigDecimal minPrice,
                        @RequestParam(required = false) java.math.BigDecimal maxPrice,
                        @RequestParam(required = false) Boolean isFree,
                        @RequestParam(defaultValue = "desc") String sort) {

                PageResponse<EventResponse> data = eventService.getAllEvents(page, size, keyword, categories, status,
                                organizerId, startTime, endTime, minPrice, maxPrice, isFree, sort);
                return ApiResponse.<PageResponse<EventResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Lấy danh sách sự kiện thành công")
                                                .build())
                                .data(data)
                                .build();
        }

        /**
         * 2. Public API: Xem chi tiết 1 sự kiện
         */
        @GetMapping("/public/event-detail/{id}")
        public ApiResponse<EventResponse> getEventById(@PathVariable Long id) {
                return ApiResponse.<EventResponse>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Lấy thông tin sự kiện thành công")
                                                .build())
                                .data(eventService.getEventById(id))
                                .build();
        }

        /**
         * 3. Organizer API: Tạo sự kiện mới
         * Dùng @ModelAttribute vì chúng ta cần nhận File ảnh (bannerFile) kết hợp với
         * các text fields
         */
        @PostMapping("/create-event")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<EventResponse> createEvent(@Valid @ModelAttribute EventRequest request) {
                return ApiResponse.<EventResponse>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Tạo sự kiện bản nháp thành công")
                                                .build())
                                .data(eventService.createEvent(request))
                                .build();
        }

        /**
         * 4. Organizer API: Sửa nội dung sự kiện
         */
        @PutMapping("/update-event/{id}")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ApiResponse<EventResponse> updateEvent(
                        @PathVariable Long id,
                        @Valid @ModelAttribute EventRequest request) {

                return ApiResponse.<EventResponse>builder() // EventResponse là kiểu dữ liệu trả về của data
                                .status(ApiResponse.Status.builder()
                                                .message("Cập nhật sự kiện thành công")
                                                .build())
                                .data(eventService.updateEvent(id, request))
                                .build();
        }

        /**
         * 5. Organizer/Admin API: Đổi trạng thái sự kiện
         * (Từ DRAFT -> PUBLISHED, hoặc CANCELLED...)
         */
        @PutMapping("/events/{id}/status")
        @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
        public ApiResponse<Void> changeEventStatus(
                        @PathVariable Long id,
                        @RequestParam EventStatus newStatus) {

                eventService.changeEventStatus(id, newStatus);
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Cập nhật trạng thái sự kiện thành công.")
                                                .build())
                                .build();
        }

        /**
         * 6. Organizer/Admin API: Xóa Sự kiện ra khỏi hệ thống
         * Lưu ý logic: Nếu đã có vé bán ra, nó sẽ chuyển thành CANCELLED thay vì Xóa
         * cứng.
         */
        @DeleteMapping("/delete-event/{id}")
        @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
        public ApiResponse<Void> deleteEvent(@PathVariable Long id) {

                eventService.deleteEvent(id);
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Xóa hoặc hủy sự kiện thành công")
                                                .build())
                                .build();
        }

        /**
         * 7. Staff/Admin API: Xem danh sách sự kiện được phân công của một Staff cụ thể
         * (Staff chỉ được xem của chính mình, Admin/Organizer được xem của tất cả)
         */
        @GetMapping("/events/assigned-to-staff/{staffId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'STAFF')")
        public ApiResponse<PageResponse<EventResponse>> getAssignedEventsByStaffId(
                        @PathVariable Long staffId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "desc") String sort,
                        @RequestParam(required = false) String search) {

                return ApiResponse.<PageResponse<EventResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Lấy danh sách sự kiện phân công cho nhân viên thành công")
                                                .build())
                                .data(eventService.getAssignedEventsByStaffId(staffId, page, size, sort, search))
                                .build();
        }

        /**
         * 8. Organizer/Admin API: Xem danh sách giao dịch thanh toán của sự kiện
         * Hỗ trợ tìm kiếm theo mã đơn, email và lọc theo trạng thái, khoảng thời gian.
         */
        @GetMapping("/events/{eventId}/payments")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
        public ApiResponse<PageResponse<PaymentTransactionResponse>> getEventPayments(
                        @PathVariable Long eventId,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) PaymentStatus status,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                PageResponse<PaymentTransactionResponse> data = paymentService.getEventPayments(eventId, keyword,
                                status, from, to, page, size);

                return ApiResponse.<PageResponse<PaymentTransactionResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Lấy danh sách thanh toán thành công")
                                                .build())
                                .data(data)
                                .build();
        }
}
