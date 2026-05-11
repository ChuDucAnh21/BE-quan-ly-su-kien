package com.example.backend.enums;

public enum UserStatus {
    ACTIVE, // Đang hoạt động: Có thể login và dùng app bình thường
    LOCKED  // Đã bị khóa: Không thể login (Thường dùng cho xóa mềm hoặc vi phạm)
}
