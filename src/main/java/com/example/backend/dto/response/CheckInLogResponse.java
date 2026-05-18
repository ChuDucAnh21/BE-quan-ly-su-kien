package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInLogResponse {
    private Long id;
    private String hashedQrCode;
    private String customerEmail;
    private String customerFullName;
    private LocalDateTime checkedInAt;
    private String staffEmail;
    private String checkInMethod; // QR or MANUAL
}
