package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PublicUserProfileResponse {
    private Long id;
    private String fullName;
    private String avatarUrl;
    private String role;
    private String gender;
    private LocalDateTime createdAt;
}
