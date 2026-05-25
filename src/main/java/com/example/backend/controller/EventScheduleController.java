package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.EventScheduleResponse;
import com.example.backend.service.EventScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ⚠️  DEPRECATED — Các API trong Controller này đã được tích hợp vào EventController.
 *
 * Kể từ phiên bản mới:
 *  - Tạo/Sửa/Đồng bộ lịch trình → gửi kèm field "schedules" trong POST /create-event hoặc PUT /update-event/{id}
 *  - Xem lịch trình → đã trả về trong GET /public/event-detail/{id} (field "schedules")
 *
 * Controller này CHỈ GIỮ LẠI endpoint Public GET dự phòng nếu Frontend cần
 * lấy riêng lịch trình mà không muốn fetch toàn bộ chi tiết event.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventScheduleController {

    private final EventScheduleService eventScheduleService;

    /**
     * [GIỮ LẠI - Public] Lấy danh sách lịch trình của 1 sự kiện.
     * Kết quả này đã được bao gồm trong GET /public/event-detail/{id}.
     * Endpoint này chỉ để dùng khi cần lấy riêng lịch trình mà không fetch toàn bộ event.
     */
    @GetMapping("/public/events/{eventId}/schedules")
    public ResponseEntity<ApiResponse<List<EventScheduleResponse>>> getSchedules(
                    @PathVariable("eventId") Long eventId) {
        List<EventScheduleResponse> data = eventScheduleService.getSchedulesByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.<List<EventScheduleResponse>>builder()
                        .status(ApiResponse.Status.builder()
                                        .code(200)
                                        .message("Lấy danh sách lịch trình thành công")
                                        .build())
                        .data(data)
                        .build());
    }
}
