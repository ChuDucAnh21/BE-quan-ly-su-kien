package com.example.backend.controller;

import com.example.backend.dto.request.BatchCheckInRequest;
import com.example.backend.dto.request.CheckInRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.BatchCheckInResponse;
import com.example.backend.dto.response.CheckInLogResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.TicketResponse;
import com.example.backend.enums.CheckInMethod;
import com.example.backend.enums.TicketStatus;
import com.example.backend.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TicketController {

        private final TicketService ticketService;

        // 1. Xem danh sách toàn bộ vé hệ thống (Dành cho Admin/Organizer tra cứu theo
        // Event)
        @GetMapping("/events/{eventId}/tickets")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'STAFF')")
        public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getTicketsByEvent(
                        @PathVariable Long eventId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) com.example.backend.enums.TicketStatus status) {

                Pageable pageable = PageRequest.of(page, size);
                Page<TicketResponse> ticketPage = ticketService.getTicketsCustomerByEventId(eventId, search, status,
                                pageable);

                PageResponse<TicketResponse> pageResponse = PageResponse.<TicketResponse>builder()
                                .currentPage(ticketPage.getNumber())
                                .totalPages(ticketPage.getTotalPages())
                                .pageSize(ticketPage.getSize())
                                .totalElements(ticketPage.getTotalElements())
                                .data(ticketPage.getContent())
                                .build();

                return ResponseEntity.ok(ApiResponse.<PageResponse<TicketResponse>>builder()
                                .status(ApiResponse.Status.builder().code(200).message("Lấy danh sách vé thành công")
                                                .build())
                                .data(pageResponse)
                                .build());
        }

        // 2. Khách hàng tự lấy danh sách vé của mình
        @GetMapping("/tickets/my-tickets")
        @PreAuthorize("isAuthenticated()") // User nào cũng lấy được vé của chính mình
        public ApiResponse<PageResponse<TicketResponse>> getMyTickets(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "desc") String sort,
                        @RequestParam(required = false) TicketStatus status) {
                Direction direction = "asc".equalsIgnoreCase(sort) ? Direction.ASC : Direction.DESC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
                Page<TicketResponse> dataPage = ticketService.getMyTickets(status, pageable);
                PageResponse<TicketResponse> pageResponse = PageResponse.<TicketResponse>builder()
                                .currentPage(dataPage.getNumber())
                                .totalPages(dataPage.getTotalPages())
                                .pageSize(dataPage.getSize())
                                .totalElements(dataPage.getTotalElements())
                                .data(dataPage.getContent())
                                .build();

                return ApiResponse.<PageResponse<TicketResponse>>builder()
                                .status(ApiResponse.Status.builder().code(200).message("Lấy danh sách vé thành công")
                                                .build())
                                .data(pageResponse)
                                .build();
        }

        // Lấy lịch sử Check-in của Sự kiện
        @GetMapping("/events/{eventId}/checkin-history")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'STAFF')")
        public ApiResponse<PageResponse<CheckInLogResponse>> getCheckInHistory(
                        @PathVariable("eventId") Long eventId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) CheckInMethod method,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
                Pageable pageable = PageRequest.of(page, size);
                Page<CheckInLogResponse> dataPage = ticketService
                                .getCheckInHistoryByEventId(eventId, search, method, fromDate, toDate, pageable);
                PageResponse<CheckInLogResponse> pageResponse = PageResponse.<CheckInLogResponse>builder()
                                .currentPage(dataPage.getNumber())
                                .totalPages(dataPage.getTotalPages())
                                .pageSize(dataPage.getSize())
                                .totalElements(dataPage.getTotalElements())
                                .data(dataPage.getContent())
                                .build();

                return ApiResponse.<PageResponse<CheckInLogResponse>>builder()
                                .status(ApiResponse.Status.builder().code(200)
                                                .message("Lấy lịch sử Check-in thành công")
                                                .build())
                                .data(pageResponse)
                                .build();
        }

        // 3. Check-in thông qua Camera Scanner tại cổng
        @PostMapping("/checkin")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'STAFF')")
        public ResponseEntity<ApiResponse<TicketResponse>> processCheckIn(
                        @Valid @RequestBody CheckInRequest request) {
                TicketResponse response = ticketService.processCheckIn(request);
                return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                                .status(ApiResponse.Status.builder().code(200).message("Check-in thành công! Cổng mở.")
                                                .build())
                                .data(response)
                                .build());
        }

        // 4. Đồng bộ danh sách Check-in Offline (Batch Sync)
        @PostMapping("/checkin/batch")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'STAFF')")
        public ResponseEntity<ApiResponse<BatchCheckInResponse>> processBatchCheckIn(
                        @Valid @RequestBody BatchCheckInRequest request) {
                BatchCheckInResponse response = ticketService.processBatchCheckIn(request);
                return ResponseEntity.ok(ApiResponse.<BatchCheckInResponse>builder()
                                .status(ApiResponse.Status.builder().code(200)
                                                .message("Đồng bộ danh sách Check-in hoàn tất!").build())
                                .data(response)
                                .build());
        }
}
