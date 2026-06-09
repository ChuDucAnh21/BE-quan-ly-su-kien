package com.example.backend.service;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.StaffResponse;
import com.example.backend.enums.UserStatus;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;

public interface EventStaffService {

    // API 1: Hiển thị rổ Staff toàn hệ thống (dành cho Admin & Organizer lựa chọn)
    List<StaffResponse> getAllAvailableStaffs();

    // API 2: Lấy danh sách Staff đang được phân công vào Event cụ thể
    PageResponse<StaffResponse> getStaffsByEventId(Long eventId, String search, UserStatus status, Pageable pageable);

    // API 3: Đồng bộ danh sách Staff: Xoá cũ dư thừa, thêm mới chưa có, giữ nguyên
    // người hiện tại.
    List<StaffResponse> syncEventStaffs(Long eventId, Set<Long> staffIds);

    // API 4: Xoá thẻ phân công của 1 nhân viên ra khỏi Event
    void removeStaffFromEvent(Long eventId, Long staffId);
}
