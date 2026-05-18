package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {
    @NotBlank(message = "Mã QR code không được để trống")
    private String qrCode;

    @jakarta.validation.constraints.NotNull(message = "ID của sự kiện không được để trống")
    private Long eventId;
}
