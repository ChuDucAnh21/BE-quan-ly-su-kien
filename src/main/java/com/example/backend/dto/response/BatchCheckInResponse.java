package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCheckInResponse {
    private int total;
    private int successfulCount;
    private int failedCount;
    private List<TicketResponse> successfulTickets;
    private List<BatchCheckInError> errors;

    @Data
    @AllArgsConstructor
    public static class BatchCheckInError {
        private String hashedQrCode;
        private String errorMessage;
    }
}
