package com.example.messaging.service;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TimeSyncService {

    private final java.util.Map<String, Long> timeOffsets = new java.util.concurrent.ConcurrentHashMap<>();

    public void setOffset(String nodeId, long millis) {
        timeOffsets.put(nodeId, millis);
    }

    public java.util.Map<String, Long> getOffsets() {
        return timeOffsets;
    }

	public Instant correctedNow(String nodeId) {
        long offset = timeOffsets.getOrDefault(nodeId, 0L);
		return Instant.now().plusMillis(offset);
	}
}
