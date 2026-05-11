package com.example.backend.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface WebhookService {
    ObjectNode processWebhook(ObjectNode payload);
}
