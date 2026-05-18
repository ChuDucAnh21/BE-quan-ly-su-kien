package com.example.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchCheckInRequest {
    @NotEmpty(message = "Danh sách mã QR không được để trống")
    private List<String> hashedQrCodes;

    @jakarta.validation.constraints.NotNull(message = "ID của sự kiện không được để trống")
    private Long eventId;
}
