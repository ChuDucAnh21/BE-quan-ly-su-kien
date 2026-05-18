package com.example.backend.dto.response;

import com.example.backend.enums.Gender;
import com.example.backend.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String qrCode; // Mã gốc (Chỉ trả về cho Khách)
    private String hashedQrCode; // Mã băm (Chỉ trả về cho Staff Offline Sync)
    private Long eventId;
    private String eventTitle;
    private BigDecimal ticketPrice;
    private Long userId;
    private Long registrationId;
    private String registrationCode;
    private String userEmail;
    private String userFullName;
    private Gender genderUser;
    private String avatarUrlUser;
    private String addressUser;
    private String phoneNumber;
    private TicketStatus status;
    private LocalDateTime checkedInAt;
    private LocalDateTime issuedAt;
}
