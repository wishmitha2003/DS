package com.example.messaging.controller;

import com.example.messaging.service.TimeSyncService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// Updated to force editor refresh
@CrossOrigin("*")
@RestController
@RequestMapping("/api/time")
public class TimeController {

    private final TimeSyncService timeSyncService;

    public TimeController(TimeSyncService timeSyncService) {
        this.timeSyncService = timeSyncService;
    }

    @PostMapping("/offset/{nodeId}/{millis}")
    public Map<String, Long> updateOffset(@PathVariable String nodeId, @PathVariable long millis) {
        timeSyncService.setOffset(nodeId, millis);
        return timeSyncService.getOffsets();
    }

    @GetMapping("/offsets")
    public Map<String, Long> offsets() {
        return timeSyncService.getOffsets();
    }
}