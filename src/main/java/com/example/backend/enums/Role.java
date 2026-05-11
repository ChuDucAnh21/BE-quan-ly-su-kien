package com.example.backend.enums;

public enum Role {
    ADMIN,      // Quản trị viên hệ thống (có quyền cao nhất)
    ORGANIZER,  // Nhà tổ chức sự kiện (được tạo và quản lý sự kiện riêng)
    STAFF,      // Nhân viên hỗ trợ sự kiện (được phân công check-in)
    USER        // Người dùng bình thường (chủ yếu là khách mua vé)
}
