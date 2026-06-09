package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.StaffResponse;
import com.example.backend.enums.UserStatus;
import com.example.backend.service.EventStaffService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/events/{eventId}/staffs")
@RequiredArgsConstructor
public class EventStaffController {

        private final EventStaffService eventStaffService;

        // API 2: Lấy danh sách Staff đang phụ trách sự kiện này
        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'STAFF')")
        public ResponseEntity<ApiResponse<PageResponse<StaffResponse>>> getStaffsByEvent(
                        @PathVariable("eventId") Long eventId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "desc") String sort,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) UserStatus status) {

                Direction direction = "asc".equalsIgnoreCase(sort)
                                ? Direction.ASC
                                : Direction.DESC;
                Pageable pageable = PageRequest.of(page,
                                size, Sort.by(direction, "createdAt"));

                PageResponse<StaffResponse> data = eventStaffService
                                .getStaffsByEventId(eventId, search, status, pageable);
                ApiResponse<PageResponse<StaffResponse>> response = ApiResponse.<PageResponse<StaffResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Lấy danh sách nhân viên của sự kiện thành công")
                                                .build())
                                .data(data)
                                .build();
                return ResponseEntity.ok(response);
        }

        // API 3: Đồng bộ danh sách Staff: Xoá cũ dư thừa, thêm mới chưa có, giữ nguyên
        // người hiện tại
        @PutMapping
        @PreAuthorize("hasRole('ORGANIZER')")
        public ResponseEntity<ApiResponse<List<StaffResponse>>> syncStaffs(
                        @PathVariable("eventId") Long eventId,
                        @RequestBody Set<Long> staffIds) {

                List<StaffResponse> data = eventStaffService.syncEventStaffs(eventId, staffIds);
                ApiResponse<List<StaffResponse>> response = ApiResponse.<List<StaffResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Phân công nhân viên thành công")
                                                .build())
                                .data(data)
                                .build();
                return ResponseEntity.ok(response);
        }

        // API 4: Xoá thẻ phân công của 1 nhân viên ra khỏi Event
        @DeleteMapping("/{staffId}")
        @PreAuthorize("hasRole('ORGANIZER')")
        public ResponseEntity<ApiResponse<Void>> removeStaffFromEvent(
                        @PathVariable("eventId") Long eventId,
                        @PathVariable("staffId") Long staffId) {

                eventStaffService.removeStaffFromEvent(eventId, staffId);
                ApiResponse<Void> response = ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .code(200)
                                                .message("Xoá thẻ phân công nhân viên thành công")
                                                .build())
                                .build();
                return ResponseEntity.ok(response);
        }
}
