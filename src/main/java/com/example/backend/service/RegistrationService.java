package com.example.backend.service;

import com.example.backend.dto.request.RegistrationRequest;
import com.example.backend.dto.response.RegistrationResponse;

public interface RegistrationService {
    RegistrationResponse registerEvent(RegistrationRequest request);
    void cancelRegistration(Long registrationId);
}
