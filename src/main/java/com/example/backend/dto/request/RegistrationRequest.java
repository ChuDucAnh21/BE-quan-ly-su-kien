package com.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationRequest {
    
    @NotNull(message = "Lỗi: Chưa truyền ID sự kiện")
    private Long eventId;
}
