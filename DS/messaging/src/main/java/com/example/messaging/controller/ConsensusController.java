package com.example.messaging.controller;

import com.example.messaging.service.ConsensusService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/consensus")
public class ConsensusController {

    private final ConsensusService consensusService;

    public ConsensusController(ConsensusService consensusService) {
        this.consensusService = consensusService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return consensusService.consensusStatus();
    }

    @PostMapping("/elect")
    public Map<String, Object> elect() {
        consensusService.electNewLeader();
        return consensusService.consensusStatus();
    }
}