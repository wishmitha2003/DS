package com.example.messaging.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeduplicationService {

    private final Set<String> processedClientIds = ConcurrentHashMap.newKeySet();

    public boolean isDuplicate(String clientMessageId) {
        return clientMessageId != null && processedClientIds.contains(clientMessageId);
    }

    public void markProcessed(String clientMessageId) {
        if (clientMessageId != null && !clientMessageId.isBlank()) {
            processedClientIds.add(clientMessageId);
        }
    }
}