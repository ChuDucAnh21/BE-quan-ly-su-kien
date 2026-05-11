package com.example.backend.controller;

import com.example.backend.dto.request.EventScheduleRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.EventScheduleResponse;
import com.example.backend.service.EventScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventScheduleController {

        private final EventScheduleService eventScheduleService;

        // 1. GET ALL (Public API - Ai cũng có thể xem Lịch trình của một sự kiện)
        @GetMapping("/public/events/{eventId}/schedules")
        public ResponseEntity<ApiResponse<List<EventScheduleResponse>>> getSchedules(
                        @PathVariable("eventId") Long eventId) {
                List<EventScheduleResponse> data = eventScheduleService.getSchedulesByEventId(eventId);
                ApiResponse<List<EventScheduleResponse>> response = ApiResponse.<List<EventScheduleResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Lấy danh sách lịch trình thành công")
                                                .build())
                                .data(data)
                                .build();
                return ResponseEntity.ok(response);
        }

        // 2. CREATE BATCH (Tạo 1 mảng Lịch trình cùng lúc để khỏi gọi đi gọi lại API)
        @PostMapping("/events/{eventId}/schedules")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ResponseEntity<ApiResponse<List<EventScheduleResponse>>> createSchedules(
                        @PathVariable("eventId") Long eventId,
                        @Valid @RequestBody List<EventScheduleRequest> requests) {

                List<EventScheduleResponse> data = eventScheduleService.createSchedules(eventId, requests);
                ApiResponse<List<EventScheduleResponse>> response = ApiResponse.<List<EventScheduleResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Thêm mới danh sách lịch trình thành công")
                                                .build())
                                .data(data)
                                .build();
                return ResponseEntity.ok(response);
        }

        // 3. UPDATE BATCH (Đồng bộ: Xoá cũ, Thêm mới)
        @PutMapping("/events/{eventId}/schedules")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ResponseEntity<ApiResponse<List<EventScheduleResponse>>> updateSchedules(
                        @PathVariable("eventId") Long eventId,
                        @Valid @RequestBody List<EventScheduleRequest> requests) {

                List<EventScheduleResponse> data = eventScheduleService.updateSchedules(eventId, requests);
                ApiResponse<List<EventScheduleResponse>> response = ApiResponse.<List<EventScheduleResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Đồng bộ danh sách lịch trình thành công")
                                                .build())
                                .data(data)
                                .build();
                return ResponseEntity.ok(response);
        }

        // 4. (Organizer) XÓA SẠCH CHUỒNG (Toàn bộ Lịch trình của Sự kiện)
        @DeleteMapping("/events/{eventId}/schedules")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ResponseEntity<ApiResponse<Void>> deleteAllSchedules(@PathVariable("eventId") Long eventId) {
                eventScheduleService.deleteAllSchedules(eventId);

                ApiResponse<Void> response = ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Đã xóa trắng toàn bộ lịch trình của sự kiện")
                                                .build())
                                .data(null)
                                .build();
                return ResponseEntity.ok(response);
        }

        // 5. (Organizer) XÓA LẺ 1 LỊCH TRÌNH
        @DeleteMapping("/events/{eventId}/schedules/{scheduleId}")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ResponseEntity<ApiResponse<Void>> deleteSchedule(
                        @PathVariable("eventId") Long eventId,
                        @PathVariable("scheduleId") Long scheduleId) {
                eventScheduleService.deleteSchedule(eventId, scheduleId);

                ApiResponse<Void> response = ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Xóa lịch trình con thành công")
                                                .build())
                                .data(null)
                                .build();
                return ResponseEntity.ok(response);
        }
}
