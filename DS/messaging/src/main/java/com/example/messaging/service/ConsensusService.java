package com.example.messaging.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsensusService {

    private final NodeClusterService nodeClusterService;
    private int currentTerm = 1;

    public ConsensusService(NodeClusterService nodeClusterService) {
        this.nodeClusterService = nodeClusterService;
    }

    public synchronized String getLeader() {
        String leader = nodeClusterService.getPrimaryNode();
        if (leader == null || !nodeClusterService.isNodeActive(leader)) {
            return electNewLeader();
        }
        return leader;
    }

    public synchronized String electNewLeader() {
        List<String> activeNodes = nodeClusterService.getActiveNodes();
        if (activeNodes.isEmpty()) {
            throw new IllegalStateException("No active nodes available for leader election");
        }

        String newLeader = activeNodes.get(0);
        nodeClusterService.promoteNode(newLeader);
        currentTerm++;
        return newLeader;
    }

    public synchronized Map<String, Object> consensusStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("leader", nodeClusterService.getPrimaryNode());
        result.put("term", currentTerm);
        result.put("activeNodes", nodeClusterService.getActiveNodes());
        return result;
    }
}