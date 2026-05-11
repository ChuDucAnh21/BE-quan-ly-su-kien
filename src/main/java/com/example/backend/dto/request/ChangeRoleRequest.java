package com.example.backend.dto.request;

import com.example.backend.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    // DTO nhận yêu cầu cập nhật phân quyền từ Frontend do Admin thao tác

    @NotNull(message = "Role không được để trống")
    private Role role; // Role mới (ADMIN, ORGANIZER, STAFF, USER)
}
