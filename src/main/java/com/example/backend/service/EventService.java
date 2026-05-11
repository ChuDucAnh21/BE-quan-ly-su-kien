package com.example.backend.service;

import com.example.backend.dto.request.EventRequest;
import com.example.backend.dto.response.EventResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.enums.EventStatus;

public interface EventService {
    
    // Tìm kiếm sự kiện đa năng (dành cho mọi người hoặc list của Organizer)
    PageResponse<EventResponse> getAllEvents(int page, int size, String keyword, String category, EventStatus status, Long organizerId);
    
    // Lấy chi tiết 1 sự kiện
    EventResponse getEventById(Long id);
    
    // Organizer/Admin tạo sự kiện
    EventResponse createEvent(EventRequest request);
    
    // Organizer sửa sự kiện
    EventResponse updateEvent(Long eventId, EventRequest request);
    
    // Organizer/Admin đổi trạng thái hoặc Hủy
    void changeEventStatus(Long eventId, EventStatus newStatus);
    
    // Xóa cứng sự kiện ra khỏi hệ thống (Chỉ áp dụng khi chưa bán được vé nào)
    void deleteEvent(Long eventId);
}
