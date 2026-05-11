package com.example.backend.dto.request;

import com.example.backend.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequest {
    // DTO nhận yêu cầu thay đổi trạng thái tài khoản (Khóa/Mở Khóa) do Admin thao tác

    @NotNull(message = "Status không được để trống")
    private UserStatus status; // Trạng thái mới (ACTIVE, LOCKED)
}
