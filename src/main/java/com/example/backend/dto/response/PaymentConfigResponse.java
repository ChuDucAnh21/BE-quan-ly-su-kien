package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentConfigResponse {
    private Long id;
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private Boolean isActive;
    private LocalDateTime updatedAt;
}
