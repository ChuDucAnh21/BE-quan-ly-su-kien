package com.example.backend.service;

import com.example.backend.dto.request.EventScheduleRequest;
import com.example.backend.dto.response.EventScheduleResponse;

import java.util.List;

public interface EventScheduleService {

    // (Public) Ai cũng có thể xem lịch của 1 sự kiện
    List<EventScheduleResponse> getSchedulesByEventId(Long eventId);

    // (Organizer) Thêm NHIỀU lịch trình con mới vào Sự kiện cùng lúc (Batch Create)
    List<EventScheduleResponse> createSchedules(Long eventId, List<EventScheduleRequest> requests);

    // (Organizer) Đồng bộ (Sửa/Xóa/Thêm) toàn bộ Lịch trình bằng cách Ghi đè (Replace All)
    List<EventScheduleResponse> updateSchedules(Long eventId, List<EventScheduleRequest> requests);

    // (Organizer) Xóa sạch toàn bộ Lịch trình con của Sự kiện
    void deleteAllSchedules(Long eventId);

    // (Organizer) Xóa từng Lịch trình con lẻ tẻ
    void deleteSchedule(Long eventId, Long scheduleId);
}
