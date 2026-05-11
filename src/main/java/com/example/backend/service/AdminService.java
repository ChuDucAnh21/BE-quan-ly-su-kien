package com.example.backend.service;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.UserProfileResponse;
import com.example.backend.enums.Role;
import com.example.backend.enums.UserStatus;

public interface AdminService {
    // 1. Phân quyền Người dùng
    void changeUserRole(Long userId, Role newRole);

    // 2. Thay đổi trạng thái Người dùng (Khóa / Mở Khóa)
    void changeUserStatus(Long userId, UserStatus newStatus);

    // 3. Xóa tài khoản vĩnh viễn dành cho Super Admin
    void deleteAccountUser(Long idUser);

    // 4. Lấy danh sách toàn bộ Người dùng, hỗ trợ tìm kiếm, lọc và sắp xếp
    PageResponse<UserProfileResponse> getAllUsers(int page, int size, String keyword, Role role, UserStatus status,
            String sort);
}
