package com.example.backend.controller;

import com.example.backend.dto.request.EventRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.EventResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.enums.EventStatus;
import com.example.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventController {

        private final EventService eventService;

        /**
         * 1. Public API: Ai cũng có thể vào xem danh sách Sự Kiện
         * Hỗ trợ tìm kiếm, phân trang và bộ lọc.
         */
        @GetMapping("/public/events-all")
        public ApiResponse<PageResponse<EventResponse>> getAllEvents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) EventStatus status,
                        @RequestParam(required = false) Long organizerId) {

                PageResponse<EventResponse> data = eventService.getAllEvents(page, size, keyword, category, status,
                                organizerId);
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
                                                .message("Cập nhật trạng thái sự kiện thành: " + newStatus)
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
                                                .message("Thao tác loại bỏ sự kiện thành công")
                                                .build())
                                .build();
        }
}
