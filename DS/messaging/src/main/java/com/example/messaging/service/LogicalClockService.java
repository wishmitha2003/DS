package com.example.messaging.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class LogicalClockService {

	private final AtomicLong counter = new AtomicLong(0);

	public long tick() {
		return counter.incrementAndGet();
	}

	public long current() {
		return counter.get();
	}
}
