package com.example.messaging.service;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TimeSyncService {

	public Instant correctedNow(String nodeId) {
		return Instant.now();
	}
}
