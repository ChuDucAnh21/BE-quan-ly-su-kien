package com.example.backend.controller;

import com.example.backend.service.WebhookService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    // Tự động đón Webhook từ hệ thống PayOS trả về mỗi khi có giao dịch thành công
    // hoặc thất bại
    @PostMapping("/webhook")
    public ObjectNode handlePayOSWebhook(@RequestBody ObjectNode payload) {
        log.info("Nhận được Webhook từ PayOS: {}", payload.toString());
        // Return trực tiếp ObjectNode cho đúng format PayOS yêu cầu (PayOS cần HTTP 200
        // OK kèm body JSON)
        return webhookService.processWebhook(payload);
    }
}
