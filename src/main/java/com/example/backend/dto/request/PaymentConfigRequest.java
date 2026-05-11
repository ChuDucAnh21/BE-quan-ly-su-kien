package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentConfigRequest {

    @NotBlank(message = "INVALID_KEY_CLIENT_ID")
    private String clientId;

    @NotBlank(message = "INVALID_KEY_API_KEY")
    private String apiKey;

    @NotBlank(message = "INVALID_KEY_CHECKSUM_KEY")
    private String checksumKey;
}
